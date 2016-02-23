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
	public static ServerThread serverThread;
	
	//assuming args[0] be fileName, 
	//args[1] be this node's ID
	
	public static void main(String[] args) throws Exception {
		thisNode = new Node();
		
		for(int i=0; i<args.length; i++){
			args[i]=args[i].trim();
		}
		thisNode.setNodeId(Integer.parseInt(args[1]));
//		thisNode.setHostName(args[2]);
//		thisNode.setPort(Integer.parseInt(args[3]));
		////////////////////////////////////////////////////////////////////////////////////////
		//This is To get the My Own Hostname and My Own Port Number
		FileReader fileReader = new FileReader(args[0]);	//Reading "Temp" file
		BufferedReader bufferReader1 = new BufferedReader(fileReader);	//	BufferedReader 1 -- To Get MyHostname and MyPortNumber
	
		String line = bufferReader1.readLine();				//Line = First line of Temp file-- "TotalNumNodes" "RootNode"
		String[] words = line.split("\t", -1);		//String Array of Words to store TotalNumNodes and RootNode
		for(int i=0; i<words.length; i++){
			words[i]=words[i].trim();
		}

		nodeCount = Integer.parseInt(words[0]);	//TotalNodes = First word in First Line
		System.out.println("node count is: "+nodeCount);
		rootNodeId = Integer.parseInt(words[1]);		//RootNode = Second Word in First line
		System.out.println("root node id is :"+rootNodeId);
		
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
		thisNode.setNeighbours(configParser.getNeighbors(args[0], Integer.parseInt(args[1]), nodeCount));
		System.out.println("["+ thisNode.getNodeId()+"]"+"my neighbours are:");
		for(Node node:thisNode.getNeighbours()){
			System.out.print(node.getNodeId()+" ");
		}
		System.out.println("\n");
		
		FileReader fileReader1 = new FileReader(args[0]);
		BufferedReader bufferedReader = new BufferedReader(fileReader1);
		
		try {

			serverThread = new ServerThread(thisNode);
			serverThread.start();
			
		//first making clients for all the neighbours of root node (make sure, the servers on root's neighbours are up and running before deploying this code to root)
			if(thisNode.isRoot()){
				Thread.sleep(5000);
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
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		finally {
			//sendSocket.close();
			//receiveSocket.close();
			serverThread.join();
		}
	}
}
