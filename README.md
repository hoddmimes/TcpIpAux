# TcpIpAux
Java TCP/IP wrapper with optional encryption and ZLIB compression.
The utility is message/package oriented rather than stream oriented.
Implicating that clients/server are send/receiving messages i.e. (byte buffers/vectors).
The interface is asynchronous, implicating that the wrapper starts a reading thread and deliver read messages 
via a callback when messages becomes available

When creating a client it's possible to define whatever compression and or encryption should be used or not. Three modes
are available
* **plain**, plain socket without encryption and compression
* **encrypt**, AES 256 bit encryption
* **compression**, ZLIB compression

Furthermore it is also possible to configure the server to require that the initialization of a connection require thyat clients provides a SSH key **_authorization_**.
Then configuring SSH _authorization_ the server must have access to client public keys. When the client connects it will sign the initial server sync message with its private SSH key. 
The server will verify the message using the client public SSH key. 


The encryption is AES 256 bit encryption with anonymous Diffe/Hellman key exchange. 
The ZLIB is based on the on the package from JCraft http://www.jcraft.com/jzlib/

A client and server sample is found in the package com.hoddmimes.tcpip.sample
* [Client Sample] (https://github.com/hoddmimes/TcpIpAux/blob/master/src/main/java/com/hoddmimes/tcpip/sample/Client.java)
* [Server Sample] (https://github.com/hoddmimes/TcpIpAux/blob/master/src/main/java/com/hoddmimes/tcpip/sample/Server.java)

When a connection is established an initial message is exchanged between the client and server outside the application message flow.
This takes place hidden for the application. The message sequence is the following 



```sequence
Client --> Server: InitRqst (optional SSH key signed)
Server --> Client: InitRsp

```