package kv;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import common.Parameters;


public class Client {
	
	static List<KeyValue> kvList = new ArrayList<KeyValue>();
	static int cmd;
	static int qrm;
	static String serverIP;
	
	public static void main(String[] args) {
		
		getInput(args);
		
		long startTime = System.nanoTime()/1000;

		for(KeyValue kv : kvList){
			try{
				
				Socket clientSocket = new Socket(serverIP,Parameters.kvListenerPort);
			
				InputStream is = clientSocket.getInputStream();
				OutputStream os = clientSocket.getOutputStream();
		
				DataInputStream dis = new DataInputStream(is);
				DataOutputStream dos = new DataOutputStream(os);
				
				dos.writeInt(cmd);
				dos.writeInt(qrm);
				
				boolean keyPresent = false;
				boolean successful = false;
				
				switch (cmd){
				case KVServer.CMDINSERT :
					dos.writeUTF(kv.getKey());
					dos.writeUTF(kv.getValue().get());
					keyPresent = dis.readBoolean();
					successful = dis.readBoolean();
					String insInfo;
					/*
					if(keyPresent){
						insInfo = "The key to be inserted " + kv.getKey() + " is already present. Updated it "
								+ "with the value " + kv.getValue().get();
					} else {
						insInfo = "Inserted key " + kv.getKey() + " with value " + kv.getValue().get();
					}
					*/
					
					if(successful){
						insInfo = "Inserted key " + kv.getKey() + " with value " + kv.getValue().get();
						System.out.println(insInfo);						
					}else {
						String warn = "Insertion of the key " + kv.getKey() + "with value" + 
								kv.getValue().get() + "was not successful"; 
						System.out.println(warn);
					}
					break;
				case KVServer.CMDLOOKUP :
					dos.writeUTF(kv.getKey());
					keyPresent = dis.readBoolean();
					String lookInfo;
					if(keyPresent){
						String result = dis.readUTF();
						kv.setValue(new Value<String>(result));
						lookInfo = "Key " + kv.getKey() + " found with value " + kv.getValue().get();
					} else {
						lookInfo = "The key " + kv.getKey() + " was not found";
					}
					
					System.out.println(lookInfo);
					break;
				case KVServer.CMDDELETE :
					dos.writeUTF(kv.getKey());
					keyPresent = dis.readBoolean();
					successful = dis.readBoolean();
					String delInfo;
					if(successful){
						delInfo = "The key " + kv.getKey() + " has been deleted";
					} else {
						delInfo = "The key " + kv.getKey() + " was not found";
					}
					System.out.println(delInfo);
					break;
					
				case KVServer.CMDUPDATE :
					dos.writeUTF(kv.getKey());
					dos.writeUTF(kv.getValue().get());
					keyPresent = dis.readBoolean();
					successful = dis.readBoolean();
					String upInfo;
					if(!keyPresent){
						upInfo = "The key " + kv.getKey() + " is not present"; 
					} else {
						upInfo = "Updated key " + kv.getKey() + " with value " + 
								kv.getValue().get(); 
					}
					
					System.out.println(upInfo);
					
					if(!successful){
						String warn = "Updation of the key " + kv.getKey() + " with value " + 
								kv.getValue().get() + "was not successful"; 
						System.out.println(warn);
					}
					
					break;
				default: System.out.println("INVALID PROGRAMMING!!!");
				}
				
				clientSocket.close();
			} catch (IOException e) {
				System.out.println("Client connected to " + Client.serverIP + " at " + 
						Parameters.kvListenerPort + "has encountered IOException");
				e.printStackTrace();
			}
		}
		
		long endTime = System.nanoTime()/1000;
		
		double timeTaken = ((double)endTime-(double)startTime)/1000;
		
		System.out.println("Time taken: " + String.format("%.2f", timeTaken) + " ms");
	}
	
	
	static void getInput(String[] args){
		boolean INVALID_INPUT = false;
		
		KeyValue kv = new KeyValue();
		int serverNum = 0;
		int qrmNum = 1;
		int cmdNum = 2;
		int keyNum = 3;
		int valueNum = 4;
		
		if(args.length > keyNum){
			
			int serverId = Integer.parseInt(args[serverNum]);
			List<String> ipList = new ArrayList<String>();
			dGrepClient.Client.getIPAddresses(ipList);
			
			if(serverId < ipList.size()){
				Client.serverIP = ipList.get(serverId);
			} else {
				System.out.println("Invalid server id");
				INVALID_INPUT = true;
			}
		
			String strQrm = args[qrmNum];
			if(strQrm.equalsIgnoreCase("one")){
				Client.qrm = KVServer.QUORUMONE;
			} else 
				if(strQrm.equalsIgnoreCase("quorum")){
					Client.qrm = KVServer.QUORUMQ;
			} else
				if(strQrm.equalsIgnoreCase("all")){
					Client.qrm = KVServer.QUORUMALL;
			} else {
				INVALID_INPUT = true;
			}

			String strCmd = args[cmdNum];
			Client.cmd = KVServer.CMDINVALID;
			
			if(strCmd.equals("lookup")){
				Client.cmd = KVServer.CMDLOOKUP;
				kv.setKey(args[keyNum]);
				kvList.add(kv);
			} else 
				if(strCmd.equals("insert")){
				Client.cmd = KVServer.CMDINSERT;
				kv.setKey(args[keyNum]);
				if(args.length > valueNum){
					kv.setValue(new Value<String>(args[valueNum]));
					kvList.add(kv);
				} else {
					INVALID_INPUT = true;
				}
			} else 
				if(strCmd.equals("delete")){
				Client.cmd = KVServer.CMDDELETE;
				kv.setKey(args[keyNum]);
				kvList.add(kv);
			} else 
				if(strCmd.equals("update")){
				Client.cmd = KVServer.CMDUPDATE;
				kv.setKey(args[keyNum]);
				if(args.length > valueNum){
					kv.setValue(new Value<String>(args[valueNum]));
					kvList.add(kv);
				} else {
					INVALID_INPUT = true;
				}
			} else 
				if(strCmd.equals("fileinsert")){
				Client.cmd = KVServer.CMDINSERT;
				String fileName = args[keyNum];
				getKVList(fileName);
			} else {
				INVALID_INPUT = true;
			}
			
		} else {
			INVALID_INPUT = true;
		}
		
		if(INVALID_INPUT){
			System.out.println("Invalid arguments. Please provide arguments as: <ServerID> <quorum> <command>"
					+ " <key> [value]");
			System.out.println("Server ID is the number of server in IP.txt file");
			System.out.println("Quorum can be 'one', 'quorum' or 'all'");
			System.out.println("Command can be 'insert', 'update', 'delete' or 'lookup'");
			System.out.println("Value needs to be provided for 'insert' and 'update'");
			System.exit(0);
		}
	}
	
	static void getKVList(String fileName){
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = br.readLine()) != null) {
				String[] kvStr = line.split(":");
				KeyValue kv = new KeyValue(kvStr[0]);
				kv.setValue(new Value<String>(kvStr[1]));
				kvList.add(kv);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
