package kv;

import gossip.Gossip;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import common.Parameters;

public class Start {
	
	public static void main(String args[]) throws UnsupportedEncodingException, FileNotFoundException{
		
		Gossip.init();
		new Thread(new SyncData()).start();
		KVServer kVServer = new KVServer(Parameters.kvListenerPort);
		new Thread(kVServer).start();
		
		CommandInput.start();
	}
}
