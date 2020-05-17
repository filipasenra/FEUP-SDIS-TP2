package com.assigment_2.SSLEngine;

import com.assigment_2.Chord.MessageFactoryChord;
import com.assigment_2.Chord.Node;
import com.assigment_2.Chord.SimpleNode;
import com.assigment_2.Peer;
import com.assigment_2.PeerClient;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;

/**
 * An SSL server for TLS Protocols.
 * <p>
 * This server will listen to a specific address and port and serve TLS connections.
 * <p>
 * After initialization, {@link SSLEngineServer#start()} should be called.
 * This will allow the server to start listening to new connection requests.
 * <p>
 * A {@link Runnable} with a {@link SSLEngineServer} object should be created.
 * The runnable (for example {@link ServerRunnable}) should start the server by calling
 * {@link SSLEngineServer#start()} in its run method.
 * It should also provide a stop method which will stop de server by calling {@link SSLEngineServer#stop()}
 */
public class SSLEngineServer extends SSLEngineHandler {

    /**
     * States if the server is active
     * It is false after {@link SSLEngineServer#stop()}
     */
    private boolean active;

    /**
     * The context will be initialized with a specific TLS protocol.
     * Will be used to create and {@link SSLEngine} for each new connection.
     */
    private final SSLContext context;

    /**
     * Examines one or more Channel instances, and determines which channels are ready for reading or writing.
     */
    private final Selector selector;


    /**
     * Applies a TLS protocol prepares to listen to an address and port.
     *
     * @param protocol The TLS protocol to be used. For Java 1.6 use up to TLSv1 protocol. For Java 1.7 or later can also use TLSv1.1 and TLSv1.2 protocols.
     * @param address  The address of the peer.
     * @param port     The peer's port that will be used.
     * @throws Exception if an error occurs.
     */
    public SSLEngineServer(String protocol, String address, int port) throws Exception {

        context = SSLContext.getInstance(protocol);
        context.init(createKeyManagers("../com/assigment_2/Resources/server.jks", "storepass", "keypass"), createTrustManagers("../com/assigment_2/Resources/trustedCerts.jks", "storepass"), new SecureRandom());

        SSLSession dummySession = context.createSSLEngine().getSession();

        ByteBuffer myAppData = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
        ByteBuffer myNetData = ByteBuffer.allocate(dummySession.getPacketBufferSize());
        ByteBuffer peerAppData = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
        ByteBuffer peerNetData = ByteBuffer.allocate(dummySession.getPacketBufferSize());

        setByteBuffers(myAppData, myNetData, peerAppData, peerNetData);

        dummySession.invalidate();

        selector = SelectorProvider.provider().openSelector();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(address, port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        active = true;

    }

    /**
     * Starts listening to an address and port
     *
     * @throws Exception if an error occurs.
     */
    public void start() throws Exception {

        System.out.println("Waiting for connections...");

        //If stop wasn't called
        while (isActive()) {

            selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    // a connection was accepted by a ServerSocketChannel.
                    accept(key);

                } else if (key.isConnectable()) {
                    // a connection was established with a remote server.

                } else if (key.isReadable()) {
                    // a channel is ready for reading
                    read((SocketChannel) key.channel(), (SSLEngine) key.attachment());


                    byte[] received = getPeerAppData().array();
                    MessageFactoryChord messageFactoryChord = new MessageFactoryChord();
                    messageFactoryChord.parseMessage(received);

                    if (messageFactoryChord.getMessageType().equals("FIND_SUCCESSOR")) {
                        BigInteger request_id = messageFactoryChord.getRequestId();

                        SimpleNode node = PeerClient.getNode().find_successor(request_id);
                        System.out.println(node.getId());
                    }


                } else if (key.isWritable()) {
                    // a channel is ready for writing
                }

                keyIterator.remove();
            }

        }

        System.out.println("Ended Connection! See you later!");

    }

    /**
     * Makes the server inactive.
     * Allows the the reading loop in {@link SSLEngineServer#start()} to stop.
     */
    public void stop() {
        System.out.println("Closing server...");

        exec.shutdown();
        selector.wakeup();
        active = false;
    }

    /**
     * Handles a connection that was accepted by the {@link SocketChannel}.
     *
     * @param key - represents the new connection
     * @throws Exception if an error occurs.
     */
    private void accept(SelectionKey key) throws Exception {

        System.out.println("New connection request!");

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(false);
        engine.beginHandshake();

        if (doHandshake(socketChannel, engine)) {
            socketChannel.register(selector, SelectionKey.OP_READ, engine);
        } else {
            socketChannel.close();
            System.out.println("Failed to connect to client!");
        }
    }

    /**
     * Determines if the the server is active or not.
     *
     * @return if the server is active or not.
     */
    private boolean isActive() {
        return active;
    }

}