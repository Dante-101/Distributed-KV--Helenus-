package kv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import common.Log;
import common.Parameters;

public class KVServer implements Runnable{

	//Port to start the KVServer on
    int serverPort;
    ServerSocket serverSocket = null;
    final static int CMDINVALID = 0;
	final static int CMDLOOKUP = 1;
	final static int CMDINSERT = 2;
	final static int CMDUPDATE = 3;
	final static int CMDDELETE = 4;
	final static int CMDDATAGET = 5;
	
	final static int QUORUMONE = 1001;
	final static int QUORUMQ = 1002;
	final static int QUORUMALL = 1003;
	
	//To be used when the request is a part of Quorum and not be Client
	final static int QUORUMDISABLE = 1000;
	
	public static HashMap<String, Value<String>> kvData;
	
	public static LinkedList<String> lastRead = new LinkedList<String>();
	public static LinkedList<String> lastWrite = new LinkedList<String>();

    public KVServer(int port){
        this.serverPort = port;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run(){
        openServerSocket();
        while(true){
            Socket clientSocket = null;
            try {
            	// Whenever there is a connection
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException("Error accepting connection", e);
            }
            //Starts a new thread from the class ServerThread which handles rest of the processing
            new Thread(new KVServerThread(clientSocket)).start();
        }
    }

    /**
     * It starts a dGrepServer at the port specified by serverPort and references it to serverSocket
     */
    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + this.serverPort, e);
        }
    }
    
    public static void getKeyValueListFromRangeList(List<HashRange> rangeList, List<KeyValue> kvList){
    	synchronized (KVServer.kvData) {
			for(String k : KVServer.kvData.keySet()){
				for(HashRange hr : rangeList){
					if(hr.isInRange(k)){
						KeyValue kv = new KeyValue(k);
						kv.setValue(KVServer.kvData.get(k));
						kvList.add(kv);
					}
				}
			}
		}
    }
    
    public static int getQuorumNumber(int quorum){
    	
    	int num = 0;
    	switch(quorum){
    	case KVServer.QUORUMONE:	num = 1; 		break;
    	case KVServer.QUORUMQ:		num = Parameters.numQuorum; break;
    	case KVServer.QUORUMALL:	num = Parameters.numReplica; break;
    	case KVServer.QUORUMDISABLE: num = KVServer.QUORUMDISABLE; break;
    	default:
    		String str = "Invalid Quorum number in the program";
    		Log.fatal(str);
    		System.exit(0);
    	}
    	return num;
    	
    }
    
    public static void readOperation(KeyValue kv){
    	synchronized(KVServer.lastRead){
	    	if(!KVServer.lastRead.isEmpty() && KVServer.lastRead.size() >= 10 ){
				KVServer.lastRead.removeLast();
			}
	    	KVServer.lastRead.addFirst("Lookup - " + kv.getKey() + " : " + kv.getValue().get());
    	}
    }
    
    public static void writeOperation(KeyValue kv, String cmd){
    	synchronized(KVServer.lastWrite){
	    	if(!KVServer.lastWrite.isEmpty() && KVServer.lastWrite.size() >= 10 ){
				KVServer.lastWrite.removeLast();
			}
	    	KVServer.lastWrite.addFirst(cmd + " - " + kv.getKey() + " : " + kv.getValue().get());
    	}
    }
    
}
