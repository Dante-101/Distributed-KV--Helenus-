package kv;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import common.Log;
import common.Parameters;

public class Hash {
	
	public static final String zero = getZero();
	public static final String max = getMax();
	
	public static String getHash(String input){
		
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance(Parameters.hashAlgo);
		}catch(NoSuchAlgorithmException e){
			String error = "Invalid hash algorithm " + Parameters.hashAlgo;
			Log.fatal(error);
			System.exit(0);
		}
		
		if(md.getDigestLength() < Parameters.hashSize){
			String error = "Invalid hash algorithm " + Parameters.hashAlgo + 
					" for the selected hash size of " + Parameters.hashSize;
			Log.fatal(error);
			System.exit(0);
		}
		
		byte[] bytesOfMessage = input.getBytes();
		byte[] digest = md.digest(bytesOfMessage);
		
		byte[] output = new byte[Parameters.hashSize];
		for(int i=0; i<output.length; i++){
			output[i] = digest[i];
		}
		
		String result = bytesToHex(output);
		if(result.equals(Hash.zero) || result.equals(Hash.max)){
			Log.fatal("Hash has come out to be zero or max value. The program does not handle "
					+ "these conditions. Aborting.");
			System.exit(0);
		}
		return result;
			 
	}
	
	private final static char[] hexArray = "0123456789abcdef".toCharArray();
	
	private static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	private static String getZero(){
		String result = "";
		for(int i=0; i<Parameters.hashSize*2 ; i++){
			result += "0";
		}
		return result;
	}
	
	private static String getMax(){
		String result = "";
		for(int i=0; i<Parameters.hashSize*2 ; i++){
			result += "f";
		}
		return result;
	}

}
