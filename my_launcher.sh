#!/bin/bash

javac *.java
echo "Compiled all the Programms Successfully"
CONFIG=$1
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

while read line;
do
        #turn all spaces to single line spaces
	line=$(echo $line | tr -s ' ')
	########Extract Number of nodes and root node
        if [ $current_line -eq 1 ]; then
		node_count=$(echo $line | cut -f1 -d" ")
                let node_count=$node_count+0
		echo "Number of nodes are:" $node_count
		
		root_node=$(echo $line | cut -f2 -d" ")
		let root_node=$root_node+0
		echo "Root Node is:" $root_node


	 else
	 #########Extract Location of each node
	       if [ $current_line -le $(expr $node_count + 1) -a $current_line -ne $(expr $root_node + 2) ]; then
			echo "echoing 4"
	            nodes_location+=$( echo -e $line"#" )
	            node_id=$(echo $line | cut -f1 -d" ")
	            hostname=$(echo $line | cut -f2 -d" ")
		    host_names[$node_id]="$hostname"
		    port_num=$(echo $line | cut -f3 -d" ")
		    neighbor_line=$(($node_count+$node_id+2))

		#increment the line pointer to the desired locartion
		    let current_line=$current_line+$node_count
		    read line
		    echo $line
		    let current_line=$current_line-$node_count
 
		    echo "node_id" $node_id
		    echo "hostname" $hostname
		    echo "portNum" $port_num
		    #ssh -o StrictHostKeyChecking=no $netid@$hostname "cd $(pwd); echo "haha" > sampleFile
##command line arguments of the java file are 1)temp file 2)node id 3)hostname 4)port
		ssh -o StrictHostKeyChecking=no $netid@$host "cd $(pwd); java MainThread temp $node_id $hostname $port_num" &
		fi
	fi

	let current_line+=1
done < temp


