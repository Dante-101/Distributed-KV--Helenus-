package kv;

import gossip.Gossip;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import common.Log;
import common.Parameters;

public class Insert {
	KeyValue kv;
	int quorum;
	HashMap<String, Value<String>> kvData;
	boolean keyPresent;
	boolean successful;
	AtomicInteger quorumCompleted;
	AtomicInteger numKeyPresent;
	AtomicInteger numSuccessful;
	int numServersToContact;
	Object lockObj;

	public Insert(KeyValue kv, int quorum){
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
	
	public void insertKeyValue(){
		
		if(kv.getKey() == null){
			this.successful = false;
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
				}
				kvData.put(kv.getKey(), kv.getValue());
				this.successful = true;
			}
		
			if(this.successful){
				if(this.isKeyPresent()){
				
					String warn = "The key to be inserted " + kv.getKey() + " is already present. Updated it "
							+ "with the value " + kv.getValue().get(); 
					Log.warn(warn);
					Log.keyUpdate(warn);
				} else {
					String info = "Inserted key " + kv.getKey() + " with value " + kv.getValue().get(); 
					Log.keyInsert(info);
				}
			}
		}
		
		if(this.quorum == KVServer.QUORUMDISABLE){
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
		synchronized(lockObj){
			for(int i=0; i<numServersToContact; i++){
				crdTh[i] = new CoordinatorThread(KVServer.CMDINSERT, 
						nodeIPList.get(i), this.kv, this.quorumCompleted, this.numKeyPresent,
						this.numSuccessful, lockObj);
				new Thread(crdTh[i]).start();
			}
		
			while(this.quorumCompleted.get() < this.quorum){
				try{
					lockObj.wait(Parameters.maxWaitForReply);
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
