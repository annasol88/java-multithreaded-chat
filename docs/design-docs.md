# Design Documentation

## Use of threads

#### The application uses several threads to handle the necessary functionality.

1. The ChatServer itself runs on a thread, dedicated to listening to new socket connections 
in the form of clients running the app and adding those client threads to a thread pool. 
This allows server operations to not be blocked by the serverSocket.accept().

2. When a new client (ChatClient) is ran it creates a new socket that connects to the ChatServer submitting a new 
ChatClientThread to the thread pool. Each ChatClientThread is responsible for listening to requests sent 
from the client over a socket input stream and responding to them via the socket output stream.

#### The ChatClient when created, runs its own 3 sub-threads that do the following:

1. ListenerThread - dedicated to listening to the socket input stream (receiving messages from the server) 
and invoking the appropriate responses in the client. Having this on a separate thread prevents the 
readLine() function on the input stream from blocking other client operations.

2. WriterThread - dedicated to queuing client requests and sending them to the socket output stream 
(sending messages to the server). In theory, the writer doesn't need to run on a separate thread however 
with this implementation we can mitigate potential for synchronization or deadlock issues by scheduling client requests in a 
concurrentLinkedQueue. (more on this below)

3. ConsoleListenerThread - dedicated to listening to user input from the command line and sending it back to the client 
to be handled. This is essential to solve our final blocking issue with listening to System.in, hence allowing messages 
from the server (or other users in a chatroom) to not get blocked because the ChatClient is waiting for user input. 


## Synchronization Issues 
#### ConcurrentHashmaps to store data
Our application uses the Java Util Concurrent Hashmap to store:
- chat rooms in a server
- accounts in a server
- friends list on a User object

This is the cleanest implementation we could find to store data that should be accessed synchronously.
From our understanding of the Java Docs a ConcurrentHashmap allows any number of threads to perform 
retrieval operation at any given time, and for update/add/remove operations the thread must lock the particular 
segment in which the thread wants to operate, allowing these operations to be done safely. 
This also removes the need for manually synchronizing methods and data objects when using them. 

#### Client thread synchronizedList
For chat messaging functionality, the server stores a list to reference all the clients currently 
running a chatroom and iterates through them to send messages to active users. 
This is implemented as a synchronized list and is synchronized when being traversed to ensure thread 
safety between clients leaving and joining a chatroom.

## Deadlocks

#### Attempts in the design were made to remediate the possibility of deadlocks
write concurrentLinkedQueue
login split into 2 parts


## ThreadPool

## IO Streams
#### Reasoning for choosing the following IO stream objects
PrintWriter - Unlike BufferedReader and allows auto-flush for every println/print/write which
removes the need to manually call the flush() method each time data is written.
The println operation also adds a /n (new line) exit character to strings written, which removes the
need to manually do this also.
 
BufferedReader - A fast and efficient implementation for reading Strings from an input stream.

## Considerations
#### Some considerations for future.
Improvements could be made to message passing to use Enumerated string or schemas to remove coupling between our 
communication streams will improve readability and human error.  

Improvements could be made to the number of server requests done


missing functionality - 

## Scalability
...//TODO

## Modularity
//TODO