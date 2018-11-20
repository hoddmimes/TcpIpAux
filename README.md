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
* **encryption_compression**, AES 256 bit encryption and compression. 


The encryption is AES 256 bit encryption with anonymous Diffe/Hellman key exchange. 
The ZLIB is based on the on the package from JCraft http://www.jcraft.com/jzlib/

A client and server sample is found in the package com.hoddmimes.tcpip.sample
* ![Client] (https://github.com/hoddmimes/TcpIpAux/blob/master/src/main/java/com/hoddmimes/tcpip/sample/Client.java "Client Sample")
* ![Server] (https://github.com/hoddmimes/TcpIpAux/blob/master/src/main/java/com/hoddmimes/tcpip/sample/Client.java "Server Sample")

