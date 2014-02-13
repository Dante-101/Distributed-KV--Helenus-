package kv;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class KVServerThread implements Runnable {
	
	Socket clientSocket = null;
	KeyValue kv;
	
	public KVServerThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.kv = new KeyValue();
	}
	
	public void run() {
		
		try {
			InputStream is = clientSocket.getInputStream();
			OutputStream os = clientSocket.getOutputStream();
		
			DataInputStream dis = new DataInputStream(is);
			DataOutputStream dos = new DataOutputStream(os);
			
			int cmd = dis.readInt();
			int qrm = dis.readInt();
			
			qrm = KVServer.getQuorumNumber(qrm);
			
			switch (cmd){
			case KVServer.CMDINSERT :
				kv.setKey(dis.readUTF());
				Value<String> v = new Value<String>(dis.readUTF());
				if(qrm == KVServer.QUORUMDISABLE){
					kv.setValue(v);
				} else {
					kv.setValue(Value.setTimeStamp(v));
				}
				Insert ins = new Insert(kv,qrm);
				ins.insertKeyValue();
				KVServer.writeOperation(kv,"Insert");
				dos.writeBoolean(ins.isKeyPresent());
				dos.writeBoolean(ins.isSuccessful());
				break;
			case KVServer.CMDLOOKUP :
				kv.setKey(dis.readUTF());
				LookUp lu = new LookUp(kv,qrm);
				lu.getValue();
				dos.writeBoolean(lu.isKeyPresent());
				if(lu.isKeyPresent()){
					if(qrm == KVServer.QUORUMDISABLE){
						dos.writeUTF(kv.getValue().get());
					} else {
						dos.writeUTF(Value.getData(kv.getValue()));
					}
				}
				KVServer.readOperation(kv);
				break;
			case KVServer.CMDDELETE :
				kv.setKey(dis.readUTF());
				Delete del = new Delete(kv,qrm);
				del.delete();
				KVServer.writeOperation(kv,"Delete");
				dos.writeBoolean(del.isKeyPresent());
				dos.writeBoolean(del.isSuccessful());
				break;
			case KVServer.CMDUPDATE :
				kv.setKey(dis.readUTF());
				Value<String> v1 = new Value<String>(dis.readUTF());
				if(qrm == KVServer.QUORUMDISABLE){
					kv.setValue(v1);
				} else {
					kv.setValue(Value.setTimeStamp(v1));
				}
				Update up = new Update(kv,qrm);
				up.updateKeyValue();
				KVServer.writeOperation(kv,"Update");
				dos.writeBoolean(up.isKeyPresent());
				dos.writeBoolean(up.isSuccessful());
				break;
			case KVServer.CMDDATAGET:
				List <HashRange> hrList = new ArrayList<HashRange>();
				int numHashRange = dis.readInt();
				for(int i=0; i<numHashRange; i++){
					String startHash = dis.readUTF();
					String endHash = dis.readUTF();
					hrList.add(new HashRange(startHash, endHash));
				}
				
				List<KeyValue> kvList = new ArrayList<KeyValue>();
				KVServer.getKeyValueListFromRangeList(hrList, kvList);
				
				dos.writeInt(kvList.size());
				for(KeyValue kv : kvList){
					KVServer.readOperation(kv);
					dos.writeUTF(kv.getKey());
					dos.writeUTF(kv.getValue().get());
				}
			}
			
			dos.close();
			dis.close();
			
			//clientSocket.close();	
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("KVServerThread at port " + clientSocket.getLocalPort() + 
					" connected to " + clientSocket.getInetAddress() + " : " + clientSocket.getPort()
					+ "has encountered IOException");
		}
		//System.out.println(threadName + " has ended");
	}

}

