# Design Documentation

## Threading

#### Operational Threads.

1. The ChatServer itself runs on a thread which accepts to new socket connections 
as they come through clients running the app. 
This allows server functions to be invoked by other threads without being blocked by serverSocket.accept().

2. When a new client (ChatClient) is run it creates a new socket that connects to the ChatServer submitting a new 
ChatClientThread to the thread pool for execution. Each ChatClientThread listens to requests coming from the socket
input stream (sent by a ChatClient) and responding to them appropriately.

#### Client Threads:

1. ListenerThread - dedicated to listening to the socket input stream (receiving messages from the server) 
and invoking the appropriate responses in the client. Having this on a separate thread prevents the 
socket -> inputStream -> readLine() function from blocking client operations.

2. WriterThread - dedicated to queuing client requests and sending them to the socket output stream 
(sending messages to the server). The implementation of client requests as a queue was made to 
mitigate potential for synchronization issues with multiple requests coming in 
however it was since discovered that synchronization issues are very unlikely with our basic implementation
(requests only come in as fast as a user types...) making this thread redundant.

3. ConsoleListenerThread - dedicated to listening to user input from the command line and sending it back to the client 
to be handled. This is essential to solve the blocking issue with listening to System.in, hence allowing messages 
from the server (or other client in a chatroom) to not get blocked because the ChatClient is waiting for user input. 

## Synchronization

#### ConcurrentHashmap implementation
Our application uses the Java Util Concurrent Hashmap to store:
- chat rooms in a server
- accounts in a server
- friends list & friends request list on a User object

This is an implementation for storing data that should be accessed synchronously.
From the Java Docs it is stated that a ConcurrentHashmap allows any number of threads 
to perform retrieval operation at any given time, hence preventing the potential for deadlocks on read-only operations,
such as get user chat rooms, get chat members, get user profile etc.

However, for put operations the Object provides a putIfAbsent() method which locks on the hashmap object
only allowing one thread to put something in the hashmap at a time, hence preventing synchronisation issues from occurring.
such as in: register user, send friend request, create new chatroom.
//TOREFACTOR

#### Synchronizing on objects
for updates objects r synchronized
//TODO

#### synchronizedList
For chat messaging functionality, the server stores a list to reference all the clients.
synchronizedList implements concurrent access during add() and remove() operations ensuring thread-safety, 
hence no need to synchronize manually in the app.
running a chatroom and iterates through them to send messages to active users. 
This is synchronized when being traversed (as suggested in the docs) to ensure thread 
safety between parallel threads leaving and joining a chatroom.

## Deadlocks
#### Application designed to prevent possibility of deadlocks
Read-only (get) server functions allow parallel access at all times, 
This prevents mutual exclusion and won't cause any synchronisation issues 
with the data, (at most one client will have a slightly out of date view).

Where mutual exclusion is necessary (updates/adds/removes functionality) the
server functions have been constructed to only lock on 1 resource at a time when possible.
This prevents a hold and wait since there is nothing to wait for.

Circular waits are therefore impossible with our implementation.

#### server requests queued in a concurrentLinkedQueue
This was intended to remove synchronisation issues 

## ThreadPool
A newFixedThreadPool implementation with 50 threads was used in the application. 
This seems to be the most appropriate since it allows us to maintain control of our resources
provided the app has been configured to run on off a local machine.

A newCachedThreadPool implementation was also considered, to allow an undefined number of clients to connect,
however from our understanding of the documentation this is only appropriate for short-lived threads < 60seconds 
not a chat interface that will run for several minutes.

## IO Streams
#### Reasoning for choosing the following IO stream objects
PrintWriter - Unlike BufferedReader and allows auto-flush for every println/print/write which
removes the need to manually call the flush() method each time data is written.
The println operation also adds a /n (new line) exit character to strings written, which removes the
need to manually do this.
 
BufferedReader - A fast and efficient implementation for reading Strings from an input stream.

## Some considerations for future
#### missing functionality
The application is currently missing Admin functionality, which was anticipated due to low capacity 
and focus on quality of thread-safety as opposed to quantity of features.
However, the addition of admin features was thought about and where modifications need to be made to 
prevent synchronisation issues in the server this is documented as a javadoc against the functions.

