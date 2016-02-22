

import java.util.ArrayList;
import java.util.List;

public class Node {
	private boolean isRoot;
	private Node parent;
	private List<Node> children;
	private String hostName;
	private int port;
	private int nodeId;
	private List<Node> neighbours;
	
	Node(){
		//parent = new Node();
		children = new ArrayList<Node>();
		neighbours = new ArrayList<Node>();
	}
	
	
	public int getNodeId() {
		return nodeId;
	}
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	public Node getParent() {
		return parent;
	}
	public void setParent(Node parent) {
		this.parent = parent;
	}
	public List<Node> getChildren() {
		return children;
	}
	public void setChildren(List<Node> children) {
		this.children = children;
	}
	
		public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	
	
	public boolean isRoot() {
		return isRoot;
	}
	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}
	public List<Node> getNeighbours() {
		return neighbours;
	}
	public void setNeighbours(List<Node> neighbours) {
		this.neighbours = neighbours;
	}

}
