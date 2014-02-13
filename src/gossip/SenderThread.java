package gossip;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import common.Log;
import common.Parameters;

public class SenderThread implements Runnable{
	MemberList memberList;
	DatagramSocket sendSocket;
	int gossipPeriod = Parameters.gossipPeriod; //Gossip period in milliseconds
	int failureRate = Parameters.failureRate;
	private volatile int finished = 1;
	
	public SenderThread(MemberList member_list){
		this.memberList = member_list;
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	//How many times do you want to gossip before terminating
	public void terminate(int num){
		finished = -1*num;
	}

	public void run() {
		
		String info = "sender thread started.";
		Log.info(info);
		//System.out.println(info);
		Thread sg = null;
		
		if(finished != 0){
			
			Log.join("added " + Gossip.ownInfo.getId().getString() + 
					" to the system at " + System.currentTimeMillis());
			
			sendContact();
			
			//It will schedule threads from a pool to periodically gossip
			/*
			ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(2);
			threadPool.scheduleAtFixedRate(new SendGossip(memberList,failureRate), gossipPeriod, gossipPeriod, TimeUnit.MILLISECONDS);
			
			
			long start = System.currentTimeMillis();
			long previousData = Parameters.dataUsage.get();
			*/
			
			while(finished != 0){
				
				sg = new Thread(new SendGossip(this.memberList));
				sg.start();
				
				try {
					Thread.sleep(this.gossipPeriod);
				} catch (InterruptedException e) {
					Log.warn("sender thread's sleep interrupted. Continuing with other threads.");
				}
				
				/*
				long current = System.currentTimeMillis();
				if((current - start) > Parameters.BandwidthPrintInterval){
					long currentData = Parameters.dataUsage.get();
					Log.bytesSent((currentData-previousData) + "|" + current);
					previousData = currentData;
					start = current;//System.currentTimeMillis();
				}
				
				long currentData = Parameters.dataUsage.get();
				Log.bytesSent((currentData-previousData) + "|" + System.currentTimeMillis());
				previousData = currentData;
				*/
				if(finished < 0){
					++finished;
				}
				
			}
			
			if(finished == 0){
				if(sg!=null){
					try {
						sg.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				info = "sender thread finished.";
				Log.info(info);
				//System.out.println(info);
			}
		}
	}
	
	public void sendContact() {
		
		//Put the member list in a packet and send
		String[] toSend = MySerializer.serialize(memberList);
		
		//Since it is just the machine itself at this stage, we can use the first index
		byte sendData[] = toSend[0].getBytes();
		
		List<String> ipToContact = new ArrayList<String>();
		
		if(Parameters.isContactIP){
			HashSet<String> ipSet = BackupIP.getBackupIP();
			if(ipSet.size() == 0){
				return;
			}
			for(String ip : ipSet){
				ipToContact.add(ip);				
			}
		} else {
			ipToContact.add(Parameters.contactIP);
		}
		
		for(String ip : ipToContact){
			
			InetAddress address = null;
			try {
				address = InetAddress.getByName(ip);
			} catch (UnknownHostException e) {
				if(Parameters.isContactIP){
					Log.warn("Couldn't find the back up IP machine " + ip);
				} else {
					Log.fatal("Couldn't find the contact machine. Exiting.");
					System.exit(0);
				}
			}
			
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, Parameters.gossipListenerPort);
			
			try {
				sendSocket.send(sendPacket);
				Log.sent("sent membership list to IP " + ip + " at " + System.currentTimeMillis() 
						+ ". List: " + toSend[0]);
			} catch (IOException e) {
				Log.error("could not send membership list to contact IP " + 
						Parameters.contactIP + " IOException occured in SenderThread at " + 
						System.currentTimeMillis() );
			}
		}
	}
}
