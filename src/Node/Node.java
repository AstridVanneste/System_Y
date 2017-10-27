package Node;

import Network.UDP.Multicast.*;


public class Node{
	private String ip;
	private String name;
	private String previousNeighbour;
	private String nextNeighbour;
	private Subscriber sub;

	public Node(String name){
		this.name = name;
		//this.ip = giveIp;
		sub = new Subscriber(ip, 5000);

	}

	public void setNeighbours(int amountNeighbours){
		if(amountNeighbours == 0){
			previousNeighbour = this.ip;
			nextNeighbour = this.ip;
		}


		if(amountNeighbours == 1){
			//getIP
			//previousNeighbour = nextNeighbour;
		}



		if(amountNeighbours == 2){
			//previousNeighbour =;
			//nextNeighbour=;
		}
	}

	public String getData(){




		return null;
	}

	public int getID(){
		return Math.abs(name.hashCode() % 32768);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPreviousNeighbour() {
		return previousNeighbour;
	}

	public void setPreviousNeighbour(String previousNeighbour) {
		this.previousNeighbour = previousNeighbour;
	}

	public String getNextNeighbour() {
		return nextNeighbour;
	}

	public void setNextNeighbour(String nextNeighbour) {
		this.nextNeighbour = nextNeighbour;
	}
}
