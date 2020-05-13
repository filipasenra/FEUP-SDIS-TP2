package com.assigment_2.SSLEngine;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SSLEngineHandler {


    /**
     * ByteBuffer that contains this peer's data (decrypted) to be sent to the other peer'
     * Before sending to the other peer' it should be encrypted by using {@link SSLEngine#wrap(ByteBuffer, ByteBuffer)}
     *
     * Should be (at least) the size of the outgoing data
     *
     * */
    private ByteBuffer myAppData;


    /**
     * ByteBuffer that contains this peer's data (encrypted) to be sent to the other peer'
     * Generated after encrypting {@link SSLEngineHandler#myNetData} with {@link SSLEngine#wrap(ByteBuffer, ByteBuffer)}
     *
     * It should be initialized using {@link SSLSession#getPacketBufferSize()}
     *
     * */
    private ByteBuffer myNetData;


    /**
     * ByteBuffer that contains the other peer's data (decrypted) received from the other peer
     * Obtain after {@link SSLEngineHandler#peerAppData} is decrypted by using {@link SSLEngine#unwrap(ByteBuffer, ByteBuffer)}
     *
     * It Must be large enough to hold the application data from any peer.
     * It should be initialized using {@link SSLSession#getPacketBufferSize()}
     * If necessary, its size should be enlarge.
     *
     * Check {@link SSLEngineHandler#enlargeBuffer(ByteBuffer, int)}
     *
     * */
    private ByteBuffer peerAppData;


    /**
     * ByteBuffer that contains the other peer's data (encrypted) received from the other peer'
     * It should be initialized with size of 16KB
     *
     * If the {@link SSLEngine#unwrap(ByteBuffer, ByteBuffer)} detects large packets,
     * the buffer sizes returned by SSLSession will be used to updated the size dynamically.
     *
     * Check {@link SSLEngineHandler#enlargeBuffer(ByteBuffer, int)}}
     *
     * */
    private ByteBuffer peerNetData;


    /**
     * Handles tasks that may pop up during {@link SSLEngine#beginHandshake()}
     *
     * */
    protected ExecutorService exec = Executors.newSingleThreadExecutor();

    /**
     * Sets buffers with the correct values.
     *
     * @param myAppData - ByteBuffer that contains the other peer's data (decrypted) received from the other peer'
     * @param myNetData - ByteBuffer that contains this peer's data (encrypted) to be sent to the other peer'
     * @param peerAppData - ByteBuffer that contains the other peer's data (decrypted) received from the other peer'
     * @param peerNetData - ByteBuffer that contains the other peer's data (encrypted) received from the other peer'
     *
     * */
    public void setByteBuffers(ByteBuffer myAppData, ByteBuffer myNetData, ByteBuffer peerAppData, ByteBuffer peerNetData) {
        this.myAppData = myAppData;
        this.myNetData = myNetData;
        this.peerAppData = peerAppData;
        this.peerNetData = peerNetData;
    }


    /**
     * Implements the handshake protocol between this peer and the other peer'
     * Used at the beginning and ending of an exchange
     *
     * @param socketChannel - SocketChannel to communicate between this peer and the other peer'
     * @param engine - Engine that will encrypt and/or decrypt the date between the other peer'and this peer
     *
     * @return True if the connection was successful, false otherwise.
     *
     * */
    protected boolean doHandshake(SocketChannel socketChannel, SSLEngine engine) throws Exception {

        myNetData.clear();
        peerNetData.clear();

        System.out.println("Doing Handshake...");

        // Begin handshake
        SSLEngineResult.HandshakeStatus handshakeStatus = engine.getHandshakeStatus();
        while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {

            switch (handshakeStatus) {
                case NEED_UNWRAP:
                    if((handshakeStatus = readIntern(socketChannel, engine)) == null)
                        return false;

                    break;
                case NEED_WRAP:
                    if((handshakeStatus = write(socketChannel, engine)) == null)
                        return false;

                    break;
                case NEED_TASK:
                    Runnable task;
                    while ((task = engine.getDelegatedTask()) != null) {
                        exec.execute(task);
                    }
                    handshakeStatus = engine.getHandshakeStatus();
                    break;
                case NEED_UNWRAP_AGAIN:
                    break;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
            }
        }

        return true;


    }

    protected void read(SocketChannel socketChannel, SSLEngine engine) throws Exception {

        System.out.println("Reading...");

        peerNetData.clear();

        if (readIntern(socketChannel, engine) != null) {
            peerAppData.flip();
            System.out.println("RECEIVED: " + new String(peerAppData.array()));

        }

    }

        /**
         *
         * Reads a message from another peer
         *
         * @param socketChannel - SocketChannel to communicate between this peer and the other peer
         * @param engine - Engine that will encrypt and/or decrypt the date between the other peer'and this peer
         * @throws Exception if an error occurs.
         *
         * */
    private SSLEngineResult.HandshakeStatus readIntern(SocketChannel socketChannel, SSLEngine engine) throws Exception {

        // Read TLS encoded data from peer
        int num = socketChannel.read(peerNetData);
        if (num < 0) {

            if (engine.isInboundDone() && engine.isOutboundDone()) {
                return null;
            }

            System.out.println("Received end of stream. Closing connection.");
            engine.closeInbound();
            closeConnection(socketChannel, engine);

            return null;

        }

        // Process incoming data
        peerNetData.flip();
        peerAppData.clear();
        SSLEngineResult res = engine.unwrap(peerNetData, peerAppData);
        peerNetData.compact();

        switch (res.getStatus()) {
            case OK:
                break;
            case BUFFER_OVERFLOW:
                peerAppData = enlargeApplicationBuffer(peerAppData, engine);
                break;
            case BUFFER_UNDERFLOW:
                peerNetData = handleBufferUnderflow(peerNetData, engine);
                break;
            case CLOSED:
                System.out.println("Wants to close connection");
                closeConnection(socketChannel, engine);
                System.out.println("Closed connection");

                return null;
            default:
                throw new IllegalStateException("Invalid SSL status: " + res.getStatus());
        }

        return res.getHandshakeStatus();

    }


    protected void write(SocketChannel socketChannel, SSLEngine engine, String message) throws Exception {

        myAppData.clear();
        myAppData.put(message.getBytes());
        myAppData.flip();

        if (write(socketChannel, engine) != null)
            System.out.println("SENT: " + message);
    }
        /**
         * Will send a message to a peer
         *
         * @param socketChannel - SocketChannel to communicate between this peer and the other peer
         * @param engine - Engine that will encrypt and/or decrypt the date between the other peer'and this peer
         * @throws Exception if an error occurs.
         * @return
         */
    protected SSLEngineResult.HandshakeStatus write(SocketChannel socketChannel, SSLEngine engine) throws Exception {

        // Generate TLS encoded data (handshake or application data)
        myNetData.clear();
        SSLEngineResult res = engine.wrap(myAppData, myNetData);

        switch (res.getStatus()) {
            case OK:
                myNetData.flip();

                //Write until it fails
                while (myNetData.hasRemaining()) {
                    socketChannel.write(myNetData);
                }

                break;
            case BUFFER_OVERFLOW:
                myNetData = enlargePacketBuffer(myNetData, engine);
                break;
            case BUFFER_UNDERFLOW:
                throw new SSLException("Buffer underflow occurred after a wrap.");
            case CLOSED:
                System.out.println("Wants to close connection");
                closeConnection(socketChannel, engine);
                System.out.println("Closed connection");
                return null;
            default:
                throw new IllegalStateException("Invalid SSL status: " + res.getStatus());
        }

        return res.getHandshakeStatus();
    }

    /**
     * Enlarges a buffer, when overflow happens or underflow
     *
     * @param buffer - buffer to be enlarge
     * @param sessionProposedCapacity - recommended size by the engine's session
     *
     * @return The same buffer if there is no space problem or a new buffer with the same data but more space.
     * */
    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        return buffer;
    }

    /**
     * Handles {@link SSLEngineResult.Status#BUFFER_UNDERFLOW}
     *
     * @param buffer - buffer that caused the underflow
     * @param engine - Engine that will encrypt and/or decrypt the date between the other peer'and this peer
     *
     * @return The same buffer if there is no space problem or a new buffer with the same data but more space.
     *
     * */
    protected ByteBuffer handleBufferUnderflow(ByteBuffer buffer, SSLEngine engine) {
        if (engine.getSession().getPacketBufferSize() < buffer.limit()) {
            return buffer;
        } else {
            ByteBuffer replaceBuffer = enlargePacketBuffer(buffer, engine);
            buffer.flip();
            replaceBuffer.put(buffer);
            return replaceBuffer;
        }
    }

    /**
     * Handles {@link SSLEngineResult.Status#BUFFER_OVERFLOW}
     *
     * @param buffer - buffer that caused the overflow
     * @param engine - Engine that will encrypt and/or decrypt the date between the other peer'and this peer
     *
     * @return The same buffer if there is no space problem or a new buffer with the same data but more space.
     *
     * */
    protected ByteBuffer enlargePacketBuffer(ByteBuffer buffer, SSLEngine engine) {
        return  enlargeBuffer(buffer, engine.getSession().getPacketBufferSize());
    }

    protected ByteBuffer enlargeApplicationBuffer(ByteBuffer buffer, SSLEngine engine) {
        return  enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }

    /**
     * Closes a connection
     *
     * @param socketChannel - SocketChannel to communicate between this peer and the other peer
     * @param engine - Engine that will encrypt and/or decrypt the date between the other peer'and this peer
     * @throws Exception if an error occurs.
     *
     * */
    protected void closeConnection(SocketChannel socketChannel, SSLEngine engine) throws IOException {
        // Indicate that application is done with engine
        engine.closeOutbound();

        while (!engine.isOutboundDone()) {
            // Empty the local network packet buffer.
            myNetData.clear();

            // Generate handshaking data
            SSLEngineResult res = engine.wrap(myAppData, myNetData);

            //flipping from reading to writing
            myNetData.flip();

            while (myNetData.hasRemaining()) {
                socketChannel.write(myNetData);
            }

            myNetData.compact();
        }

        // Close transport
        socketChannel.close();

    }

    /**
     * Creates the key managers using a JKS keystore as an input.
     *
     * @param filepath - the path to the JKS keystore.
     * @param keystorePassword - the keystore's password.
     * @param keyPassword - the key's password.
     * @return {@link KeyManager} array.
     * @throws Exception
     */
    protected KeyManager[] createKeyManagers(String filepath, String keystorePassword, String keyPassword) throws Exception {

        // Create and initialize the SSLContext with key material
        char[] passphrase = keystorePassword.toCharArray();

        // First initialize the key material
        KeyStore ksKeys = KeyStore.getInstance("JKS");
        ksKeys.load(new FileInputStream(filepath), passphrase);

        // KeyManagers decide which key material to use
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
        kmf.init(ksKeys, keyPassword.toCharArray());

        return kmf.getKeyManagers();
    }


    /**
     * Creates the trust managers using a JKS keystore as an input.
     *
     * @param filepath - the path to the JKS keystore.
     * @param keystorePassword - the keystore's password.
     * @return {@link TrustManager} array.
     * @throws Exception
     */
    protected TrustManager[] createTrustManagers(String filepath, String keystorePassword) throws Exception {

        // Create and initialize the SSLContext with key material
        char[] passphrase = keystorePassword.toCharArray();

        // First initialize the trust material
        KeyStore ksTrust = KeyStore.getInstance("JKS");
        ksTrust.load(new FileInputStream(filepath), passphrase);

        // TrustManagers decide whether to allow connections
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(ksTrust);

        return tmf.getTrustManagers();
    }



}