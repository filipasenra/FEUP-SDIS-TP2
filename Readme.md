# FEUP SDIS

Project for the Distributed Systems (SDIS) class of the Master in Informatics and Computer Engineering (MIEIC) at the Faculty of Engineering of the University of Porto (FEUP).

Java SE: 

Made by 
 * [Cláudia Inês Costa Martins](https://git.fe.up.pt/up201704136)
 * [Ana Filipa Campos Senra](https://git.fe.up.pt/up201704077)
 * [João Matos]()
 * [Joana Ferreira]()

## Compile

#### In folder src:
1. javac -d out com/assigment_2/*.java

## Run

### PeerClient

#### In folder out:

1. java com.assigment_2.PeerClient <version> <server id> <access_point> <MC_IP_address> <MC_port> <MDB_IP_address> <MDB_port> <MDR_IP_address> <MDR_port>

   **Ex:** java com.assigment_2.PeerClient 2.0 1 "localhost" 9222

### TestApp

#### In folder out:

1. java com.assigment_2.TestApp <rmi_peer_ap> <sub_protocol> <arguments_of_protocol>
   
   2.1. <rmi_peer_ap> BACKUP <file_path> <replication_degree>
   
   2.2. <rmi_peer_ap> DELETE <file_path>
   
   2.3. <rmi_peer_ap> RESTORE <file_path>
   
   2.4. <rmi_peer_ap> RECLAIM <disk_space>
   
   **Ex:** java com.assigment_2.TestApp "localhost" 9222 BACKUP "/home/filipasenra/Desktop/Sem título 1.odt" 2
