package kv;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import common.Parameters;

public class GetFile {

	static int cmd = KVServer.CMDLOOKUP;
	static int qrm = KVServer.QUORUMQ;
	static String serverIP;
	static KeyValue kv;
	static int serverId = 0;
	
	public static void main(String[] args) {
		
		getInput(args);

		try{
			
			Socket clientSocket = new Socket(GetFile.serverIP,Parameters.kvListenerPort);
		
			InputStream is = clientSocket.getInputStream();
			OutputStream os = clientSocket.getOutputStream();
	
			DataInputStream dis = new DataInputStream(is);
			DataOutputStream dos = new DataOutputStream(os);
			
			dos.writeInt(cmd);
			dos.writeInt(qrm);
			
			boolean keyPresent = false;
			
			dos.writeUTF(kv.getKey());
			keyPresent = dis.readBoolean();
			StringBuilder lookInfo = new StringBuilder();
			if(keyPresent){
				String result = dis.readUTF();
				kv.setValue(new Value<String>(result));
				lookInfo.append("Files containing the keyword:\n");
				String[] arrFileName = result.split(",");
				for(String str : arrFileName){
					lookInfo.append(str).append("\n");
				}
			} else {
				lookInfo.append("No files were found for the keyword " + kv.getKey());
			}
			
			System.out.println(lookInfo.toString());
			
			clientSocket.close();
		} catch (IOException e) {
			System.out.println("GetFile connected to " + GetFile.serverIP + " at " + 
					Parameters.kvListenerPort + "has encountered IOException");
			e.printStackTrace();
		}
	}
	
	
	static void getInput(String[] args){
		
		GetFile.kv = new KeyValue();
		int keyNum = 0;
		
		if(args.length > keyNum){
			
			List<String> ipList = new ArrayList<String>();
			dGrepClient.Client.getIPAddresses(ipList);
			
			if(serverId < ipList.size()){
				GetFile.serverIP = ipList.get(serverId);
			} else {
				System.out.println("Invalid Server");
				System.exit(0);
			}
			kv.setKey(args[keyNum]);
			
		} else {
			System.out.println("Invalid arguments. Please provide arguments as: <key>");
			System.exit(0);
		}
	}
}
