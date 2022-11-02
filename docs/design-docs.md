# Design Choices

## Client Server Communication
The client server connection is implemented using a threadpool and exec service...
Since the number of threads in our application will be dynamic depending on the 
number of clients connected to the server; the thread pool is implemented as a newCachedThreadPool
since this allows threads to be created as required and reused when they become available.



## User Interface
User Interface currently uses a hybrid console and Jframe approach...//TODO

## Scalability
The Thread pool accepts multiple clients connecting to it.
The server also supports multiple chatrooms
The ClientRunner Class allows multiple clients to be run on the same machine to allow testing.
...//TODO

## Modularity
//TODO