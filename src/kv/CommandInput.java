package kv;

import gossip.Gossip;
import gossip.ListenerThread;
import gossip.MemberList;
import gossip.SenderThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import common.Log;
import common.Parameters;

public class CommandInput {
	
	public static Thread send = null;
	public static Thread listen = null;
	public static SenderThread st = null;
	public static ListenerThread lt = null;
	public static String input = "";
	private static boolean joined = false;
	
	public static void start (){
		
		while(true){
			
			System.out.print("Command: ");
			try{
			    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			    input = bufferRead.readLine();	    
			}
			catch(IOException e)
			{
				String error = "IOException in reading from console. Exiting.";
				Log.fatal(error);
				System.exit(0);
			}
			
			if(input.equals("join")){
				CommandInput.join();			
			} else if (input.equals("leave")){
				CommandInput.leave();
			} else if (input.equals("info")){
				if(joined){
					System.out.println(Gossip.memberList.getMemberIds());
					synchronized (KVServer.kvData) {
						System.out.println("Total Entries: " + KVServer.kvData.size());
					}
					
				}
			} else if (input.equals("show")){
				if(joined){
					synchronized (KVServer.lastRead) {
						for(String str : KVServer.lastRead){
							System.out.println(str);
						}
					}
					
					synchronized (KVServer.lastWrite) {
						for(String str : KVServer.lastWrite){
							System.out.println(str);
						}
					}
					
				}
			} else if (input.equals("print")){
				if(joined){
					System.out.println(Gossip.memberList.getMemberInfo());
					if(Gossip.sucessorInfo!=null)
						System.out.println("Successor : " + Gossip.sucessorInfo.getHash());
				}
			} else if (input.equals("deleteall")){
				synchronized (KVServer.kvData) {
					KVServer.kvData.clear();
				}
			} else if (input.equals("exit")){
				CommandInput.exit();
			} else {
				System.out.println("Invalid Input. Valid values are: join, leave, show, print and exit");
			}
		}
	}
	
	private static void join(){
		
		if(!joined){
			Gossip.ownInfo = Gossip.getNewInfo();
			 KVServer.kvData = new HashMap<String, Value<String>>();
			Log.info("node started with timestamp " + Gossip.ownInfo.getId().getTimestamp());
			Gossip.memberList = new MemberList();
			
			//periodically gossip
			st = new SenderThread(Gossip.memberList);
			send = (new Thread(st));
			send.start();
			
			//accepts gossip messages and processes them
			lt = new ListenerThread(Gossip.memberList);
			listen = (new Thread(lt));
			listen.start();
			joined = true;
		} else {
			String warn = "the system has already joined the group. cannot join again";
			Log.warn(warn);
			System.out.println(warn);
		}
	}
	
	private static void leave(){
		
		//KVServer.copyKVToSuccessor();
		
		synchronized (Gossip.ownInfo) {
			Gossip.ownInfo.setHeartBeat(Long.MAX_VALUE);
		}
		
		if(send != null && send.isAlive()){
			
			st.terminate(Parameters.timesToGossipBeforeLeaving);
			
			try {
				send.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(listen != null  && listen.isAlive()){
			lt.terminate();
			try {
				listen.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(Gossip.ownInfo != null){
			String str = Gossip.ownInfo.getId().getString();
			Log.fail("failure detected for" + str + " at " + System.currentTimeMillis());
			Gossip.ownInfo = null;
		}
		Gossip.memberList = null;
		Gossip.sucessorInfo = null;
		KVServer.kvData.clear();
		joined = false;
	}
	
	private static void exit(){
		//To ensure that packets are sent completely and no half packets are sent
		//which might cause data corruption.
		if(send != null && send.isAlive()){
			st.terminate(0);
			try {
				send.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String fatal = "exit command issued, killed all the threads.";
		Log.fatal(fatal);
		joined = false;
		System.exit(0);
	}
}
