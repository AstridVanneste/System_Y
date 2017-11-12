#   System Y
##  Terminology
**Owner** = The node whose ID is lower and the closest to the hash value generated from the filename by the Nameserver

**Local File Holder** = The person who controls a local copy of a file (and who isn't the owner of the file)

##  Responsibilities
### Nameserver
-   Calculates hashes from filenames (`Math.abs(filename.hashCode() % 32768)`)
-   Keeps a map that maps ID (hash) <-> IP address
-   Saves map in CSV format
-   When a new node joins the network the NameServer checks if its hash is unique

####  Nameserver Procedures
##### Discovery (Thomas & Astrid)
-   Clients sends broadcast (or multicast) over network to request Nameserver IP
-   Nameserver replies with a packet containing the nameserver's IP, The IP of a node's neighbours, the node's own ID

##### Failure (Fergan & Axel)
-   The Nameserver's FailureAgent listens for messages about nodes being possibly dead
-   When a node is possible-dead, the Nameserver checks this by "pinging" the dead node
-   If the node's death is verified, then the shutdown procedure is initiated for the dead node

##### Shutdown (Fergan & Axel)
-   The node that needs to be shut down is removed from the ID-IP table
-   The neighbours of the node that's being shut down are notified that their neighbours have changed
-   A broadcast/multicast is sent across the network to notify all hosts that a host has been removed from the network, this allows all nodes that own files of the lost node to remove these files from their owned files

##### Regular Operation (Thomas & Astrid)
-   Any node can request the Nameserver to resolve a filename or a node-ID to an IP

####  Nameserver Classes
```
+-------------+
| Nameserver  |
+-------------+-------------------------------------------------------------------------------------------------+
|  +---------------------------------+                                                                          |
|  | Nameserver                      |                                                                          |
|  +---------------------------------+                                                                          |
|  | Implements Resolver and         |                                                                          |
|  | ShutdownAgentinterfaces         |                                                                          |
|  +---------------------------------+                                                                          |
|  | TreeMap <int, String> map       |                                                                          |
|  | Resolver nodeResolver           |                                                                          |
|  | DiscoveryAgent discovery        |                                                                          |
|  | NameServer.ShutdownAgent shutdown          |                                                                          |
|  +---------------------------------+                                                                          |
|  | RMI server on port 1099         |                                                                          |
|  +-------------+-------------------+                                                                          |
|                |                                                                                              |
|                +-----------------------------------+-----------------------------------+                      |
|                |                                   |                                   |                      |
|  +-------------+---------------+   +---------------+-------------+   +-----------------+---------------+      |
|  | Resolver                    |   | DiscoveryAgent              |   | NameServer.ShutdownAgent                   |      |
|  +-----------------------------+   +-----------------------------+   +---------------------------------+      |
|  | implements RMI              |   | implements Runnable         |   | implements RMI                  |      |
|  +-----------------------------+   | Listens on UDP Port 1997    |   +---------------------------------+      |
|  | IP lookup (int nodeId)      |   +-----------------------------+   | void suspectedDead (IP nodeIp)  |      |
|  | IP lookup (String filename)  |   | Continuously listens for    |   | void shutdown (IP nodeIp)       |      |
|  +-----------------------------+   | incoming broadcasts or      |   +---------------------------------+      |
|                                    | multicasts                  |                                            |
|                                    +-----------------------------+                                            |
|                                    | When a incoming connection  |                                            |
|                                    | is received, the agent      |                                            |
|                                    | responds appropriately      |                                            |
|                                    +-----------------------------+                                            |
+---------------------------------------------------------------------------------------------------------------+
```

### Node
When a node leaves the network (either because of a failure or because it wants to) its files are made inaccessible over the network (they are not removed)

### File Agent
The file agent is a list of all file present on the network.

It continuously circulates through the network by all nodes calling RMI methods on eachother

`void update (List<String> fileList)`

## TO-DO

### Opdracht 4
#### Discovery & Bootstrap
- [ ] Clean up Node code and move Discovery into LifeCycleManager
- [ ] Remove unnecessary println()'s from all code
- [ ] Fix concurrency issues in Network Classes
- [ ] Create a few Subclasses of the datagram class for (1. Discovery (request & reply), 2. Sending files (request & reply))
- [ ] Make Node-Discovery check for duplicate IP's
- [ ] Test Discovery System when going from 2 nodes -> 3 nodes
- [ ] Find a fix for failure-recursion
- [ ] Update Serializer to use ByteBuffer's

#### Shutdown (Thomas)

#### Failure
- [ ] Provide centralized interface (Static/Singleton?) for handling exceptions
- [ ] Add ping functionality

## Design Decisions
- Security?
- Will the nameserver limit the amount of requests a node can make over a specified period of time?
- Can downloaded files be reshared?
- What happens when a file is changed? In an owner? In a local file? In a downloaded file?
