#!/bin/bash

#Author: Mayank Agrawal
#Compile all java Programs
javac *.java
echo "Compiled all the Programms Successfully"

#Read the Command Line Arguments
#First: Configuration File
CONFIG=$1

#Second: NetID for PasswordLess SSH
netid=$2

#edit the file name without extension
config_file_name=$(echo $CONFIG | rev | cut -f1 -d"/" | rev | cut -f1 -d".") #without extension

# extract the important lines from the config file. the ones with no '#' or empty lines
sed -e "s/#.*//" $CONFIG | sed -e "/^\s*$/d" > temp

# insert a new line to EOF. It is necessary for the while loop
echo  >> temp
node_count=0
nodes_location="" #Stores a # delimited string of Location of each node
host_names=() #Stores the hostname of each node
neighbors_dict=() # Stores the Token path of each node
current_line=1

# Reading from the temp file created above
#IFS=''
while read line;
do
        #turn all spaces to single line spaces
	line=$(echo $line | tr -s ' ')
	########Extract Number of nodes and root node
        if [ $current_line -eq 1 ]; then
		node_count=$(echo $line | cut -f1 -d" ")
                let node_count=$node_count+0
		echo "Number of nodes read by laucher script are:" $node_count
		
		root_node=$(echo $line | cut -f2 -d" ")
		let root_node=$root_node+0
		echo "laucher script found the root node as:" $root_node

	 else
	 #########Extract Location of each node
	       if [ $current_line -le $(expr $node_count + 1) -a $current_line -ne $(expr $root_node + 2) ]; then
	            nodes_location+=$( echo -e $line"#" )
	            node_id=$(echo $line | cut -f1 -d" ")
	            hostname=$(echo $line | cut -f2 -d" ")
		    host_names[$node_id]="$hostname"
		    port_num=$(echo $line | cut -f3 -d" ")
		    neighbor_line=$(($node_count+$node_id+2))
		    
		   # echo "neighbor list is: value at " $neighbor_line
		   # echo $list
		   # echo "node_id" $node_id
		   # echo "hostname" $hostname
		   # echo "portNum" $port_num
		   echo "laucher script is now going to launch the java file into the" $hostname "and pass the argument two arguments as temp file and node ID" $node_id 
		    ssh -o StrictHostKeyChecking=no $netid@$hostname "cd $(pwd); java MainThread temp $node_id" &  #execute the java parser file and pass the args as node id and temp file
		    fi
	fi

	let current_line+=1
done < temp

	echo "Root Node is: " $root_node
	echo "laucher script is now going to launch the java file into root_Node" $root_node 
#	ssh -o StrictHostKeyChecking=no $netid@$hostname "cd $(pwd); java MainThread temp $root_node" &  #execute the java parser file and pass the args as node id and temp file


