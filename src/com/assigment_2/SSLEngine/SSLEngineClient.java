package com.assigment_2.SSLEngine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * An SSL/TLS client that connects to a server using its IP address and port.
 * <p/>
 * After initialization of a {@link SSLEngineClient} object, {@link SSLEngineClient#connect()} should be called,
 * in order to establish connection with the server.
 * <p/>
 * When the connection between the client and the object is established, {@link SSLEngineClient} provides
 * a public write and read method, in order to communicate with its peer.
 *
 * @author <a href="mailto:alex.a.karnezis@gmail.com">Alex Karnezis</a>
 */
public class SSLEngineClient extends SSLEngineBase {

    /**
     * The remote address of the server this client is configured to connect to.
     */
    private final String remoteAddress;

    /**
     * The port of the server this client is configured to connect to.
     */
    private final int port;

    /**
     * The engine that will be used to encrypt/decrypt data between this client and the server.
     */
    private final SSLEngine engine;

    /**
     * The socket channel that will be used as the transport link between this client and the server.
     */
    private SocketChannel socketChannel;


    /**
     * Initiates the engine to run as a client using peer information, and allocates space for the
     * buffers that will be used by the engine.
     *
     * @param protocol The SSL/TLS protocol to be used. Java 1.6 will only run with up to TLSv1 protocol. Java 1.7 or higher also supports TLSv1.1 and TLSv1.2 protocols.
     * @param remoteAddress The IP address of the peer.
     * @param port The peer's port that will be used.
     * @throws Exception
     */
    public SSLEngineClient(String protocol, String remoteAddress, int port) throws Exception  {
        this.remoteAddress = remoteAddress;
        this.port = port;

        SSLContext context = SSLContext.getInstance(protocol);
        context.init(createKeyManagers("../com/assigment_2/Resources/client.jks", "storepass", "keypass"), createTrustManagers("../com/assigment_2/Resources/trustedCerts.jks", "storepass"), new SecureRandom());
        engine = context.createSSLEngine(remoteAddress, port);
        engine.setUseClientMode(true);

        SSLSession session = engine.getSession();
        myAppData = ByteBuffer.allocate(1024);
        myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
        peerAppData = ByteBuffer.allocate(1024);
        peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());
    }

    /**
     * Opens a socket channel to communicate with the configured server and tries to complete the handshake protocol.
     *
     * @return True if client established a connection with the server, false otherwise.
     * @throws Exception
     */
    public boolean connect() throws Exception {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(remoteAddress, port));
        while (!socketChannel.finishConnect()) {
            // can do something here...
        }

        engine.beginHandshake();
        return doHandshake(socketChannel, engine);
    }

    /**
     * Public method to send a message to the server.
     *
     * @param message - message to be sent to the server.
     * @throws IOException if an I/O error occurs to the socket channel.
     */
    public void write(String message) throws IOException {
        write(socketChannel, engine, message);
    }

    /**
     * Should be called when the client wants to explicitly close the connection to the server.
     *
     * @throws IOException if an I/O error occurs to the socket channel.
     */
    public void shutdown() throws IOException {
        System.out.println("About to close connection with the server...");
        closeConnection(socketChannel, engine);
        executor.shutdown();
        System.out.println("Goodbye!");
    }

}
