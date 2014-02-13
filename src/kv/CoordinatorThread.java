package kv;

import gossip.Gossip;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import common.Log;
import common.Parameters;

public class CoordinatorThread implements Runnable{
	
	int cmd;
	int quorum = KVServer.QUORUMDISABLE;;
	String ipRM;
	KeyValue kv;
	AtomicInteger numKeyPresent;
	AtomicInteger numSuccessful;
	AtomicInteger quorumCompleted;
	//It is used to block on and notify the parent thread in operations except lookup.
	//In lookup, the obj is a hashmap to store the values
	//;
	Object lockObj;
	boolean keyPresent = false;
	boolean successful = false;
	boolean quorumDone = false;
	
	
	public CoordinatorThread(int cmd, String ipRM, KeyValue kv,AtomicInteger quorumCompleted,
			AtomicInteger numkeyPresent, AtomicInteger numSuccessful, Object obj) {
		super();
		this.cmd = cmd;
		this.ipRM = ipRM;
		this.kv = kv;
		this.quorumCompleted = quorumCompleted;
		this.numKeyPresent = numkeyPresent;
		this.numSuccessful = numSuccessful;
		this.lockObj = obj;
	}
	
	public CoordinatorThread(int cmd, String ipRM, KeyValue kv) {
		super();
		this.cmd = cmd;
		this.ipRM = ipRM;
		this.kv = kv;
		this.numKeyPresent = null;
		this.numSuccessful = null;
		this.quorumCompleted = null;
		this.lockObj = null;
	}
	
	public CoordinatorThread(int cmd, String ipRM) {
		super();
		this.cmd = cmd;
		this.ipRM = ipRM;
		this.kv = null;
		this.numKeyPresent = null;
		this.numSuccessful = null;
		this.quorumCompleted = null;
		this.lockObj = null;
	}
	
	public void run(){
		
		Socket clientSocket = null;
		
		try {
			clientSocket = new Socket(ipRM,Parameters.kvListenerPort);
			//System.out.println("Connecting to IP " + ipRM);
			InputStream is = clientSocket.getInputStream();
			OutputStream os = clientSocket.getOutputStream();
			DataInputStream dis = new DataInputStream(is);
			DataOutputStream dos = new DataOutputStream(os);
			
			dos.writeInt(cmd);
			dos.writeInt(quorum);
			
			switch(cmd){
			case KVServer.CMDLOOKUP :
				if(this.lockObj instanceof HashMap){
					HashMap<String,Value<String>> valueMap = (HashMap<String,Value<String>>) lockObj;
					dos.writeUTF(kv.getKey());
					this.keyPresent = dis.readBoolean();
					if(this.keyPresent){
						numKeyPresent.incrementAndGet();
						String result = dis.readUTF();
						synchronized(valueMap){
							valueMap.put(ipRM, new Value<String>(result));
						}
						if(result != null && result != ""){
							numSuccessful.incrementAndGet();
						}
					}
				}
				quorumCompleted.incrementAndGet();
				quorumDone = true;
				synchronized(lockObj){
					lockObj.notifyAll();
				}
				break;
			case KVServer.CMDINSERT :
				dos.writeUTF(kv.getKey());
				dos.writeUTF(kv.getValue().get());
				this.keyPresent = dis.readBoolean();
				this.successful = dis.readBoolean();
				if(lockObj == null){
					break;
				}
				if(this.keyPresent)	numKeyPresent.incrementAndGet();
				if(this.successful) numSuccessful.incrementAndGet();
				quorumCompleted.incrementAndGet();
				quorumDone = true;
				synchronized(lockObj){
					lockObj.notifyAll();
				}
				break;
				
			case KVServer.CMDDELETE :
				dos.writeUTF(kv.getKey());
				this.keyPresent = dis.readBoolean();
				this.successful = dis.readBoolean();
				if(this.keyPresent)	numKeyPresent.incrementAndGet();
				if(this.successful) numSuccessful.incrementAndGet();
				quorumCompleted.incrementAndGet();
				quorumDone = true;
				synchronized(lockObj){
					lockObj.notifyAll();
				}
				break;
				
			case KVServer.CMDUPDATE :
				dos.writeUTF(kv.getKey());
				dos.writeUTF(kv.getValue().get());
				this.keyPresent = dis.readBoolean();
				this.successful = dis.readBoolean();
				if(this.keyPresent)	numKeyPresent.incrementAndGet();
				if(this.successful) numSuccessful.incrementAndGet();
				quorumCompleted.incrementAndGet();
				quorumDone = true;
				synchronized(lockObj){
					lockObj.notifyAll();
				}
				break;
				
			case KVServer.CMDDATAGET:
				//System.out.println("Coordinator contacting " + ipRM);
				synchronized (Gossip.ownInfo) {
					List<HashRange> ownHrList = Gossip.ownInfo.getRangeList();
					int numHashRange = ownHrList.size();
					dos.writeInt(numHashRange);
					for(int i=0; i<numHashRange; i++){
						dos.writeUTF(ownHrList.get(i).getStartHash());
						dos.writeUTF(ownHrList.get(i).getEndHash());
					}
				}
				
				List<KeyValue> kvList = new ArrayList<KeyValue>();
				int size = dis.readInt();
				for(int i=0; i<size; i++ ){
					KeyValue kv = new KeyValue(); 
					kv.setKey(dis.readUTF());
					kv.setValue(new Value<String>(dis.readUTF()));
					kvList.add(kv);
				}
				
				HashMap<String, Value<String>> kvData = KVServer.kvData;
				
				synchronized (kvData) {
					for(KeyValue kv: kvList){
						if(kvData.containsKey(kv.getKey())){
							Value<String> v = kvData.get(kv.getKey());
							if(Value.getTimeStamp(v) < Value.getTimeStamp(kv.getValue())){
								kvData.put(kv.getKey(), kv.getValue());
							}
						} else {
							kvData.put(kv.getKey(), kv.getValue());
						}
					}
				}
				//System.out.println("Finished coordinator thread to contact " + ipRM);
				break;
				
			}
			dos.close();
			dis.close();
		} catch (IOException e) {
			String str = "Cannot connect to " + ipRM + " : " + Parameters.kvListenerPort;
			Log.warn(str);
		}
		
		if(!quorumDone && quorumCompleted !=  null){
			quorumCompleted.incrementAndGet();
		}
	}
}
