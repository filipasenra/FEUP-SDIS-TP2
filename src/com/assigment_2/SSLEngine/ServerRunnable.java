package com.assigment_2.SSLEngine;

public class ServerRunnable implements Runnable {

    SSLEngineServer server;

    public ServerRunnable(SSLEngineServer server) {

        this.server = server;

    }

    @Override
    public void run() {

        try {
            this.server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.server.stop();
    }
}
