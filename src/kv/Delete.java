package kv;

import gossip.Gossip;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import common.Log;
import common.Parameters;

public class Delete {
	
	KeyValue kv;
	int quorum;	//stores the number of quorum servers
	HashMap<String, Value<String>> kvData;
	boolean keyPresent;
	boolean successful;
	AtomicInteger quorumCompleted;
	AtomicInteger numKeyPresent;
	AtomicInteger numSuccessful;
	int numServersToContact;
	Object lockObj;

	public Delete(KeyValue kv, int quorum){
		this.kv = kv;
		this.kvData = KVServer.kvData;
		this.quorum = quorum;
		this.keyPresent = false;
		this.successful = false;
		this.quorumCompleted = new AtomicInteger(0);
		this.numKeyPresent = new AtomicInteger(0);
		this.numSuccessful = new AtomicInteger(0);
		this.numServersToContact = Parameters.numReplica;
		this.lockObj = new Object();
	}
	
	public boolean isKeyPresent() {
		return keyPresent;
	}
	
	public boolean isSuccessful() {
		return successful;
	}
	
	public void delete(){
		
		if(kv.getKey() == null){
			this.keyPresent = false;
			return;
		}
		
		boolean self = false;
		synchronized (Gossip.ownInfo) {
			if(Gossip.ownInfo.isInRange(kv.getKey())){
				self = true;
			}
		}
		
		if(self){
			synchronized (kvData) {
				if(kvData.containsKey(kv.getKey())){
					this.keyPresent = true;
					kvData.remove(kv.getKey());
					this.successful = true;
				}
			}
			
			if(this.successful){
				String info = "The key value pair " + kv.getKey() + ":" + kv.getValue().get()
						+ " has been deleted";
				Log.keyDelete(info);
				//System.out.println(warn);
			}
		}
		
		if(this.quorum == KVServer.QUORUMDISABLE){
			//System.out.print("Returning due to DISABLE - q: " + this.quorum + ":" + this.successful);
			return;
		}
		
		if(self){
			quorum--;
			numServersToContact--;
		}
		
		List<String> nodeIPList = LookUp.getOtherNodeIP(kv);
		
		if(nodeIPList.size() < quorum){
			String str = "Quorum size is more than the number of up servers. Taking only "
					+ "the available servers";
			Log.error(str);
			System.out.println(str);
			quorum = nodeIPList.size();
		}
		
		if(numServersToContact > nodeIPList.size()){
			String str = "Can't locate enough servers with data. Taking only "
					+ "the available servers";
			Log.error(str);
			System.out.println(str);
			numServersToContact = nodeIPList.size();
		}
		
		CoordinatorThread[] crdTh = new CoordinatorThread[numServersToContact];
		synchronized(this.lockObj){
			for(int i=0; i<numServersToContact; i++){
				crdTh[i] = new CoordinatorThread(KVServer.CMDDELETE, 
						nodeIPList.get(i), this.kv, this.quorumCompleted, this.numKeyPresent,
						this.numSuccessful, this.lockObj);
				new Thread(crdTh[i]).start();
			}
			
			while(this.quorumCompleted.get() < this.quorum){
				try{
					this.lockObj.wait(Parameters.maxWaitForReply);
				} catch(InterruptedException e){
					String str = "Quorum waiting has been interrupted.";
					Log.warn(str);
					System.out.println(str);
					e.printStackTrace();
				}
			}
			
			if(numKeyPresent.get() > 0)	this.keyPresent = true;
			if(numSuccessful.get() > 0)	this.successful = true;
		}
	}
}
