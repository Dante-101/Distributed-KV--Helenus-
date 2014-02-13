package kv;

import gossip.Entry;
import gossip.Gossip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import common.Log;
import common.Parameters;

public class LookUp {
	
	KeyValue kv;
	int quorum;	//stores the number of quorum servers
	HashMap<String, Value<String>> kvData;
	boolean keyPresent;
	boolean successful;
	AtomicInteger quorumCompleted;
	AtomicInteger numKeyPresent;
	AtomicInteger numSuccessful;
	int numServersToContact;
	HashMap<String, Value<String>> valueMap;

	public boolean isKeyPresent() {
		return keyPresent;
	}

	public LookUp(KeyValue kv, int quorum){
		this.kv = kv;
		this.kvData = KVServer.kvData;
		this.quorum = quorum;
		this.keyPresent = false;
		this.quorumCompleted = new AtomicInteger(0);
		this.numKeyPresent = new AtomicInteger(0);
		this.numSuccessful = new AtomicInteger(0);
		this.numServersToContact = Parameters.numReplica;
		this.valueMap = new HashMap<String, Value<String>>();
	}
	
	public void getValue(){
		
		Long maxTimeStamp = Long.MIN_VALUE;
		
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
			Value<String> result = null;
			synchronized (kvData) {
				if(kvData.containsKey(kv.getKey())){
					this.keyPresent = true;
					result = new Value<String>(kvData.get(kv.getKey()).get());
					this.successful = true;
				}
			}
			
			if(this.successful){
				valueMap.put(Parameters.selfIP, result);
				kv.setValue(result);
				maxTimeStamp = Value.getTimeStamp(result);
				String str = "Key " + kv.getKey() + " found with value " + 
						result.get();
				//System.out.println(str);
				Log.keyLookUp(str);
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
		synchronized(valueMap){
			for(int i=0; i<numServersToContact; i++){
				crdTh[i] = new CoordinatorThread(KVServer.CMDLOOKUP, 
						nodeIPList.get(i), this.kv, this.quorumCompleted, this.numKeyPresent,
						this.numSuccessful, valueMap);
				new Thread(crdTh[i]).start();
			}
			
			while(this.quorumCompleted.get() < this.quorum){
				try{
					valueMap.wait(Parameters.maxWaitForReply);
				} catch(InterruptedException e){
					String str = "Quorum waiting has been interrupted.";
					Log.warn(str);
					System.out.println(str);
					e.printStackTrace();
				}
			}
			
			if(numKeyPresent.get() > 0)	this.keyPresent = true;
			if(numSuccessful.get() > 0)	this.successful = true;
						
			for(Map.Entry<String,Value<String>> entry : valueMap.entrySet()){
				Long timeStamp = Value.getTimeStamp(entry.getValue());
				if(timeStamp > maxTimeStamp){
					maxTimeStamp = timeStamp; 
					kv.setValue(entry.getValue());
				}
			}
				
			for(Map.Entry<String,Value<String>> entry : valueMap.entrySet()){
				Long timeStamp = Value.getTimeStamp(entry.getValue());
				if(timeStamp < maxTimeStamp){
					CoordinatorThread crdTh1 = new CoordinatorThread(KVServer.CMDINSERT, 
							entry.getKey(), this.kv);
					new Thread(crdTh1).start();
				}
			}
		}
	}
	
	public static List<String> getOtherNodeIP(KeyValue keyValue){
		
		List<String> ipList = new ArrayList<String>();
		
		/*
		synchronized (Gossip.ownInfo) {
			if(Gossip.ownInfo.isInRange(keyValue.getKey())){
				ipList.add(Parameters.selfIP);
			}
		}
		*/
		
		synchronized (Gossip.memberList) {
			for(Entry e : Gossip.memberList.getMembers()){
				if(e.getMemberInfo().isInRange(keyValue.getKey())){
					ipList.add(e.getMemberInfo().getId().getIPAdrress());
				}
			}
		}
		
		if(ipList.size() == 0){
			Log.error("Could not find the machine for this key. Unstable system.");
			String str = "Could not find the machine for this key.\n"
					+ "Mostly, the query was initialized before some operation could complete.\n"
					+ "The system may not work correctly now.\n"
					+ "Restarting everything is highly recommended.";
					
			System.out.println(str);
		}
		
		return ipList;
	}
	
	
}
