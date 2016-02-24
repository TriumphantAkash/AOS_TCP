import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

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
	public static final int MESSAGE_SIZE = 10000;
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
				//receiveSocket = serverSocket.accept();
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
				
				
			//control comes here whenever a new client is connected to the server
			//receiveSocket.getLocalAddress();		
			
			//ObjectInputStream ois = new ObjectInputStream(receiveSocket.getInputStream());
			
			//msg = (Message)ois.readObject();
			
				if(msg.getMsgType().equals("Find")){	//find msg
				//	if(msg.)
					if(thisNode.isRoot()){
						//send nack
						Message nackMsg = new Message();
						nackMsg.setSender(thisNode);
						nackMsg.setMsgType("Nack");
						
						//********************************************************************************
//						sendSocket = new Socket(msg.getSender().getHostName(), msg.getSender().getPort());
//						ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
//						
//						//get the node's details who sent this find message
//						//and now send this nack message to that host's server
//						oos.writeObject(nackMsg);
//						sendSocket.close();
						//*********************************************************************************
						SocketAddress socketAddress = new InetSocketAddress(msg.getSender().getHostName(), msg.getSender().getPort());
						SctpChannel sctpChannel = SctpChannel.open();
						sctpChannel.connect(socketAddress);
						messageInfo = MessageInfo.createOutgoing(null, 0);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(bos);
						oos.writeObject(nackMsg);
						byteBuffer.put(bos.toByteArray());
						byteBuffer.flip();
						sctpChannel.send(byteBuffer, messageInfo);
						sctpChannel.close();
						byteBuffer.clear();
						
						//*********************************************************************************
					
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
							//******************************************************************************
//							try{
//								sendSocket = new Socket(msg.getSender().getHostName(), msg.getSender().getPort());
//							}catch(ConnectException e){
//								System.out.println("["+ thisNode.getNodeId()+"]"+ "connect exception when try to connect to"+msg.getSender().getNodeId());
//							}
//							
//							ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
//							
//							//get the node's details who sent this find message
//							//and now send this nack message to that host's server
//							oos.writeObject(ackMsg);
//							
//							sendSocket.close();
							//send find message to the neighbors
							
							//*********************************************************************************
							SocketAddress socketAddress = new InetSocketAddress(msg.getSender().getHostName(), msg.getSender().getPort());
							SctpChannel sctpChannel = SctpChannel.open();
							sctpChannel.connect(socketAddress);
							messageInfo = MessageInfo.createOutgoing(null, 0);
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(bos);
							oos.writeObject(ackMsg);
							byteBuffer.put(bos.toByteArray());
							byteBuffer.flip();
							sctpChannel.send(byteBuffer, messageInfo);
							sctpChannel.close();
							byteBuffer.clear();

							
							//*********************************************************************************
							for(Node node:thisNode.getNeighbours()){
								//Socket findSocket = new Socket(node.getHostName(), node.getPort());
								
								Message findMsg = new Message();
								findMsg.setSender(thisNode);
								findMsg.setMsgType("Find");
								//********************************************************************************
//								Socket findSendSocket = new Socket(node.getHostName(), node.getPort());
//								ObjectOutputStream findOos = new ObjectOutputStream(findSendSocket.getOutputStream());
//								
//								findOos.writeObject(findMsg);
//								
//								findSendSocket.close();
								//*********************************************************************************
								socketAddress = new InetSocketAddress(node.getHostName(), node.getPort());
								sctpChannel = SctpChannel.open();
								sctpChannel.connect(socketAddress);
								messageInfo = MessageInfo.createOutgoing(null, 0);
								bos = new ByteArrayOutputStream();
								oos = new ObjectOutputStream(bos);
								oos.writeObject(findMsg);
								byteBuffer.put(bos.toByteArray());
								byteBuffer.flip();
								sctpChannel.send(byteBuffer, messageInfo);
								sctpChannel.close();
								byteBuffer.clear();
								//*********************************************************************************
								
								
								
							}
						} else{	//parent is not null
							//now there will be two cases
							if(thisNode.getDSR() <= msg.getSender().getDSR() + 1){//my distance from root is less than Find sender's distance + 1
								//send nack
								Message nackMsg = new Message();
								nackMsg.setSender(thisNode);
								nackMsg.setMsgType("Nack");
								//*******************************************************************************
//								sendSocket = new Socket(msg.getSender().getHostName(), msg.getSender().getPort());
//								ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
//								
//								//get the node's details who sent this find message
//								//and now send this nack message to that host's server
//								oos.writeObject(nackMsg);
//								sendSocket.close();
								//**********************************************************************************
								SocketAddress socketAddress = new InetSocketAddress(msg.getSender().getHostName(), msg.getSender().getPort());
								SctpChannel sctpChannel = SctpChannel.open();
								sctpChannel.connect(socketAddress);
								messageInfo = MessageInfo.createOutgoing(null, 0);
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								ObjectOutputStream oos = new ObjectOutputStream(bos);
								oos.writeObject(nackMsg);
								byteBuffer.put(bos.toByteArray());
								byteBuffer.flip();
								sctpChannel.send(byteBuffer, messageInfo);
								sctpChannel.close();
								byteBuffer.clear();
								//***********************************************************************************
								
							}else {
								//(update parent, send ack to sender) and (releaseMe to the current parent) and find to all the neighbours again
								//1) releaseMe to current parent
								Message releaseMsg = new Message();
								releaseMsg.setSender(thisNode);
								releaseMsg.setMsgType("ReleaseMe");
								
								//***********************************************************************************
//								sendSocket = new Socket(thisNode.getParent().getHostName(), thisNode.getParent().getPort());
//								ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
//								
//								//get the node's details who sent this find message
//								//and now send this nack message to that host's server
//								oos.writeObject(releaseMsg);
//								sendSocket.close();
								//**************************************************************************************
								SocketAddress socketAddress = new InetSocketAddress(thisNode.getParent().getHostName(), thisNode.getParent().getPort());
								SctpChannel sctpChannel = SctpChannel.open();
								sctpChannel.connect(socketAddress);
								messageInfo = MessageInfo.createOutgoing(null, 0);
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								ObjectOutputStream oos = new ObjectOutputStream(bos);
								oos.writeObject(releaseMsg);
								byteBuffer.put(bos.toByteArray());
								byteBuffer.flip();
								sctpChannel.send(byteBuffer, messageInfo);
								sctpChannel.close();
								byteBuffer.clear();
								//**************************************************************************************
								//2)(update parent, send ack to FindSender, update DFR)
								thisNode.setParent(msg.getSender());
								
								//update DFR
								thisNode.setDSR(msg.getSender().getDSR()+1);
								
								//send ack
								Message ackMsg = new Message();
								ackMsg.setSender(thisNode);
								ackMsg.setMsgType("Ack");
//								//**************************************************************************************
//								try{
//									sendSocket = new Socket(msg.getSender().getHostName(), msg.getSender().getPort());
//								}catch(ConnectException e){
//									System.out.println("["+ thisNode.getNodeId()+"]"+ "connect exception when try to connect to"+msg.getSender().getNodeId());
//								}
//								
//								ObjectOutputStream oos1 = new ObjectOutputStream(sendSocket.getOutputStream());
//								
//								//get the node's details who sent this find message
//								//and now send this nack message to that host's server
//								oos1.writeObject(ackMsg);
//								
//								sendSocket.close();
								
								//set ackNackCount to 0;
								//***************************************************************************************
								socketAddress = new InetSocketAddress(msg.getSender().getHostName(), msg.getSender().getPort());
								sctpChannel = SctpChannel.open();
								sctpChannel.connect(socketAddress);
								messageInfo = MessageInfo.createOutgoing(null, 0);
								bos = new ByteArrayOutputStream();
								oos = new ObjectOutputStream(bos);
								oos.writeObject(ackMsg);
								byteBuffer.put(bos.toByteArray());
								byteBuffer.flip();
								sctpChannel.send(byteBuffer, messageInfo);
								sctpChannel.close();
								byteBuffer.clear();
								//***************************************************************************************
								ackNackCount = 0;
								//3)find to all the neighbours
								//send find message to the neighbors
								for(Node node:thisNode.getNeighbours()){
									//Socket findSocket = new Socket(node.getHostName(), node.getPort());
									
									Message findMsg = new Message();
									findMsg.setSender(thisNode);
									findMsg.setMsgType("Find");
									//****************************************************************************
//									Socket findSendSocket = new Socket(node.getHostName(), node.getPort());
//									ObjectOutputStream findOos = new ObjectOutputStream(findSendSocket.getOutputStream());
//									
//									findOos.writeObject(findMsg);
//									
//									findSendSocket.close();
									//*****************************************************************************
									socketAddress = new InetSocketAddress(node.getHostName(), node.getPort());
									sctpChannel = SctpChannel.open();
									sctpChannel.connect(socketAddress);
									messageInfo = MessageInfo.createOutgoing(null, 0);
									bos = new ByteArrayOutputStream();
									oos = new ObjectOutputStream(bos);
									oos.writeObject(findMsg);
									byteBuffer.put(bos.toByteArray());
									byteBuffer.flip();
									sctpChannel.send(byteBuffer, messageInfo);
									sctpChannel.close();
									byteBuffer.clear();
									//************************************************************************************
									
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
					//do noting
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
					//write to file and exit
					File file = new File(thisNode.getNodeId()+"output.txt");
					
					try {
						FileWriter fileWriter = new FileWriter(file, false);
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
