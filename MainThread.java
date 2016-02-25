import java.io.FileReader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class MainThread {
	public static ServerSocket serverSocket;
	public static Socket receiveSocket, sendSocket;
	public static int rootNodeId;
	public static Node thisNode;
	public static Message msg;
	public static int nodeCount;
	public static int ackNackCount = 0;
	public static ServerThread serverThread;
	public static final int MESSAGE_SIZE = 500000;
	
	public static ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
	
	
	//assuming args[0] be fileName, 
	//args[1] be this node's ID
	
	public static void main(String[] args) throws Exception {
		thisNode = new Node();
		
		for(int i=0; i<args.length; i++){
			args[i]=args[i].trim();
		}
		thisNode.setNodeId(Integer.parseInt(args[1]));
		
		////////////////////////////////////////////////////////////////////////////////////////
		//This is To get the My Own Hostname and My Own Port Number
		FileReader fileReader = new FileReader(args[0]);	//Reading "Temp" file
		BufferedReader bufferReader1 = new BufferedReader(fileReader);	//	BufferedReader 1 -- To Get MyHostname and MyPortNumber
	
		String line = bufferReader1.readLine();				//Line = First line of Temp file-- "TotalNumNodes" "RootNode"
		String[] words = line.split("\\s+");		//String Array of Words to store TotalNumNodes and RootNode
		for(int i=0; i<words.length; i++){
			words[i]=words[i].trim();
		}

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
			words = line.split("\\s+");
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
		thisNode.setNeighbours(configParser.getNeighbors(args[0], Integer.parseInt(args[1]), nodeCount));
		//System.out.println("["+ thisNode.getNodeId()+"]"+"my neighbours are:");
		for(Node node:thisNode.getNeighbours()){
			//System.out.print(node.getNodeId()+" ");
		}
		//System.out.println("\n");
		
		FileReader fileReader1 = new FileReader(args[0]);
		BufferedReader bufferedReader = new BufferedReader(fileReader1);
	

			serverThread = new ServerThread(thisNode);
			serverThread.start();
			
		//first making clients for all the neighbours of root node (make sure, the servers on root's neighbours are up and running before deploying this code to root)
			if(thisNode.isRoot()){
				Message findMsg = new Message();
				findMsg.setSender(thisNode);
				findMsg.setMsgType("Find");
				
				//Thread.sleep(5000);
				for(Node node:thisNode.getNeighbours()){
					createClient(findMsg, node);
				}
			}
			//so that the main thread waits for the server threads to exit
			serverThread.join();
		}
	
	//used for sending findMsg to node server
	static void createClient(Message findMsg, Node node) throws InterruptedException{
		SocketAddress socketAddress = new InetSocketAddress(node.getHostName(), node.getPort());
		try{
			SctpChannel sctpChannel = SctpChannel.open();
			sctpChannel.connect(socketAddress);
			MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(findMsg);
			byteBuffer.put(bos.toByteArray());
			byteBuffer.flip();
			sctpChannel.send(byteBuffer, messageInfo);
			sctpChannel.close();
			byteBuffer.clear();
		}catch(IOException e1){
			//System.out.println("["+thisNode.getNodeId()+"]"+"client received exception while connecting to "+ node.getNodeId());
			Thread.sleep(3000);
			//recreate a client socket if the server it is trying to connect to has not started yet
			createClient(findMsg, node);
		}
	}
}
