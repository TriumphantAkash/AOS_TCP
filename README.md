# DistributedSystem_SCTP_sockets_less-threads
A SCTP socket based Distributed System

This is a Java application that builds a minimum height spanning tree out of a graph of machines upto 45 node
Uses SCTP sockets to communicate between machines

1) This Project Consist of all java files
	MainThread.java
	ConfigParser.java
	ServerThread.java
	Node.java
	Message.java
	
2) This Project also Consists of two shell Script File:
	launcher.sh
	cleanup.sh
	
3) Steps to Run the Program:
	
		Step1: sh cleanup.sh "<Path_To>config.txt" "netID"
		Step2: sh launcher.sh "<Path_To>config.txt" "netID"

Output:
Minimum Height Spanning Tree of the input nodes 

