package common;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class Log {
	
	static Writer writer;
	
	public static void Init() throws UnsupportedEncodingException, FileNotFoundException{
		OutputStream out = new FileOutputStream(Parameters.logFile);
		Log.writer = new OutputStreamWriter(out, "UTF-8");
	}
	
	public static void keyInsert(String str){
		write("keyinsert:"+str+"\n");
	}
	
	public static void keyLookUp(String str){
		write("keylookup:"+str+"\n");
	}
	
	public static void keyDelete(String str){
		write("keydelete:"+str+"\n");
	}

	public static void keyUpdate(String str){
		write("keyupdate:"+str+"\n");
	}
	
	public static void info(String str){
		write("info:"+str+"\n");
	}
	
	public static void memberList(String str){
		write("memberList:"+str+"\n");
	}
	
	public static void fatal(String str){
		System.out.println("FATAL : " + str+"\n");
		write("fatal:"+str+"\n");
	}
	
	public static void error(String str){
		write("error:"+str+"\n");
	}
	
	public static void warn(String str){
		write("warn:"+str+"\n");
	}
	
	public static void debug(String str){
		write("debug:"+str+"\n");
	}
	
	public static void sent(String str){
		write("sent:"+str+"\n");
	}
	
	public static void receive(String str){
		write("receive:"+str+"\n");
	}
	
	public static void join(String str){
		write("join:"+str+"\n");
	}
	
	public static void fail(String str){
		write("fail:"+str+"\n");
	}
	
	public static void clean(String str){
		write("clean:"+str+"\n");
	}
	
	public static void leave(String str){
		write("leave:"+str+"\n");
	}
	
	public static void bytesSent(String str){
		write("bytesSent:"+str+"\n");
	}
	
	private static void write(String str){
		//System.out.println(str);
		synchronized(Log.writer){
			try {
				Log.writer.write(str);
				Log.writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
