package gossip;

import common.Log;

public class Entry {
	MemberInfo memberInfo;
	Long time;
	Boolean failed;
	
	public MemberInfo getMemberInfo() {
		return memberInfo;
	}

	public void setMemberInfo(MemberInfo memberInfo) {
		this.memberInfo = memberInfo;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}
	
	long getTime() {
		return time;
	}

	void setTime(long time) {
		this.time = time;
	}

	public Entry(MemberInfo info , long time) {
		this.memberInfo = info;
		this.time = time;
		this.failed = false;
	}
	
	public Entry(MemberInfo info) {
		this.memberInfo = info;
		this.failed = false;
		this.time = null;
	}
	
	//returns 2 if the items needs to be cleaned up, 1 if the item has just failed
	public int failCleanUP(int failTime, int cleanTime){
		long sysTime = System.currentTimeMillis();
		long eTime = this.time;
		
		if((sysTime - eTime) > failTime){
			if(!this.failed){
				this.failed = true;
				String str = this.memberInfo.getId().getString();
				Log.fail("failure detected for" + str + " at " + System.currentTimeMillis());
				return 1;
			}
			
			if((sysTime - eTime) > cleanTime){
				return 2;
			}
		}
		return 0;

	}
}
