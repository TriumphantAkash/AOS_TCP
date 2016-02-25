import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

//java ConfigParser <file> <nodeId> 
//this parser is gonna work on temp file which is output of launcher script
public class ConfigParser {
	//return ArrayList<Node> of neighbours
	List<Node> getNeighbors(String fileName, int nodeNumber, int nodeCount) throws IOException{
		
		String line;
		FileReader fileReader = new FileReader(fileName);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		
		List<Node> neighboursDetailsList = new ArrayList<Node>();
		
		//move the file pointer to the place till it is just before the line that has 'nodeNumber's neighbours
		for(int i=0; i<nodeNumber+nodeCount+1; i++){
			line = bufferedReader.readLine();
		}
		
			
		line = bufferedReader.readLine();	//now line has node ids of 'nodeNumber's neighbours
		String[] neighboursNodeIds = line.split("\\s+");
		
		//removing extra whitespaces from the strings in order to get rid of NumberFormatException later
		for(int i=0;i<neighboursNodeIds.length;i++){
			neighboursNodeIds[i]=neighboursNodeIds[i].trim();
		}
				
		//System.out.println("number of neighbours are: "+neighboursNodeIds);
		//System.out.println("this node is: "+nodeNumber);
		if(neighboursNodeIds.length > 0){//it means non empty line
			//System.out.println(line);
		}
		
		//close the buffered reader in order to reinitialize it from the start of the file
		bufferedReader.close();
		
		
		fileReader = new FileReader(fileName);
		BufferedReader newBufferedReader = new BufferedReader(fileReader);	//now points to the start of the file
		
		newBufferedReader.readLine();	//for ignoring the first line that has root node and node count
		
		
		//System.out.println(line);
				for(int j=0; j<nodeCount; j++){
					line = newBufferedReader.readLine();
					//System.out.println(line);
					String[] nodeDetails = line.split("\\s+");
					
					//removing extra whitespaces from the strings in order to get rid of NumberFormatException later
					for(int i=0;i<nodeDetails.length;i++){
						nodeDetails[i]=nodeDetails[i].trim();
					}
					
					for(String nodeId: neighboursNodeIds) {
						if(nodeId.equals(nodeDetails[0])){
							Node node = new Node();
							node.setNodeId(Integer.parseInt(nodeDetails[0]));
							node.setHostName(nodeDetails[1]);
							node.setPort(Integer.parseInt(nodeDetails[2]));
							neighboursDetailsList.add(node);
						}
					}
				}
				newBufferedReader.close();		
			//System.out.println(neighboursDetailsList);
		return neighboursDetailsList;
	
	}
}
