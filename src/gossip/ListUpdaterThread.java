package gossip;
import java.net.DatagramPacket;

import common.Log;


public class ListUpdaterThread implements Runnable{
	
	DatagramPacket packet;
	MemberList memberList;
	
	public ListUpdaterThread(DatagramPacket packet, MemberList member_list) {
		this.packet = packet;
		this.memberList = member_list;
	}
	
	public void run(){
		//Retrieve the received memberList from the packet
		String received = new String(packet.getData(),0,packet.getLength() );
		Log.receive("received membership list from " + packet.getAddress() + 
				" at " + System.currentTimeMillis() + " | " + received);
		MemberList receivedMemberList = MySerializer.deserialize(received);
		/*
		//If the member list has just one element and has heartbeat of 1, we send our own list
		List<Entry> newJoin = receivedMemberList.getMembers();
		if(newJoin.size() == 1 && newJoin.get(0).getIdHeartBeat().getHeartBeat() == 1){
			new Thread(new SendGossip(this.memberList, Parameters.failureRate)).start();
		}
		*/
		//Merge it with the existing memberList
		memberList.MergeNewList(receivedMemberList);
		//memberList.logDump();
	}
}
