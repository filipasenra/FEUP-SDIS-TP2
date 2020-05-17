package com.assigment_2.Chord;

import javax.net.ssl.SSLEngine;
import java.nio.channels.SocketChannel;

public interface ChordMessagesHandler {

    public void run(SocketChannel socketChannel, SSLEngine engine, byte[] message);
}
