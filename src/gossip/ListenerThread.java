package gossip;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import common.Log;
import common.Parameters;


public class ListenerThread implements Runnable{
	
	MemberList memberList;
	static final int listenerPort = Parameters.gossipListenerPort;
	int threadPoolSize = Parameters.listenerThreadPoolSize;
	private volatile boolean finished = false;
	
	public ListenerThread(MemberList member_list) {
		this.memberList = member_list;
	}
	
	public void terminate(){
		finished = true;
	}
	
	public void run() {
		
		String info = "listener thread started.";
		Log.info(info);
		//System.out.println(info);
		Thread lt = null;
		
		//ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		
		while(!finished) {
			byte[] receiveData = new byte[1024];
			// Accept Packet
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			DatagramSocket receiveSocket = null;
			try {
				receiveSocket = new DatagramSocket(listenerPort);
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				Log.error("Could not receive packet properly. IOException occurred.");
			}
			
			// Start a new thread that will read the packet and merge the incoming list and existing list
			/*
			try {
				Runnable updater = new ListUpdaterThread(receivePacket, memberList) ;
				executor.execute(updater);
			} catch(RejectedExecutionException e) {
				Log.error("insufficient number of threads in the executor pool");
			}
			*/
			
			lt = new Thread(new ListUpdaterThread(receivePacket, memberList));
			lt.start();
			receiveSocket.close();
		}
		
		if(finished){
			if(lt!=null){
				try {
					lt.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			info = "listener thread finished.";
			Log.info(info);
			//System.out.println(info);
		}
		
	}
}
