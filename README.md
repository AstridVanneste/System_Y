#   System_Y
##  Responsibilities
### NameServer
-   Calculates hashes from filenames (`Math.abs(filename.hashCode() % 32768)`)
-   Keeps a map that maps ID (hash) <-> IP address
