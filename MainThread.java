import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MainThread {
	public static ServerSocket serverSocket;
	public static Socket receiveSocket, sendSocket;
	public static int rootNodeId;
	public static Node thisNode;
	public static Message msg;
	public static int nodeCount;
	public static int ackNackCount = 0;
	
	//assuming args[0] be fileName, 
	//args[1] be this node's ID
	
	public static void main(String[] args) throws IOException {
		thisNode = new Node();
		
		thisNode.setNodeId(Integer.parseInt(args[1]));
//		thisNode.setHostName(args[2]);
//		thisNode.setPort(Integer.parseInt(args[3]));
		////////////////////////////////////////////////////////////////////////////////////////
		//This is To get the My Own Hostname and My Own Port Number
		FileReader fileReader = new FileReader(args[0]);	//Reading "Temp" file
		BufferedReader bufferReader1 = new BufferedReader(fileReader);	//	BufferedReader 1 -- To Get MyHostname and MyPortNumber
	
		String line = bufferReader1.readLine();				//Line = First line of Temp file-- "TotalNumNodes" "RootNode"
		String[] words = line.split("\t", -1);		//String Array of Words to store TotalNumNodes and RootNode
		nodeCount = Integer.parseInt(words[0]);	//TotalNodes = First word in First Line
		rootNodeId = Integer.parseInt(words[1]);		//RootNode = Second Word in First line
		
		if(rootNodeId==thisNode.getNodeId())
		{
			thisNode.setRoot(true);		//Set as Root
		}
		else
		{
			thisNode.setRoot(false);	//Set as NonRoot
		}
		
		//This For Loop is to get MyHostName and MyPortNumber
		for(int i=0; i<nodeCount;i++)
		{
			line = bufferReader1.readLine();	//Reading From Second Line till Last Line of Host Information(just before the Neighbour List Starts)
			//filewriter.write("node before" +myNodeId+ "is:" +line);
			words = line.split("\t", -1);
			for(int j=0;j<words.length;j++)
			{
				words[j]=words[j].trim();				
			}
			
			if(Integer.parseInt(words[0]) == thisNode.getNodeId())  //Comparing the First Word and MyNodeID
			{
				//filewriter.write("Node ID of the Current Node is:" +myNodeId);
				thisNode.setHostName(words[1]);
				//filewriter.write("The hostname of current Node is:" +thisNode.getHostName());
				thisNode.setPort(Integer.parseInt(words[2]));
				//filewriter.write("The PortNumber of the Current node is: " +thisNode.getPortNumber());	
			}
			
		}
		bufferReader1.close();

		////////////////////////////////////////////////////////////////////////////////////////
		
		
		ConfigParser configParser = new ConfigParser();
		thisNode.setNeighbours(configParser.getNeighbors(args[0], Integer.parseInt(args[1])));
		
		FileReader fileReader1 = new FileReader(args[0]);
		BufferedReader bufferedReader = new BufferedReader(fileReader1);
		
		try {
			serverSocket = new ServerSocket(thisNode.getPort());
			if(thisNode.isRoot()){
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
			
			while(true){
				
				//writing node's parent and child information to a file after algorithm finishes
				if(ackNackCount >= thisNode.getNeighbours().size()){
					//write to file and exit
					File file = new File("output.txt");
					
					try {
						FileWriter fileWriter = new FileWriter(file);
						fileWriter.write("me: "+thisNode.getNodeId()+"\n");
						fileWriter.write("parent: "+thisNode.getParent()+"\n");
						fileWriter.write("children: ");
						
						for (Node node: thisNode.getChildren()){
							fileWriter.write(node.getNodeId()+" ");
						}
						
						fileWriter.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				/////////////////////////////////////////////////////////////////////////////////
				
				
				receiveSocket = serverSocket.accept();
				
				//control comes here whenever a new client is connected to the server
				//receiveSocket.getLocalAddress();		
				
				ObjectInputStream ois = new ObjectInputStream(receiveSocket.getInputStream());
				
				msg = (Message)ois.readObject();
				
					if(msg.getMsgType().equals("Find")){	//find msg
						if(thisNode.isRoot() || (!thisNode.getParent().equals(null))){
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
							
							sendSocket = new Socket(msg.getSender().getHostName(), msg.getSender().getPort());
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
			}

			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		finally {
			//sendSocket.close();
			receiveSocket.close();
		}
	}
}
