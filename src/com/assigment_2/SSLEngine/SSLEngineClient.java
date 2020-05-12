package com.assigment_2.SSLEngine;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;

import javax.net.ssl.*;

import static javax.net.ssl.SSLEngineResult.Status.OK;

/**
 * An SSL client for TLS Protocols.
 *
 * This client connects to a server by a given address and port.
 *
 * After initialization, {@link SSLEngineClient#connect()} should be called.
 * This will establish a connection with the server.
 *
 * After the connection is established, it is possible to write to the server
 * through the {@link SSLEngineClient#write(String)} and read a message from
 * the server through the {@link SSLEngineClient#read()}
 *
 */
public class SSLEngineClient extends SSLEngineHandler {

    /**
     * Server's address this client is going to connect to
     */
    private final String address;

    /**
     * Server's port this client is going to connect to
     */
    private final int port;

    /**
     * Engine that will encrypt and/or decrypt the date between the client and the server
     */
    private final SSLEngine engine;

    /**
     * The socket channel that is going to establish a non-blocking way to communication with the server.
     */
    private SocketChannel socketChannel;


    /**
     * Prepares the client to initiate the communication with the server.
     *
     * @param protocol The TLS protocol to be used. For Java 1.6 use up to TLSv1 protocol. For Java 1.7 or later can also use TLSv1.1 and TLSv1.2 protocols.
     * @param address The address of the peer.
     * @param port The peer's port that will be used.
     * @throws Exception if an error occurs.
     */
    public SSLEngineClient(String protocol, String address, int port) throws Exception  {
        this.address = address;
        this.port = port;

        KeyManager[] keyManagers = createKeyManagers("../com/assigment_2/Resources/client.jks", "storepass", "keypass");
        TrustManager[] trustManagers = createTrustManagers("../com/assigment_2/Resources/trustedCerts.jks", "storepass");

        SSLContext context = SSLContext.getInstance(protocol);
        context.init(keyManagers, trustManagers, new SecureRandom());

        this.engine = context.createSSLEngine(address, port);
        this.engine.setUseClientMode(true);

        SSLSession session = engine.getSession();

        ByteBuffer myAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        ByteBuffer peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
        ByteBuffer myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        ByteBuffer peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());

        setByteBuffers(myAppData, myNetData, peerAppData, peerNetData);

    }

    /**
     * Opens a socket channel to communicate with the server and starts the handshake protocol.
     *
     * @return True if the connection with the server was successful, false otherwise.
     * @throws Exception if an error occurs.
     */
    public boolean connect() throws Exception {

        // Create a nonblocking socket channel
        this.socketChannel = SocketChannel.open();
        this.socketChannel.configureBlocking(false);
        this.socketChannel.connect(new InetSocketAddress(this.address, this.port));

        // Complete connection
        while (!this.socketChannel.finishConnect()) {
            // do something until connect completed
        }

        //Created byte buffers for holding application and encoded data in constructor

        // Do initial handshake
        engine.beginHandshake();
        return doHandshake(socketChannel, engine);
    }

    /**
     * Send a message to the server
     *
     * @param message - message to sent to the server
     * @throws Exception if an error occurs.
     */
    public void write(String message) throws Exception {

        SSLEngineResult res;
        if ((res = write(socketChannel, engine, message)) != null) {

            if (res.getStatus() == OK)
                System.out.println("PRINT: " + message);
        }
    }

    /**
     * Receive a message from the server
     *
     * @throws Exception if an error occurs.
     */
    public void read() throws Exception {

        SSLEngineResult res;
        if ((res = read(socketChannel, engine)) != null) {

            if (res.getStatus() == OK)
                System.out.println("PRINT: " + this.peerNetData);
        }
    }


    /**
     * Method to shutdown connection with the server
     *
     * @throws Exception if an error occurs.
     */
    public void shutdown() throws Exception {

        System.out.println("Closing connection to server...");
        closeConnection(socketChannel, engine);
        exec.shutdown();
        System.out.println("Closed. See you later!");
    }

}
