# FEUP SDIS

Project for the Distributed Systems (SDIS) class of the Master in Informatics and Computer Engineering (MIEIC) at the Faculty of Engineering of the University of Porto (FEUP).

Made by 
 * [Cláudia Inês Costa Martins](https://git.fe.up.pt/up201704136)
 * [Ana Filipa Campos Senra](https://git.fe.up.pt/up201704077)
 * [João Matos](https://git.fe.up.pt/up201705471)
 * [Joana Ferreira](https://git.fe.up.pt/up201705722)

## Compile

#### In folder src:
1. javac -d out com/assigment_2/*.java

## Run

### PeerClient

#### In folder out:

1. java com.assigment_2.PeerClient <version> <rmi_peer_ap> `<address>` `<port>` 

   **Ex:** java com.assigment_2.PeerClient 2.0 1 "localhost" 9222
   
2. java com.assigment_2.PeerClient <version> <rmi_peer_ap> <address> <port> <address_initiator> <port_initiator> 

   **Ex:** java com.assigment_2.PeerClient 2.0 2 "localhost" 9223 "localhost" 9222


### TestApp

#### In folder out:

1. java com.assigment_2.TestApp <rmi_peer_ap> <sub_protocol> <arguments_of_protocol>
   
   2.1. <rmi_peer_ap> BACKUP <file_path> <replication_degree>
   
   2.2. <rmi_peer_ap> DELETE <file_path>
   
   2.3. <rmi_peer_ap> RESTORE <file_path>
   
   2.4. <rmi_peer_ap> RECLAIM <disk_space>
   
   **Ex:** java com.assigment_2.TestApp 1 BACKUP "/home/claudia/Desktop/imagem.jpg" 2
