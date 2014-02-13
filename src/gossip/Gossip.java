package gossip;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import common.Log;
import common.Parameters;

public class Gossip {
	
	public static MemberInfo ownInfo;
	public static MemberInfo sucessorInfo;
	public static MemberList memberList;
	
	public static void init() throws UnsupportedEncodingException, FileNotFoundException{
		
		Log.Init();
		BackupIP.Init();
		
		//It will schedule threads from a pool to periodically update the heartbeat
		//ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1);
		//threadPool.scheduleAtFixedRate(new HeartBeatUpdaterThread(), 0, Parameters.heartBeatUpdatePeriod, TimeUnit.MILLISECONDS);
		
		
	}
	
	public static MemberInfo getNewInfo(){
		//get the IP
		String ip = Parameters.selfIP;
		//time at which the thread is created
		long timestamp = System.currentTimeMillis();
		//Set up the id of the server and add it to the member list
		
		Id id = new Id(ip, timestamp);
		return new MemberInfo(id, 0);
	}
}