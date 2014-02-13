package dGrepClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * ClientThread represents one thread which communicates with one of the nodes in the system 
 * and sends it the key and value pair to grep and get the results back. 
 * 
 * @author Gaurav, Tanvi
 *
 */
public class ClientThread implements Runnable{ 
    Socket socket = null;
    String key = "";
    String value = "";
    boolean test;
    List<String> param;
    List<String> resultList;

    /**
     * This constructor is called when the thread is supposed to display the results on the terminal
     * 
     * @param clientSocket the socket to communicate through
     * @param key regex to search in the key part of the key-value pair
     * @param value regex to search in the value part of the key-value pair
     */
    public ClientThread(Socket clientSocket, String key, String value, List<String> param) {
        this.socket = clientSocket;
        this.key = key;
        this.value = value;
        this.test = false;
        this.resultList = null;
        this.param = param;
    }
    
    /**
     * This constructor is called when the object is required to store the results in a list.
     * It is used in unit testing to deliver the results back for comparison.
     * 
     * @param clientSocket the socket to communicate through
     * @param key regex to search in the key part of the key-value pair
     * @param value regex to search in the value part of the key-value pair
     * @param resultList a list of strings to store the grep results from the dGrepServer
     */
    public ClientThread(Socket clientSocket, String key, String value, List<String> param, List<String> resultList) {
        this.socket = clientSocket;
        this.key = key;
        this.value = value;
        this.param = param;
        this.test = true;
        this.resultList = resultList;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
    	
    	String threadName;
    	
    	synchronized (this) {
			threadName = Thread.currentThread().getName();
		}
    	
    	//System.out.println(threadName + " at port " + socket.getLocalPort() + 
    	//		" connected to " + socket.getInetAddress() + " : " + socket.getPort());
    	
    	try {
    		InputStream is = socket.getInputStream();
        	OutputStream os = socket.getOutputStream();
			
	    	DataInputStream dis = new DataInputStream(is);
	    	DataOutputStream dos = new DataOutputStream(os);
	    	
	    	//Send the parameters to the dGrepServer
	    	dos.writeInt(param.size());
	    	for(String str : param){
	    		dos.writeUTF(str);
	    	}
	    	
	    	//Send the key and value regex to the dGrepServer
	    	dos.writeUTF(key);
	    	dos.writeUTF(value);
	    	
	    	//Get the size of the result
	    	int resultSize;
	    	resultSize = dis.readInt();
	    	
	    	//Read the DataInputStream that many times and get all the results
	    	for(int i=0;i<resultSize;i++){
	    		String str = dis.readUTF();
	    		
	    		if(test){
	    			resultList.add(str);
	    		} else {
	    			System.out.println(str);
	    		}
	    	}
	    	
	    	socket.close();
	    	
	    	//System.out.println(threadName + " has ended");
	    	
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println(threadName + " at port " + socket.getLocalPort() + 
	    			" connected to " + socket.getInetAddress() + " : " + socket.getPort()
	    			+ "has encountered IOException");
		}    	
    	
    }

}
