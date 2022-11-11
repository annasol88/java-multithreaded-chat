# Design Documentation

## Thread safety
The application implements 3 Runnable Object types. 
1. The server which runs on its own thread, and maintains a threadpool responsible for scheduling
the client and chat threads submitted to it.

2. The ChatClientThread, which runs a separate thread for each client currently using the application, that listens to
the client requests and returns relevant data from the server. 

3. The ChatRoomHandler, which runs a separate thread for each chat window a logged-in client has open.
This thread listens to incoming messages from other chat users allowing thread safety between messages sent and received in the chat.


##Synchronization issues 
###login
###chat

##Deadlocks

##IO Streams
printWriter is used as an output stream between our client and client handler
since this allows auto-flush which is what we need whenever messages are sent.

## Client Server Communication
The client server connection is implemented using a threadpool and exec service...
Since the number of threads in our application will be dynamic depending on the 
number of clients connected to the server; the thread pool is implemented as a newCachedThreadPool
since this allows threads to be created as required and reused when they become available.


## User Interface
User Interface is implemented as a console application. //TODO

## Scalability
The Thread pool accepts multiple clients connecting to it.
The server also supports multiple chatrooms
The ClientDriver Class allows multiple clients to be run on the same machine to allow testing.
...//TODO

## Modularity
//TODO