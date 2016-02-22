import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread{

	Message msg;
	Socket receiveSocket, sendSocket;
	ServerSocket serverSocket;
	public Node thisNode;
	public int ackNackCount = 0;
	ServerThread(Node thisNode){
		this.thisNode = thisNode;
		msg = new Message();
		receiveSocket = new Socket();
	}
	public void run(){
		try {
			serverSocket = new ServerSocket(thisNode.getPort());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true){
			
			//writing node's parent and child information to a file after algorithm finishes
			if(ackNackCount >= thisNode.getNeighbours().size()){
				//write to file and exit
				File file = new File(thisNode.getNodeId()+"output.txt");
				
				try {
					FileWriter fileWriter = new FileWriter(file);
					fileWriter.write("me: "+thisNode.getNodeId()+"\n");
					if(thisNode.getParent() != null){
						fileWriter.write("parent: "+thisNode.getParent().getNodeId()+"\n");
					}else {
						fileWriter.write("parent: *");
					}
					
					if(thisNode.getChildren().size() == 0){
						fileWriter.write("children: *");
					}else {
						fileWriter.write("children: ");
						
						for (Node node: thisNode.getChildren()){
							fileWriter.write(node.getNodeId()+" ");
						}
					}
					
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			/////////////////////////////////////////////////////////////////////////////////
			
			try{
				receiveSocket = serverSocket.accept();
				
			//control comes here whenever a new client is connected to the server
			//receiveSocket.getLocalAddress();		
			
			ObjectInputStream ois = new ObjectInputStream(receiveSocket.getInputStream());
			
			msg = (Message)ois.readObject();
			
				if(msg.getMsgType().equals("Find")){	//find msg
					if(thisNode.isRoot() || (thisNode.getParent() != null)){
						//send nack
						Message nackMsg = new Message();
						nackMsg.setSender(thisNode);
						nackMsg.setMsgType("Nack");
						
						sendSocket = new Socket(msg.getSender().getHostName(), msg.getSender().getPort());
						ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
						
						//get the node's details who sent this find message
						//and now send this nack message to that host's server
						oos.writeObject(nackMsg);
						sendSocket.close();
						
					}else {
						
						
						//update the node itself first
						thisNode.setParent(msg.getSender());
						
						//send ack
						Message ackMsg = new Message();
						ackMsg.setSender(thisNode);
						ackMsg.setMsgType("Ack");
						try{
						sendSocket = new Socket(msg.getSender().getHostName(), msg.getSender().getPort());
					}catch(ConnectException e){
						System.out.println("["+ thisNode.getNodeId()+"]"+ "connect exception when try to connect to"+msg.getSender().getNodeId());
					}
						ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
						
						//get the node's details who sent this find message
						//and now send this nack message to that host's server
						oos.writeObject(ackMsg);
						
						sendSocket.close();
						//send find message to the neighbors
						for(Node node:thisNode.getNeighbours()){
							//Socket findSocket = new Socket(node.getHostName(), node.getPort());
							
							Message findMsg = new Message();
							findMsg.setSender(thisNode);
							findMsg.setMsgType("Find");
							
							Socket findSendSocket = new Socket(node.getHostName(), node.getPort());
							ObjectOutputStream findOos = new ObjectOutputStream(findSendSocket.getOutputStream());
							
							findOos.writeObject(findMsg);
							
							findSendSocket.close();
						}
					}
				}
				
				if(msg.getMsgType().equals("Ack")){
					
					//creating child
					Node child = msg.getSender();
					//add a child to this node
					thisNode.getChildren().add(child);
					
					//increment ack/nack count
					ackNackCount++;
				}
				
				if(msg.getMsgType().equals("Nack")){
					//do noting
					ackNackCount++;
				}
				
				}catch(IOException | ClassNotFoundException e){
					e.printStackTrace();
				}
			finally {
				try {
					receiveSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
}
