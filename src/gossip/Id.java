package gossip;

public class Id {
	String ipadrress;
	long timestamp;
	
	public String getIPAdrress() {
		return ipadrress;
	}

	void setIPAdrress(String ipadrress) {
		this.ipadrress = ipadrress;
	}

	public long getTimestamp() {
		return timestamp;
	}

	void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Id(String ipaddress, long timestamp){
		this.ipadrress = ipaddress;
		this.timestamp = timestamp;
	}
	
	public String getString(){
		String s = this.ipadrress + "|" + this.timestamp; 
		return s;
	}
	
	public boolean equals(Id id) {
		if(this.ipadrress.equals(id.getIPAdrress())){
			if(this.timestamp == id.getTimestamp()){
				return true;
			}
		}
		return false;
	}
}
