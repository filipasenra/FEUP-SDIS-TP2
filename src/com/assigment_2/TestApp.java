package com.assigment_2;

import java.util.Arrays;

/*
 * Testing Client Application
 * */
public class TestApp {

    public static void main(String[] args) {

        if(!parseArgs(args))
            System.exit(-1);

    }

    private static boolean parseArgs(String[] args) {

        if(args.length < 2)
        {
            System.err.println("usage: TestApp <rmi_peer_ap> <sub_protocol> <arguments_of_protocol>\n" +
                    "Protocols available: BACKUP, RESTORE, DELETE, RECLAIM, STATE");
            return false;
        }

        String rmi_peer_ap = args[0];
        String sub_protocol = args[1];
        String[] arguments = Arrays.copyOfRange(args, 2, args.length);

        TestAppHandler testClientHandler = new TestAppHandler(rmi_peer_ap);

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
