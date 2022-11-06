# Design Documentation

## Use of threads
The server itself is run on a thread.
A new thread is then used to run each new client that is created.
This is submitted into a threadpool on the server to be handled.

##Synchronization issues


##IO Streams
printWriter is used as an output stream between our client and client handler
since this allows auto-flush which is what we need whenever messages r sent.

## Client Server Communication
The client server connection is implemented using a threadpool and exec service...
Since the number of threads in our application will be dynamic depending on the 
number of clients connected to the server; the thread pool is implemented as a newCachedThreadPool
since this allows threads to be created as required and reused when they become available.



## User Interface
User Interface is implemented as a console application. //TODO


##




## Scalability
The Thread pool accepts multiple clients connecting to it.
The server also supports multiple chatrooms
The ClientDriver Class allows multiple clients to be run on the same machine to allow testing.
...//TODO

## Modularity
//TODO