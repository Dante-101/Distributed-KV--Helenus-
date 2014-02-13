package kv;

import gossip.Gossip;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Log;
import common.Parameters;

public class DeleteSelfData implements Runnable {
	
	public void run(){
		
		try{
			Thread.sleep(Parameters.selfDeleteTime);
		} catch (InterruptedException e){
			String str = "Wait for node stabilization for deletion has been interrupted";
			Log.warn(str);
			System.out.println(str);
		}
		
		List<HashRange> ownHrList = Gossip.ownInfo.getRangeList();
		Set<String> keysToRemove = new HashSet<String>();
		synchronized (KVServer.kvData) {
			for(Map.Entry<String, Value<String>> entry : KVServer.kvData.entrySet()){
				boolean remove = true;
				for(HashRange hr : ownHrList){
					if(hr.isInRange(entry.getKey())){
						remove = false;
					}
				}
				if(remove){
					keysToRemove.add(entry.getKey());
				}
			}
			KVServer.kvData.keySet().removeAll(keysToRemove);
		}
	}
}
