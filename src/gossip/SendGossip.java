package gossip;

import java.net.InetAddress;

import common.Parameters;

public class SendGossip implements Runnable {
	MemberList memberList;
	InetAddress[] addresses;
	int numProcesses;  // no. of processes in the system
	//int numToGossip;  // no.of processes each process gossips to at every iteration
	int threadPoolSize = Parameters.senderThreadPoolSize;
	
	public SendGossip(MemberList member_list){
		this.memberList = member_list;
	}
	
	public void run() {
		//Log.debug(Thread.currentThread().getName() + ": SendGossip started at " + System.currentTimeMillis());
		//this.numToGossip = Parameters.getNumToGossip();
		this.addresses = Parameters.getIPToGossip();
		
		/*
		String addrDebug = "";
		for (InetAddress addr : addresses){
			addrDebug += addr.getHostAddress() + " ; ";
		}
		
		Log.debug(Thread.currentThread().getName() + ": addresses to send data - " + addrDebug);
		*/
		
		sendPackets(addresses);
	}
	
	public void sendPackets(InetAddress[] addresses) {
		//Serialize the member list
		String[] toSend = MySerializer.serialize(memberList);
		
		//Create a thread pool executor so that all messages go out at the same time
		//ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		for (InetAddress address : addresses) {
			/*
			try {
				Runnable parallelGossiper = new ParallelGossiperThread(address, toSend) ;
				executor.execute(parallelGossiper);
			} catch(RejectedExecutionException e) {
				System.out.println("Insufficient number of threads in the executor pool");
			} 
			*/
			new Thread(new ParallelGossiperThread(address, toSend)).start();
		}
		
	}

}
