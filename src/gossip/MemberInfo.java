package gossip;

import java.util.ArrayList;
import java.util.List;

import kv.Hash;
import kv.HashRange;

public class MemberInfo {
	Id id;
	long heartBeat;
	List<HashRange> rangeList;
	String hash;
	boolean hasData;
	
	public MemberInfo(Id id, long heartBeat) {
		super();
		this.id = id;
		this.heartBeat = heartBeat;
		this.hasData = false;
		this.rangeList = new ArrayList<HashRange>();
		this.hash = Hash.getHash(this.id.getString());
		
		rangeList.add(new HashRange(Hash.zero, this.hash));
		rangeList.add(new HashRange(this.hash, Hash.max));
		
	}
	
	public Id getId() {
		return id;
	}
	public void setId(Id id) {
		this.id = id;
	}
	public long getHeartBeat() {
		return heartBeat;
	}
	
	public void incHeartBeat() {
		if(this.heartBeat < Long.MAX_VALUE)
			++heartBeat;
	}
	
	public void setHeartBeat(long heartBeat) {
		this.heartBeat = heartBeat;
	}

	public boolean isHasData() {
		return hasData;
	}

	public void setHasData(boolean hasData) {
		this.hasData = hasData;
	}

	public List<HashRange> getRangeList() {
		return rangeList;
	}

	public void setRangeList(List<HashRange> rangeList) {
		this.rangeList = rangeList;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public boolean isInRange(String key){
		for(HashRange hr : rangeList){
			if(hr.isInRange(key)){
				return true;
			}
		}
		return false;
	}
	
	
}
