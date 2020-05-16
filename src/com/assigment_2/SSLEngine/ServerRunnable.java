package com.assigment_2.SSLEngine;

import com.assigment_2.Chord.Node;

/**
 * A {@link Runnable} with a {@link SSLEngineServer} object.
 *
 * Starts the server by calling {@link SSLEngineServer#start()} in its run method.
 * Provides a stop method which will stop the server by calling {@link SSLEngineServer#stop()}
 *
 * */
public class ServerRunnable implements Runnable {

    /**
     * The SSL server for TLS Protocols that will be run.
     *
     * */
    SSLEngineServer server;

    /**
     * Initiates the server to be run
     * */
    public ServerRunnable(SSLEngineServer server) {
        this.server = server;
    }

    /**
     * Method that runs the server by calling {@link SSLEngineServer#start()}
     * */
    @Override
    public void run() {

        try {
            this.server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that stops the server by making it inactive.
     * */
    public void stop() {
        this.server.stop();
    }
}
