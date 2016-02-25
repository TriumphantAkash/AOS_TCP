import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

public class ServerThread extends Thread{

//	boolean writeMode = true;
	Message msg;
	Socket receiveSocket, sendSocket;
	ServerSocket serverSocket;
	public Node thisNode;
	public int ackNackCount = 0;
	public static final int MESSAGE_SIZE = 500000;
	ByteBuffer byteBuffer;
	SctpChannel sctpChannel;
	SctpServerChannel sctpServerChannel;
	InetSocketAddress serverAddr;
	
	
	ServerThread(Node thisNode){
		this.thisNode = thisNode;
		msg = new Message();
		receiveSocket = new Socket();
	}
	public void run(){
		try {
			
			byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
			
			
			//serverSocket = new ServerSocket(thisNode.getPort());
			sctpServerChannel = SctpServerChannel.open();
			serverAddr = new InetSocketAddress(thisNode.getPort());
			sctpServerChannel.bind(serverAddr);
			
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while(true){
			
			//writing node's parent and child information to a file after algorithm finishes
			if(ackNackCount >= thisNode.getNeighbours().size()){
				writeOutput(false);
			}
			/////////////////////////////////////////////////////////////////////////////////
			
			try{
				sctpChannel = sctpServerChannel.accept();
				MessageInfo messageInfo = sctpChannel.receive(byteBuffer, null,null);
				
				byteBuffer.position(0);
				byteBuffer.limit(MESSAGE_SIZE);
				byte[] bytes = new byte[byteBuffer.remaining()];
				byteBuffer.get(bytes, 0, bytes.length);
				ByteArrayInputStream b = new ByteArrayInputStream(bytes);
				ObjectInputStream ois = new ObjectInputStream(b);
				
				Message msg = (Message) (ois.readObject());
				
				byteBuffer.clear();
				byteBuffer.put(new byte[MESSAGE_SIZE]);
				byteBuffer.clear();
				
			
				if(msg.getMsgType().equals("Find")){	//find msg
					if(thisNode.isRoot()){
						//send nack
						Message nackMsg = new Message();
						nackMsg.setSender(thisNode);
						nackMsg.setMsgType("Nack");
						
						//send this nack msg back to the sender of Find message
						try {
							sendMessage(nackMsg, msg.getSender());
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							System.out.println("error here");
						}						
					
					}else {//this is not a root node
						
						if(thisNode.getParent() == null){	//parent is null
							//update the node itself first
							thisNode.setParent(msg.getSender());
							
							//update DFR
							thisNode.setDSR(msg.getSender().getDSR()+1);
							
							//send ack
							Message ackMsg = new Message();
							ackMsg.setSender(thisNode);
							ackMsg.setMsgType("Ack");
							
							try {
								sendMessage(ackMsg, msg.getSender());
							} catch (InterruptedException e) {
								//e.printStackTrace();
								System.out.println("error here");
							}
							
							for(Node node:thisNode.getNeighbours()){								
								Message findMsg = new Message();
								findMsg.setSender(thisNode);
								findMsg.setMsgType("Find");
								
								try {
									sendMessage(findMsg, node);
								} catch (InterruptedException e) {
									//e.printStackTrace();
									System.out.println("exception here");
								}
							}
							
						} else{	//parent is not null
							//now there will be two cases
							if(thisNode.getDSR() <= msg.getSender().getDSR() + 1){//my distance from root is less than Find sender's distance + 1
								
								//send nack
								Message nackMsg = new Message();
								nackMsg.setSender(thisNode);
								nackMsg.setMsgType("Nack");
								
								try {
									sendMessage(nackMsg, msg.getSender());
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									//e.printStackTrace();
									System.out.println("exception here");
								}
								
							}else {
								//(update parent, send ack to sender) and (releaseMe to the current parent) and find to all the neighbours again
								//1) releaseMe to current parent
								Message releaseMsg = new Message();
								releaseMsg.setSender(thisNode);
								releaseMsg.setMsgType("ReleaseMe");
								
								try {
									sendMessage(releaseMsg, thisNode.getParent());
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									//e.printStackTrace();
									System.out.println("exception here");
								}
							
								//2)(update parent, send ack to FindSender, update DFR)
								thisNode.setParent(msg.getSender());
								
								//update DFR
								thisNode.setDSR(msg.getSender().getDSR()+1);
								
								//send ack
								Message ackMsg = new Message();
								ackMsg.setSender(thisNode);
								ackMsg.setMsgType("Ack");

								try {
									sendMessage(ackMsg, msg.getSender());
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									//e.printStackTrace();
									System.out.println("exception here");
								}
								
								ackNackCount = 0;
								//3)find to all the neighbours
								//send find message to the neighbors
								for(Node node:thisNode.getNeighbours()){
									//Socket findSocket = new Socket(node.getHostName(), node.getPort());
									
									Message findMsg = new Message();
									findMsg.setSender(thisNode);
									findMsg.setMsgType("Find");

									try {
										sendMessage(findMsg, node);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										//e.printStackTrace();
										System.out.println("exception here");
									}
									
								}
								
								
							}
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
					//increment acknack count
					ackNackCount++;
				}
				
				if(msg.getMsgType().equals("ReleaseMe")) {
					//delete the corresponding children
					Node temp = new Node();
					for(int i=0;i<thisNode.getChildren().size();i++){
						temp = thisNode.getChildren().get(i);
						if(temp.getNodeId() == msg.getSender().getNodeId()){
							thisNode.getChildren().remove(i);
						}
					}
					//write to the file also
					writeOutput(false);
				}
				
				}catch(IOException | ClassNotFoundException e){
					//e.printStackTrace();
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
	
	void writeOutput(boolean flag){
		//write to file and exit
		File file = new File("config-"+thisNode.getNodeId()+".out");
		
		try {
			FileWriter fileWriter = new FileWriter(file, flag);
			//fileWriter.write("me: "+thisNode.getNodeId()+"\n");
			if(thisNode.getParent() != null){
				fileWriter.write(thisNode.getParent().getNodeId()+"\n");
			}else {
				fileWriter.write("*\n");
			}
			
			if(thisNode.getChildren().size() == 0){
				fileWriter.write("*");
			}else {
				for (Node node: thisNode.getChildren()){
					fileWriter.write(node.getNodeId()+" ");
				}
				fileWriter.write("\n");
			}
			
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//send msg to receiverNode
	void sendMessage(Message msg, Node receiverNode) throws InterruptedException {
		try{
		SocketAddress socketAddress = new InetSocketAddress(receiverNode.getHostName(), receiverNode.getPort());
		SctpChannel sctpChannel = SctpChannel.open();
		sctpChannel.connect(socketAddress);
		MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(msg);
		byteBuffer.put(bos.toByteArray());
		byteBuffer.flip();
		sctpChannel.send(byteBuffer, messageInfo);
		sctpChannel.close();
		byteBuffer.clear();
		}catch(IOException e){
			Thread.sleep(3000);
			//send message again if the server we are trying to connect to is not up yet
			sendMessage(msg, receiverNode);
		}
	}
}
