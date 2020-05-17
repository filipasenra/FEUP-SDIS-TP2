package com.assigment_2.SSLEngine;

import javax.net.ssl.SSLEngine;
import java.nio.channels.SocketChannel;

public interface MessagesHandler {

    public void run(SocketChannel socketChannel, SSLEngine engine, byte[] message);
}
