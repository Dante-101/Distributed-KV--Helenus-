package common;

import gossip.Entry;
import gossip.Gossip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Parameters {
	
	//static MemberList memberList;
	//Gossip period in milliseconds
	public static final int gossipPeriod = 500;
	//Maximum Number of entries to put in a UDP packet
	public static final int numPacketEntries = 2;
	public static final int timesToGossipBeforeLeaving = 2;
	public static final int nodeStabilizeTime = 1500;
	public static final int selfDeleteTime = nodeStabilizeTime*4;
	public static final int maxWaitForReply = 500;
	
	public static final int numReplica = 3;
	public static final int numQuorum = (int)(numReplica/2) + 1;
	public static final String valueDelimiter = "-";
	public static final String valueDelimiterRegex = "-";
	
	// no. of processes each process gossips to at every iteration
	public static int getNumToGossip(){ return 5; }
	
	public static int getTimeout(){ return 5000; }
	
	public static int getCleanup(){ return 2*getTimeout(); }
	
	public static AtomicLong dataUsage = new AtomicLong(0L);
	
	//static final long BandwidthPrintInterval = 500; 
	
	//Port to listen the program on
	public static final int gossipListenerPort = 34001;
	
	//Port to listen gossips on
	public static final int kvListenerPort = 34002;
	
	//To run dGrep on
	public static final int grepSeverPort = 34000;
	
	//ThreadPoolSize for listener
	public static final int listenerThreadPoolSize = 5;
	//ThreadPoolSize for listener
	public static final int senderThreadPoolSize = 5;
	
	//File containing the contact IP
	public static final String contactIPFile = "contact.txt";
	public static final String contactIP = getContactIP();
	
	//Failure Rate in percentage
	public static final String failureRateFile = "failure-rate.txt";
	public static final int failureRate = getFailureRate();
		
	//IP address of the machine on which the system is running
	//public static final String selfIP = getIPFromFile();
	public static final String selfIP = getIP();
	public static final String MachineIPFile = "machineIP.txt";
	//Logfile name to write the logs
	public static final String logFile = selfIP + ".log";
	
	public static final boolean isContactIP = selfIP.equals(contactIP) ? true : false;
	
	//File to backup the IP addresses to which the contact IP contacts
	public static final String backupIPFile = "backup-ip.txt";
	
	public static final String hashAlgo = "MD5";
	public static final int hashSize = 8;	//in bytes
	
	
	/************************** FUNCTIONS *****************************/

	// no. of processes in the system
	public static int getNumProcesses(){
		synchronized (Gossip.memberList) {
			return Gossip.memberList.getMembers().size();
		}
	}
	
	//Randomly select IP addresses from membership list
	public static InetAddress[] getIPToGossip(){
		InetAddress[] addresses;
		InetAddress address;
		
		//Randomly select the members to send gossip to
		List<Entry> entries = Gossip.memberList.getMembers();
		
		int numToGossip = getNumToGossip();
		
		int no;
		synchronized (entries) {
			no = entries.size();
		}
		
		if(no <= numToGossip){
			addresses = new InetAddress[no];
			for(int i=0; i<no; i++){
				String ip;
				synchronized (entries) {
					ip = entries.get(i).getMemberInfo().getId().getIPAdrress();
				}
				try {
					address = InetAddress.getByName(ip);
					addresses[i] = address;
				} catch (UnknownHostException e) {
					Log.error("could not find the member ip address " + ip);
					addresses[i] = null;
				} 
			}
			return addresses;
		}
		
		addresses = new InetAddress[numToGossip];
		//Pick numToGossip random numbers from 0 to (no-1)
		int[] a = new int[no];
		for (int i=0 ; i<no ;i++){
			a[i] = i;
		}
		
		for (int i=0 ; i<numToGossip ; i++) {
			//Pick one number and move it to the end of array and select from the remaining next time
			int random = (int) (Math.random()*(no-i));
			int random_index = a[random];
			a[random] = a[no-1-i];
			a[no-1-i] = random_index;
			
			String machineToSend;
			//Pick the entry at this index and then add to addresses the ip address of that entry
			synchronized (entries) {
				Entry entry = entries.get(random_index);
				machineToSend = entry.getMemberInfo().getId().getIPAdrress();
			}
			
			try {
				address = InetAddress.getByName(machineToSend);
				addresses[i] = address;
			} catch (UnknownHostException e) {
				Log.error("could not find the member ip address " + machineToSend);
				addresses[i] = null;
			}
		}
		return addresses;
	}
	
	private static String getIPFromFile(){
		String IP = "";
		try {	
			BufferedReader br = new BufferedReader(new FileReader(MachineIPFile));
			if((IP = br.readLine()) == null){
				Log.fatal("IP file is empty. Cannot get the IP of the current machine. Exiting");
				System.exit(0);
			}
			br.close();
		} catch (FileNotFoundException e) {
			Log.fatal("Could not find the IP file \"" + MachineIPFile +"\". Exiting.");
			System.exit(0);
		} catch (IOException e) {
			Log.warn("Could not read the IP file \"" + MachineIPFile +"\". Exiting.");
			System.exit(0);
		}
		return IP;
	}

	
	private static String getIP(){
		
		String ip="";
		
		try {
			Enumeration<NetworkInterface> netIntEnum = NetworkInterface.getNetworkInterfaces();
			while(netIntEnum.hasMoreElements()){
				NetworkInterface netInt = netIntEnum.nextElement();
				Enumeration<InetAddress> inetAddEnum = netInt.getInetAddresses();
				while(inetAddEnum.hasMoreElements()){
					InetAddress inetAdd = inetAddEnum.nextElement();
					if(inetAdd instanceof Inet4Address && !inetAdd.isLoopbackAddress()){
						//For Ubuntu Systems, they have this special IP for some purpose
						if(!inetAdd.getHostAddress().equals("127.0.1.1")){
							ip = inetAdd.getHostAddress();
							break;
						}
					}
				}
				if(!ip.equals("")){
					break;
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return ip;
	}

	
	private static int getFailureRate(){
		String failureRate = "";
		File failRateFile = new File(failureRateFile);
		try {	
			BufferedReader br = new BufferedReader(new FileReader(failRateFile));
			if((failureRate = br.readLine()) == null){
				Log.warn("Failure rate file is empty. Assuming 0.");
				failureRate = "0";
			}
			br.close();
		} catch (FileNotFoundException e) {
			Log.warn("Could not find failure rate file \"" + failRateFile +"\". Assuming 0.");
			failureRate = "0";
		} catch (IOException e) {
			Log.warn("Could not read the failure rate file \"" + failRateFile +"\". Assuming 0.");
			failureRate = "0";
		}
		
		return Integer.parseInt(failureRate);
	}
	
	
	private static String getContactIP(){
		String ip = "";
		
		File ipFile = new File(contactIPFile);
		try {	
			BufferedReader br = new BufferedReader(new FileReader(ipFile));
			
			//Reads a line and stores it in list
			if((ip = br.readLine()) == null){
				Log.fatal("Contact IP not found.");
				System.exit(0);
			}
			br.close();
		} catch (FileNotFoundException e) {
			Log.fatal("Could not find the Contact IP file \"" + contactIPFile +"\"");
			System.exit(0);
		} catch (IOException e) {
			Log.fatal("Could not read the Contact IP file \"" + contactIPFile +"\"");
			System.exit(0);
		}
		
		return ip;
	}
	
}
