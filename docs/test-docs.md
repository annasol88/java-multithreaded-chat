# Test Documentation

## Stress Testing
To stress test the ChatServer thread pool we have constructed the ChatServerStressTest.
This declares a testNumberOfThreads and creates that number of threads who each send a test request to our server.

This test was ran multiple times with for each configuration: 
testNumberOfThreads = 49
testNumberOfThreads = 50
testNumberOfThreads = 51
testNumberOfThreads = 200
It has been observed that regardless of the testNumberOfThreads, 
only a maximum of 50 is received by the server at a given time which is expected since that is 
the limit defined in our FixedThreadPool.

Interestingly, It has also been observed in the 200 tests: that after several runs the server occasionally only
receives 49 threads. Since this only happens on the rare occasion, it is potentially just a delay caused by many 
resources being running on our local machine: every ChatClient creates 3 threads of its own, each using up CPU and memory,
potentially delaying testRequests from being sent. 

A consideration has been noted to provide some sort of waiting message to a client when their connection thread
is waiting in the pool queue, so the system doesn't appear broken by just waiting.

## Synchronization Testing
ChatServerSynchronizationTest runs a series of stress tests on individual functions in the ChatServer
that require concurrent access to objects. 

NB: The tests in the test suite need to be run individually 
because for each test a new server is being created with the same server port which will not work on the one machine.

It is also observed that synchronized objects lock where expected and our concurrentHashmap and synchronizedList 
implementations do not cause parallel access issues.

It was also observed that when:
- changing Collections.synchronizedList -> ArrayList
- changing ConcurrentHashMap -> HashMap
- removing synchronized() lock 

these tests did not pass, and not all threads changes were recognised.
Hence, proving these 
and showing that the ConcurrentHashMap and Collections.synchronizedList implementations behave as expected.

## Acceptance Testing
The acceptance tests carried out are documented in testing/uat-tests.docx
To demonstrate the core functionality that was implemented works as
expected under normal circumstances.
