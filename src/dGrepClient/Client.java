package dGrepClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the main program which starts the dGrepClient. It takes two inputs, key regex and value regex.
 * It reads from a IP file and creates thread for each connection to a server which handles the rest of the communication
 * 
 * @author Gaurav
 *
 */
public class Client implements Runnable{
	
	//Port to connect to the dGrepServer
	int port;

	//File which contains the IP addresses
	final static String ipFileName = "IP.txt";
	
	String key = "";
	String value = "";
	List<String> param = new ArrayList<String>();
	
	/**
	 * @param port port to connect to
	 * @param key regex to search in the key part of the key-value pair
     * @param value regex to search in the value part of the key-value pair
	 */
	public Client(int port, String key, String value, List<String> param){
		this.port = port;
		this.key = key;
		this.value = value;
		this.param = param;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run(){
		
		List<String> ipAddrList = new ArrayList<String>();

		//Reads all the IP address from the file
		getIPAddresses(ipAddrList);
		
		List<Thread> threadList = new ArrayList<Thread>();

		//Creates a socket and thread for each IP address
		for(String ipAddr : ipAddrList){
			Socket clientSocket = null;
			try {
				clientSocket = new Socket(ipAddr,port);
			} catch (IOException e) {
				//System.out.println("Cannot connect to " + ipAddr + " : " + port);
				continue;
			}
	        Thread t = new Thread( new ClientThread(clientSocket,this.key,this.value,this.param));
	        t.start();
	        threadList.add(t);	        
		}
		
		//Waits for all the threads to end
		for(Thread t : threadList){
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	  
    /**
     * Reads IP address from the file and stores them as a String list
     * 
     * @param ipAddrList list to store the IP address
     */
    public static void getIPAddresses(List<String> ipAddrList){
    	
		File ipFile = new File(ipFileName);
		try {	
			BufferedReader br = new BufferedReader(new FileReader(ipFile));
			String str;
			//Reads a line and stores it in list
			while((str = br.readLine()) != null){
				ipAddrList.add(str);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not find the IP file \"" + ipFileName +"\"");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not read the IP file \"" + ipFileName +"\"");
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args) {
		
    	//long start = System.currentTimeMillis();
    	
    	boolean INVALID_INPUT = false;
    	String key = "";
    	String value = "";
    	List<String> param = new ArrayList<String>();
    	
    	for(int i=0;i<args.length-2;i++){
    		if(args[i].startsWith("-")){
    			param.add(args[i]);
    		} else {
    			INVALID_INPUT = true;
    			break;
    		}
    	}
    	
		if(args.length >= 2){
				key = args[args.length-2];
				value = args[args.length-1];
		} else {
			INVALID_INPUT = true;
		}
		
		if(INVALID_INPUT){
			System.out.println("Invalid arguments. Please provide arguments as: [grep OPTIONS]"
					+ " <key pattern> <value pattern>");
			return;
		}
		
		Client client = new Client(20000, key, value, param);
		Thread t = new Thread(client);
		t.start();
		
		//Waits for the thread to end
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//long end = System.currentTimeMillis();
		//Prints the time taken by the whole process
		//System.out.println("Time Taken : " + (end-start) + " milliseconds");
		
	}
}
