#   System Y
##  Terminology
**Owner** = The node whose ID is lower and the closest to the hash value generated from the filename by the Nameserver

**Local File Holder** = The person who controls a local copy of a file (and who isn't the owner of the file)

## To-Do
- [x] Add hasFile() method to FileManager
- [x] Change dodgy `while(){synchronized}` construction in Node to spin-lock that's locked in the node and unlocked remotely (Java Semaphores)
- [x] Clean 'owned' and 'replicated' folders when node shuts down
- [ ] Fix mnemonics in Console Main.

### Opdracht 5
#### Replicatie (Sessie 1)
- [x] Node moet start methodes door-callen
- [x] `FileLedger` klasse:
```
  private short ownerID;
  private short localID;
  private List<Short> copies; 
```

- [x] `FileType enum {OWNER, LOCAL, DOWNLOAD}`

- [x] `FileManagerInterface` interface:
```
  public void refresh()
  public void pushFile(String filename, int fileSize, FileType type)
  public void pullFile(short dst, String filename) (Push doorcallen)
```
- [x] `FileManager` klasse:
```
  implements FileManagerInterface
  TCPSocket, continu luisteren op vaste poort (Zie Network.Constants)
  start() methode (TCP starten)
  stop() methode
  RMIServer draaien (binden op Node poort)
```

### Opdracht 6
#### FileAgent & RecoveryAgent
- [x] Brainstorm about FileAgent
- [x] Brainstorm about RecoveryAgent
- [ ] Test FileAgent
- [ ] Test RecoveryAgent

### Opdracht 7
#### GUI
- [ ] Create General UI design
- [ ] Implement UI in Java FX

## Design Decisions
- Security?
- Will the nameserver limit the amount of requests a node can make over a specified period of time?
- Can downloaded files be reshared?
- What happens when a file is changed? In an owner? In a local file? In a downloaded file?
