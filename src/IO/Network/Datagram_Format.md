# Network Protocol for System Y
## Overview
System Y will use UDP datagrams for various purposes.

This document tries to establish a standard format for these datagrams to allow for increased re-usability.

All datagram communication will consist of a reply-request pair.

One request is sent by party A and it will be matched with 1 reply from party B.

The request-reply pair will always have the same transaction ID in both datagrams. The request code will not change between request and reply aswell.

The reply field is left empty (0) in the request and will be filled in in the reply.

The data length field is the amount of bytes following the header that belong to this datagram. This can change between request and reply.

Version is the version number of the party that sent the datagram, this can change between reply and request.

## Header Format

![alt text](https://github.com/AstridVanneste/System_Y/blob/master/res/ProtocolHeader.png "Protocol Header")

```
                SEPARATE FIELDS                                         COMPLETE PACKAGE
        1 Byte          3 Bytes                                     4 Bytes
    |<--------->|<--------------------------------->|       |<--------------------------------------------->|
    +-----------+-----------------------------------+       +-----------+-----------------------------------+--
    |   Version |   Data Length                     |       |   Version |   Data Length                     | ^
    +-----------+-----------------------------------+       +-----------+-----------------------------------+ |
                                                            |   Transaction ID                              | |
        4 Bytes                                             +----------------------+------------------------+ |
    |<--------------------------------------------->|       |   Request Code       | Reply Code             | V 3 Quads
    +-----------------------------------------------+       +----------------------+------------------------+--
    |   Transaction ID                              |       |   Data                                        |
    +-----------------------------------------------+       +-----------------------------------------------+
                                                            Total: 12 Bytes without data
        2 Bytes                     2 Bytes
    |<--------------------->|<--------------------->|
    +-----------------------+-----------------------+
    |   Request Code        | Reply Code            |
    +-----------------------+-----------------------+
    
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
The reply code tells the receiver the result of their request. It also tells them how to interpret the attached data. The reply code is left empty (0) in the request.

## Request Codes

- 0x0001  Request to be added to network

- 0x4001  Request for file

## Reply Codes

- 0x0001  Succesfully added to network, reply to 0x0001
- 0x0002  Failed to add to network, Duplicate ID, choose new Node ID. Reply to 0x0001.
- 0x0003  Failed to add to network, Duplicate IP, choose new IP address or fix DHCP. Reply to 0x0001.

- 0x4001  Sending file. reply to 0x8001.

### Discovery Service
The Discovery Service in System Y will consist of a client sending a multicast message onto the network and the Nameserver replying with a unicast message to the new client.
The Nameserver can either return reply code 0, reply code 1 or reply code 2.
When the server returns reply code 0 the data contains the node's ID and the IP's of its neighbours.
After a new node joins the network, the new node's neighbours are informed that they need to change their next/previous node.
Ownership of some files is also rechecked to make sure the new node also contains all files it's hash maps to.
Replies 1 and 2 contain no data, they do however tell the client why joining the network failed and allow the client to be reconfigured before trying again.


#### Data format for Discovery Request (Multicast)
```
       4 Bytes        Name Length Bytes       4 Bytes
    |<------------->|<----------------->|<--------------->|
    +---------------+-------------------+-----------------+
    |   Name Length | Node Name         | Node Unicast IP |
    +---------------+-------------------+-----------------+

    Total length: unknown (Name Length + 8)
```

#### Data format for Discovery Reply
```
       2 Bytes          2 byte           
    |<--------->|<-------------------->|
    +-----------+----------------------+
    |   Node ID |    Number of Nodes   |
    +-----------+----------------------+

    Total: 4 bytes
```
