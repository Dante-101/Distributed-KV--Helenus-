package kv;

import gossip.Gossip;

import java.util.List;

import common.Log;

public class SyncData implements Runnable{
	
	public static Object notifySync = new Object();
	boolean start = true;
	
	public void run(){
		synchronized(notifySync){
			while(true){
				
				try{
					if(!start){
						System.out.println("Sync complete");
					}
					notifySync.wait();
					System.out.println("\nSyncing..");
					start = false;
					//Thread.sleep(Parameters.nodeStabilizeTime);
					//System.out.println("Out of sleep");
				} catch (InterruptedException e){
					String str = "Wait for node stabilization has been interrupted";
					Log.warn(str);
					System.out.println(str);
				}
				
				
				List<String> ipList = Gossip.memberList.getMemberIPs();
				Thread[] crdTh = new Thread[ipList.size()];
				for(int i=0; i<ipList.size(); i++){
					crdTh[i] = new Thread(new CoordinatorThread(KVServer.CMDDATAGET,ipList.get(i)));
					crdTh[i].start();
				}
				
				for(Thread th : crdTh){
					try {
						th.join();
					} catch (InterruptedException e) {
						String str = "Coordinator thread join interrupted";
						Log.warn(str);
						System.out.println(str);
						e.printStackTrace();
					}
				}
				
				new Thread(new DeleteSelfData()).start();
				
			}
			
		}
	}

}
