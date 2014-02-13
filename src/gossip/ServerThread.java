package gossip;

public class ServerThread implements Runnable {
	
	MemberList memberList;
	
	public ServerThread(MemberList memberList) {
		this.memberList = memberList;
	}
	
	public void run(){
		//periodically gossip
		(new Thread(new SenderThread(memberList))).start();
		
		//accepts gossip messages and processes them
		(new Thread(new ListenerThread(memberList))).start();
	}
}
