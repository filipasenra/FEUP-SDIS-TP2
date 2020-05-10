package com.assigment_2.Protocol;

import com.assigment_2.Pair;
import com.assigment_2.PeerClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class RestoreFileThread implements Runnable {
    String fileId;
    String filepath;
    int numChunks;

    public RestoreFileThread(String fileId, String filepath, int numChunks) {
        this.filepath = filepath;
        this.numChunks = numChunks;
        this.fileId = fileId;
    }

    public String getRecoveredName() {
        String extension;
        String name;
        String recovered;

        int j = filepath.lastIndexOf('.');
        int indexOfFileName = filepath.lastIndexOf('/');

        indexOfFileName++;

        if (j > 0) {
            extension = filepath.substring(j + 1);
            name = filepath.substring(indexOfFileName, j);
            recovered = name + "_recovered" + "." + extension;
        } else
            recovered = filepath + "_";

        return recovered;
    }

    @Override
    public void run() {

        String recovered = getRecoveredName();
        String full_path = "1/" + recovered;

        File file = new File(full_path);

        if (file.exists()) {
            file.delete();
        }

        for (int i = 0; i < numChunks; i++) {

            Pair<String, Integer> pair = new Pair<>(fileId, i);

            if (!PeerClient.getStorage().getRecoveredChunks().containsKey(pair)) {
                System.out.println("Impossible to restore file because some chunks are missing!: " + pair);
                return;
            } else {

                byte[] chunk = PeerClient.getStorage().getRecoveredChunks().get(pair);
                PeerClient.getStorage().getRecoveredChunks().remove(pair);

                try {

                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }

                    FileOutputStream fos = new FileOutputStream(full_path, true);
                    fos.write(chunk);

                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println(" > File recovered successfully!");
    }
}
