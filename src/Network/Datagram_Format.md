# Discovery Protocol for System Y
## Sequence
The discovery-service for System Y will consist of 2 messages, the initial being a broadcast, intended to go from the client to the Nameserver.

The second will be a unicast response from the Nameserver to the client.
Both packets will adhere to the following packet-format.


When a client enters the network, it broadcasts a packet with a randomly chosen transaction ID over the network.

When the Nameserver receives this packet, it copies the Transaction ID and request code, fills in the reply code and adds some data to the end.


```
        1 Byte
    |<--------->|<--------------------------------->|
    +-----------+-----------------------------------+
    |   Version |   Data Length                     |
    +-----------+-----------------------------------+
    
        8 Bytes
    |<--------------------------------------------->|
    +-----------------------------------------------+
    |   Transaction ID                              |
    +-----------------------------------------------+
    
        8 Bytes
    |<--------------------------------------------->|
    +-----------------------------------------------+
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

## Request Codes

- 0x00000001  Request to be added to network

## Reply Codes

- 0x00000000  Succesfully added to network, reply to 0x00000001
- 0x00000001  Failed to add to network, Duplicate ID, choose new Node ID. Reply to 0x00000001.
- 0x00000002  Failed to add to network, Duplicate IP, choose new IP address or fix DHCP. Reply to 0x00000001.