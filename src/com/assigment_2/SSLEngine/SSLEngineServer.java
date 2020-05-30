package com.assigment_2.SSLEngine;

import com.assigment_2.PeerClient;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Set;
import javax.net.ssl.*;

/**
 * An SSL server for TLS Protocols.
 *
 * This server will listen to a specific address and port and serve TLS connections.
 *
 * After initialization, {@link SSLEngineServer#run()} should be called.
 * This will allow the server to start listening to new connection requests.
 */
public class SSLEngineServer extends SSLEngineHandler implements Runnable {

    private final MessagesHandler messagesHandler;
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
    public SSLEngineServer(String protocol, String address, int port, MessagesHandler messagesHandler) throws Exception {

        this.messagesHandler = messagesHandler;

        context = SSLContext.getInstance(protocol);

        KeyManager[] keyManagers = createKeyManagers("../com/assigment_2/Resources/server.jks", "storepass", "keypass");
        TrustManager[] trustManagers = createTrustManagers("../com/assigment_2/Resources/trustedCerts.jks", "storepass");

        context.init(keyManagers, trustManagers, new SecureRandom());

        selector = SelectorProvider.provider().openSelector();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(address, port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        active = true;

    }

    /**
     * Starts listening to an address and port
     */
    public void run() {

        System.out.println("Waiting for connections...");

        try {

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
                        if (read((SocketChannel) key.channel(), (SSLEngine) key.attachment()) != null) {

                            ByteBuffer byteBuffer = getPeerAppData();

                            byteBuffer.flip();
                            byte[] arr = new byte[byteBuffer.remaining()];
                            byteBuffer.get(arr);

                            this.messagesHandler.run((SocketChannel) key.channel(), (SSLEngine) key.attachment(), arr);

                        }

                    } else if (key.isWritable()) {
                        // a channel is ready for writing
                    }

                    keyIterator.remove();
                }

            }

            System.out.println("Ended Connection! See you later!");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Makes the server inactive.
     * Allows the the reading loop in {@link SSLEngineServer#run()} to stop.
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

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(false);

        setByteBuffers(engine.getSession());

        engine.beginHandshake();

        if (handshake(socketChannel, engine)) {
            socketChannel.register(selector, SelectionKey.OP_READ, engine);
        } else {
            socketChannel.close();
            System.out.println("Failed to connect to client!");
        }
    }

    public void write(SocketChannel socketChannel, SSLEngine engine, byte[] message) throws Exception {
        super.write(socketChannel, engine, message);
    }

    /**
     * Determines if the the server is active or not.
     *
     * @return if the server is active or not.
     */
    public boolean isActive() {
        return active;
    }
}