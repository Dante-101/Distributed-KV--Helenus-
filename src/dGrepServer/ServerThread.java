package dGrepServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * ServerThread represents one thread which communicates with one of the clients in the system 
 * The receives the key and value pair regex to grep and sends the results back to them
 * @author Gaurav, Tanvi
 *
 */
public class ServerThread implements Runnable {

    Socket clientSocket = null;
    String logFileName = "";

    /**
     * @param clientSocket socket to send data to and receive data from
     * @param logFileName file to grep
     */
    public ServerThread(Socket clientSocket, String logFileName) {
        this.clientSocket = clientSocket;
        this.logFileName = logFileName;
    }
    
    public ServerThread(String logFileName) {
        this.clientSocket = null;
        this.logFileName = logFileName;        
    }
    
    /**
	 * @param key regex to search in the key part of the key-value pair
     * @param value regex to search in the value part of the key-value pair
     * @param param list of parameters to pass to grep
     * @param resultList list of string to store the grep result
     */
    public void pGrep (String key, String value, List<String> param, List<String> resultList){
    	
    	List<String> cmd = new ArrayList<String>();
		
		cmd.add("grep");
		
		//Adding all the parameters
		for(String str : param){
			cmd.add(str);
		}
		
		//Manipulating regex as per the file format to give the correct results
		if(key.startsWith("^")){
			key = key.replace("^", "");
			if(key.endsWith("$")){
				key = key.replace("$", "");
			} else {
				key = key + ".*";
			}
		} else {
			if(key.endsWith("$")){
				key = key.replace("$", "");
			}
		}
		
		if(value.startsWith("^")){
			value = value.replace("^", "");
		} else {
			value = ".*" + value;
		}
		
		String regex = key + ":" + value; 
		
		cmd.add(regex);
		cmd.add(logFileName);
		//Running the regex in a process
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process process = null;
		try {
			process = pb.start();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			String result;			
			//Storing the results the string list
			while((result=br.readLine()) != null){
				resultList.add(result);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
    }

    public void run() {
    	
    	String threadName;
    	synchronized (this) {
			threadName = Thread.currentThread().getName();
		}

    	//System.out.println(threadName + " connected to " + clientSocket.getInetAddress() + 
    	//		" : " + clientSocket.getPort());
    	
    	try {
			InputStream is = clientSocket.getInputStream();
			OutputStream os = clientSocket.getOutputStream();
		
			DataInputStream dis = new DataInputStream(is);
			DataOutputStream dos = new DataOutputStream(os);
			
			List<String> param = new ArrayList<String>();
		
			//Read parameters from clientThread
			int size = dis.readInt();
			for(int i=0;i<size;i++){
				param.add(dis.readUTF());
			}
    	
			//Reads key and value from the clientThread
			String key = dis.readUTF();
			String value = dis.readUTF();
			
			List<String> resultList = new ArrayList<String>();
			
			//Runs grep and stores the result
			pGrep(key, value, param, resultList);
			
			/*
			if(resultList.size() == 0){
				resultList.add("No results");
			}*/
			
			dos.writeInt(resultList.size());
			
			//Writes the result back
			for(String str : resultList){
				dos.writeUTF(logFileName + " :: " + str);
			}
			
			clientSocket.close();
			
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println(threadName + " at port " + clientSocket.getLocalPort() + 
	    			" connected to " + clientSocket.getInetAddress() + " : " + clientSocket.getPort()
	    			+ "has encountered IOException");
		}
    	
    	//System.out.println(threadName + " has ended");
    }
}
