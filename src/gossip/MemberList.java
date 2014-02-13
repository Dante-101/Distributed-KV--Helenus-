package gossip;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import kv.Hash;
import kv.HashRange;
import kv.SyncData;
import common.Log;
import common.Parameters;

public class MemberList {
	
	private List<Entry> members;
	//AtomicBoolean hasStabilised = new AtomicBoolean();
	
	
	public MemberList(){
		this.members = new LinkedList<Entry>();
		//hasStabilised.set(false);
	}
		
	public MemberList(LinkedList<Entry> members){
		this.members = members;
	}
	
	public List<Entry> getMembers() {
		return members;
	}

	public void setMembers(LinkedList<Entry> members) {
		synchronized (this.members) {
			this.members = members;
		}
	}
	
	public void MergeNewList(MemberList newMemberList) {
		List<Entry> newList = newMemberList.getMembers();
		
		boolean newEntry;
		boolean correctHash = false;
		
		for (Entry newListEntry : newList){
			MemberInfo newInfo = newListEntry.getMemberInfo();
			//Log.debug("merging received id " + newIdhb.getId().getString());
			
			if(newInfo == null){
				continue;
			}
			
			//To ensure that we don't take our own Id into consideration if own is true
			if(newInfo.getId().equals(Gossip.ownInfo.getId())){
				continue;
			}
			
			synchronized(members){
				newEntry = true;
				for(Entry entry : members){
					MemberInfo info = entry.getMemberInfo();
					if((info.getId()).equals((Id)newInfo.getId())){
						newEntry = false;
						if(info.getHeartBeat() < newInfo.getHeartBeat()){
							info.setHeartBeat(newInfo.getHeartBeat());
							if(entry.isFailed()){
								entry.setFailed(false);
								correctHash = true;
								//printMemberList();
							}
							entry.setTime(System.currentTimeMillis());
							//Log.debug("Updated the heartbeat: " + MySerializer.serialize(entry));
						}
						
						if(newInfo.getHeartBeat() == Long.MAX_VALUE){
							entry.setFailed(true);
							info.setHeartBeat(newInfo.getHeartBeat());
							String str = entry.memberInfo.getId().getString();
							Log.leave("Node "+ str + " is leaving at " + System.currentTimeMillis());
							correctHash = true;
						}
						
						break;
					}
				}
				
				if(newEntry){
					Entry e = new Entry(newInfo, System.currentTimeMillis());
					members.add(e);
					if(Parameters.isContactIP){
						BackupIP.addIP(newInfo.getId().getIPAdrress());
					}
					Log.join("added " + newInfo.getId().getString() + 
							" to the system at " + System.currentTimeMillis());
					//getMemberList();
					correctHash = true;
				}
			}
		}
		if(failCleanUp()){
			correctHash = true;
		};
		
		if(correctHash){
			correctHashRanges();
		}
	}
	
	public void correctHashRanges(){
		List<String> idHash = new ArrayList<String>();
		idHash.add(Gossip.ownInfo.getHash());
		String successorHash = "";
		
		synchronized (members) {
			if(members.size() == 0){
				Gossip.sucessorInfo = null;
			} else {
				for(Entry e : members){
					if(!e.isFailed()){
						idHash.add(e.getMemberInfo().getHash());
					}
				}
				
				Collections.sort(idHash);
				
				for(int i=0; i<idHash.size(); i++){
					MemberInfo memberInfoToChange = null;
					if(idHash.get(i).equals(Gossip.ownInfo.getHash())){
						memberInfoToChange = Gossip.ownInfo;
						if(i<idHash.size()-1){
							successorHash = idHash.get(i+1);
						} else {
							successorHash = idHash.get(0);
						}
					} else {
						for(Entry e : members){
							if(idHash.get(i).equals(e.getMemberInfo().getHash())){
								memberInfoToChange = e.getMemberInfo();
							}
						}
					}
					
					List<HashRange> hr = memberInfoToChange.getRangeList(); 
					hr.clear();
					
					if(idHash.size() <= Parameters.numReplica){
						hr.add(new HashRange(Hash.zero, Hash.max));
					} else {
						if(i < Parameters.numReplica){
							hr.add(new HashRange(Hash.zero, idHash.get(i)));
							hr.add(new HashRange(idHash.get(idHash.size()-(Parameters.numReplica-i)), Hash.max));
						} else {
							hr.add(new HashRange(idHash.get(i-Parameters.numReplica), idHash.get(i)));
						}
					}
				}
				
				if(Gossip.sucessorInfo == null || !successorHash.equals(Gossip.sucessorInfo.getHash())){
					for(Entry e : members){
						if(successorHash.equals(e.getMemberInfo().getHash())){
							Gossip.sucessorInfo = e.getMemberInfo();
							break;
						}
					}
				}
			}
		}
		
		synchronized(SyncData.notifySync){
			SyncData.notifySync.notifyAll();
		}
	}
	
	//Return true if an entry has failed else false
	public boolean failCleanUp(){
		//boolean memberPrint;
		boolean correctHash = false;
		synchronized(members){
			int failTime = Parameters.getTimeout();
			int cleanTime = Parameters.getCleanup();
			Iterator<Entry> ite = members.iterator();
			while(ite.hasNext()){
				Entry entry = ite.next();
				int failCleanStatus = entry.failCleanUP(failTime, cleanTime);
				
				if(failCleanStatus == 1){
					correctHash = true;
				}
				
				if( failCleanStatus == 2){
					String str = entry.getMemberInfo().getId().getString();
					Log.clean(str + " has been removed from the list at " + System.currentTimeMillis());
					ite.remove();
				}
			}
		}
		return correctHash;
	}
	
	public String getMemberInfo(){
		String JsonMemberString = "";
		synchronized (Gossip.ownInfo) {
			JsonMemberString += MySerializer.serialize(new Entry(Gossip.ownInfo)) + "\n";
		}
			
		synchronized (members) {
			for(Entry e : members){
				JsonMemberString += MySerializer.serialize(e) + "\n";
			}
		}
		return JsonMemberString;
	}
	
	public String getMemberIds(){
		String JsonMemberString = "";
		synchronized (Gossip.ownInfo) {
			JsonMemberString += MySerializer.serialize(Gossip.ownInfo.getId()) + "\n";
		}
			
		synchronized (members) {
			for(Entry e : members){
				JsonMemberString += MySerializer.serialize(e.getMemberInfo().getId()) + "\n";
			}
		}
		return JsonMemberString;
	}
	
	public List<String> getMemberIPs(){
		List<String> ipList = new ArrayList<String>();
		synchronized (members) {
			for(Entry e : members){
				ipList.add(e.memberInfo.getId().getIPAdrress());
			}
		}
		return ipList;
	}
}
