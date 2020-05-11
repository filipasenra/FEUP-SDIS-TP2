package com.assigment_2;

import java.util.Arrays;

/*
 * Testing Client Application
 * */
public class TestApp {

    public static void main(String[] args) throws Exception {

        if(!parseArgs(args))
            System.exit(-1);

    }

    private static boolean parseArgs(String[] args) throws Exception {

        if(args.length < 3)
        {
            System.err.println("usage: TestApp <address> <port> <sub_protocol> <arguments_of_protocol>\n" +
                    "Protocols available: BACKUP, RESTORE, DELETE, RECLAIM, STATE");
            return false;
        }

        String address = args[0];
        int port = Integer.parseInt(args[1]);
        String sub_protocol = args[2];
        String[] arguments = Arrays.copyOfRange(args, 3, args.length);

        TestAppHandler testClientHandler = new TestAppHandler("TLSv1.2", address, port);

        switch (sub_protocol){
            case "BACKUP":
                return testClientHandler.doBackup(arguments);
            case "RESTORE":
                 return testClientHandler.doRestore(arguments);
            case "DELETE":
                return testClientHandler.doDeletion(arguments);
            case "RECLAIM":
                return testClientHandler.doReclaim(arguments);
            case "STATE":
                return testClientHandler.doState(arguments);
            default:
                System.err.println("NOT A VALID PROTOCOL");
                return false;
        }

    }

}
