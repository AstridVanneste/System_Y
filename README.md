#   System Y
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
  +---------------------------------+
  | Nameserver                      |
  +---------------------------------+
  | TreeMap <int, InetAddress> map  |
  | Resolver nodeResolver           |
  | DiscoveryAgent discovery        |
  | ShutdownAgent shutdown          |
  +---------------------------------+
  | RMI server on port 1099         |
  +---------------------------------+
                |
                +-----------------------------------+-----------------------------------+
                |                                   |                                   |
  +-------------+---------------+   +---------------+-------------+   +---------------------------------+
  | Resolver                    |   | DiscoveryAgent              |   | ShutdownAgent                   |
  +-----------------------------+   +-----------------------------+   +---------------------------------+
  | implements RMI              |   | implements Runnable         |   | implements RMI                  |
  |                             |   | Listens on UDP Port 1997    |   |                                 |
  +-----------------------------+   +-----------------------------+   +---------------------------------+
  | IP lookup (String filename) |   | Continuously listens for    |   | void suspectedDead (IP nodeIp)  |
  | IP lookup (int nodeId)      |   | incoming broadcasts or      |   | void shutdown (IP nodeIp)       |
  +-----------------------------+   | multicasts                  |   +---------------------------------+
                                    +-----------------------------+
                                    | When a incoming connection  |
                                    | is received, the agent      |
                                    | responds appropriately      |
                                    +-----------------------------+
```

## TO-DO
- [ ] Implement UDP multicast

## Design Decisions
- Security?
- Will the nameserver limit the amount of requests a node can make over a specified period of time?
