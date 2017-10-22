# Discovery Protocol for System Y
## Overview
The discovery-service for System Y will consist of 2 messages, the initial being a broadcast, intended to go from the client to the Nameserver.

The second will be a unicast response from the Nameserver to the client.
Both packets will adhere to the following packet-format.


When a client enters the network, it broadcasts a packet with a randomly chosen transaction ID over the network.

When the Nameserver receives this packet, it copies the Transaction ID and request code, fills in the reply code and adds some data to the end.


```
                SEPARATE FIELDS                                         COMPLETE PACKAGE
        1 Byte                                                      8 Bytes
    |<--------->|<--------------------------------->|       |<--------------------------------------------->|
    +-----------+-----------------------------------+       +-----------+-----------------------------------+--
    |   Version |   Data Length                     |       |   Version |   Data Length                     | ^
    +-----------+-----------------------------------+       +-----------+-----------------------------------+ |
                                                            |   Transaction ID                              | |
        8 Bytes                                             +-----------------------------------------------+ |
|<--------------------------------------------->|           |   Request Code                                | | 5 Octets
    +-----------------------------------------------+       +-----------------------------------------------+ |
    |   Transaction ID                              |       |   Reply Code                                  | |
    +-----------------------------------------------+       +-----------------------------------------------+ |
                                                            |   Data                                        | v
        8 Bytes                                             +-----------------------------------------------+--
    |<--------------------------------------------->|
    +-----------------------------------------------+       Total: 40 Bytes without data
    |   Request Code                                |
    +-----------------------------------------------+
    
        8 Bytes
    |<--------------------------------------------->|
    +-----------------------------------------------+
    |   Reply Code                                  |
    +-----------------------------------------------+
    
        ??? Bytes
    |<--------------------------------------------->|
    +-----------------------------------------------+
    |   Data                                        |
    +-----------------------------------------------+
```

## Fields

### Version
The version number of this datagram. Allows the network to read datagrams from potentially outdated nodes.

### Data Length
The length (in bytes) of all data following the header.

### Transaction ID
The transaction ID is assumed to be unique for every request-reply pair. It is chosen randomly when the request is sent and kept the same in the reply.

### Request Code
This field helps the receiver determine what needs to be done with the incoming datagram. It also tells them how to interpret the attached data.


### Reply Code
The reply code tells the receiver the result of their request. It also tells them how to interpret the attached data.

## Request Codes

- 0x00000001  Request to be added to network

## Reply Codes

- 0x00000000  Succesfully added to network, reply to 0x00000001
- 0x00000001  Failed to add to network, Duplicate ID, choose new Node ID. Reply to 0x00000001.
- 0x00000002  Failed to add to network, Duplicate IP, choose new IP address or fix DHCP. Reply to 0x00000001.