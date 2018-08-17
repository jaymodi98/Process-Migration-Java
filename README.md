# Process-Migration-Java
This project demonstrates the concept of process migration in OS on JVM. This was my project during the course of Operating Systems at SEAS, Ahmedabad University during Monsoon 2017 (Semester 5).

❖ Following are the OS Concepts used in the project:
➢ Thread Management :
    ServerManager thread:- to accept client connections
    ClientListener threads:- to communicate with newly joined client.
➢ Concurrency Mechanisms : to make the program thread safe following keywords are used :
    Synchronized
    Volatile
    Transient
    Atomic
➢ Exception Handling : Exceptions have been handled and appropriate error messages have been
displayed wherever possible using Java’s Exception Handling mechanism. This takes care of
possible aborts and faults.

❖ Following are the features of the project:
➢ Multiple clients supported : One server can check/control the status of all processes of all clients .
➢ Function call to migratable process on demand : invoke a function of a running migratable process with an input command.
➢ Network communication scalable. Generalized message structure and dispatcher makes it possible to add any type of message to the framework conveniently.
➢ Load balancer friendly. The control manager class (Server Operator class) is loosely linked from other parts of server side. There is scope to a custom load balancer (future work! ).

Steps for deployment have been given in the file "OS Project Report".
