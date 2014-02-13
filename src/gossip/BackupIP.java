package gossip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashSet;

import common.Log;
import common.Parameters;

public class BackupIP {
	
	static Writer writer;
	static HashSet<String> ipSet;
	
	public static void Init() throws UnsupportedEncodingException, FileNotFoundException{
		
		if(Parameters.isContactIP){
			OutputStream out = new FileOutputStream(Parameters.backupIPFile,true);
			BackupIP.writer = new OutputStreamWriter(out, "UTF-8");
			
			BackupIP.ipSet = new HashSet<String>();
	
			File backupFile = new File(Parameters.backupIPFile);
			try {	
				BufferedReader br = new BufferedReader(new FileReader(backupFile));
				String str;
				while((str = br.readLine()) != null){
					ipSet.add(str);
				}
				br.close();
			} catch (FileNotFoundException e) {
				if(Parameters.isContactIP){
					Log.warn("Could not find backup IP file \"" + Parameters.backupIPFile +"\"");
				}
			} catch (IOException e) {
				if(Parameters.isContactIP){
					Log.warn("Could not read the backup IP file \"" + Parameters.backupIPFile +"\"");
				}
			}
		}
	}
		
	private static void writeIP(String str){
		synchronized(BackupIP.writer){
			try {
				BackupIP.writer.write(str+"\n");
				BackupIP.writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void addIP(String str){
		if(ipSet.add(str)){
			writeIP(str);
		}
	}
	
	public static HashSet<String> getBackupIP(){
		return ipSet;
	}

}	
