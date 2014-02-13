package kv;

public class HashRange {
	
	//Start hash value is exclusive of the range
	String startHash;
	//End hash value is inclusive of the range
	String endHash;
	
	public HashRange(String startHash, String endHash) {
		super();
		this.startHash = startHash;
		this.endHash = endHash;
	}

	public String getStartHash() {
		return startHash;
	}

	public void setStartHash(String startHash) {
		this.startHash = startHash;
	}

	public String getEndHash() {
		return endHash;
	}

	public void setEndHash(String endHash) {
		this.endHash = endHash;
	}
	
	public boolean isInRange(String key){
		String keyHash = Hash.getHash(key);
		//System.out.println("KeyHash - " + key + ":" + keyHash);
		if((keyHash.compareTo(this.startHash) > 0) && (keyHash.compareTo(this.endHash) <= 0)){
			return true;
		}
		return false;
	}
	
}
