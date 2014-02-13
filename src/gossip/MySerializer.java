package gossip;

import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import common.Log;
import common.Parameters;

public class MySerializer {
	
	static Gson gson = new Gson();
	static int numEntries = Parameters.numPacketEntries;
	
	//own is to decide if we want to serialize our own id or not
	static public String[] serialize(MemberList memberList) {
		List<Entry> entries = memberList.getMembers();
		String self = "";
		synchronized (Gossip.ownInfo) {
			Gossip.ownInfo.incHeartBeat();
			self = gson.toJson(Gossip.ownInfo, MemberInfo.class) + ";";
		}
		int numPackets;
		int index = 0;
		int ct = 0;
		String[] packetArr;
		synchronized (entries) {
			numPackets = (int)(entries.size()/numEntries) + 1;
			packetArr = new String[numPackets];
			packetArr[index] = self;
			++ct;
			
			int failTime = Parameters.getTimeout();
			int cleanTime = Parameters.getCleanup();
			Iterator<Entry> ite = memberList.getMembers().iterator();
			while(ite.hasNext()){
				Entry e = ite.next();
				/*
				int failCleanStatus = e.failCleanUP(failTime, cleanTime);
				if( failCleanStatus == 2){
					String str = e.getMemberInfo().getId().getString();
					Log.clean(str + " has been removed from the list at " + System.currentTimeMillis());
					ite.remove();
					continue;
				}
				*/
				
				if(ct >= numEntries){
					++index;
					packetArr[index] = "";
					ct = 0;
				}
				
				if(!e.isFailed()){
					packetArr[index] += gson.toJson(e.getMemberInfo(), MemberInfo.class) + ";";
					++ct;
				}
			}
		}
		return packetArr;
	}
	
	//own is to decide if we want to filter our own id or not
	static public MemberList deserialize(String entries) {
		String[] parts= entries.split(";");
		
		MemberList memberList = new MemberList();
		List<Entry> entryList = memberList.getMembers();
		//Id ownId = Gossip.getOwnIdHeartBeat().getId();
		
		for(String p:parts){
			//Log.debug("deserializing : " + p );
			try{
				MemberInfo info = gson.fromJson(p, MemberInfo.class);
				Entry e = new Entry(info);
				//Log.debug("deserialized : " + serialize(e));
				entryList.add(e);
			}catch(JsonSyntaxException e){
				System.out.println(p);
				System.exit(0);
			}
		}
		
		return memberList;
	}
	
	static public String serialize(Entry e){
		synchronized (e) {
			return gson.toJson(e, Entry.class) + ";";
		}
	}
	
	static public String serialize(Id id){
		synchronized (id) {
			return gson.toJson(id, Id.class) + ";";
		}
	}
}