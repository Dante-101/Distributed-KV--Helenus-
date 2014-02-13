package gossip;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import common.Log;
import common.Parameters;

public class ParallelGossiperThread implements Runnable {
	String[] sendData;
	InetAddress address;
	DatagramPacket packet;
	DatagramSocket socket;
	
	public ParallelGossiperThread(InetAddress address, String[] sendData) {
		this.address = address;
		this.sendData = sendData;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		
		for(String data : sendData){
			double random = Math.random()*100;
			if(data != null && random >= Parameters.failureRate){
			
				Parameters.dataUsage.addAndGet(data.getBytes().length);
				//System.out.print("\n******" + data.getBytes().length + "*******\n");
				packet = new DatagramPacket(data.getBytes(), data.getBytes().length , address, Parameters.gossipListenerPort);
				try {
					socket.send(packet);
					Log.sent("sent data to " + packet.getAddress() + " | " + System.currentTimeMillis() + " | : " + data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
