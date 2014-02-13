package dGrepServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import common.Parameters;

/**
 * This class contains the main program which starts a dGrepServer thread on each node.
 * The thread waits for a connection and the moment it gets a connection, it creates another thread which
 * handles the data transfer and requests to and from the dGrepClient.   
 * 
 * @author Gaurav, Tanvi
 *
 */
public class Server implements Runnable{

	//Port to start the dGrepServer on
    int serverPort;
    ServerSocket serverSocket = null;
    
    //The log file to grep
    static String logFileName;

    public Server(int port){
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
                throw new RuntimeException("Error accepting dGrepClient connection", e);
            }
            //Starts a new thread from the class ServerThread which handles rest of the processing
            new Thread( new ServerThread(clientSocket,logFileName)).start();
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
    
    public static void main(String[] args) {
    	
		logFileName = common.Parameters.logFile;
		
		Server server = new Server(Parameters.grepSeverPort);
		new Thread(server).start();	
	}

}