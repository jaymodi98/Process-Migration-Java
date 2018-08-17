package com.os.pm.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.os.pm.netwrok.ServerManager;

public class ServerOperator {
	
	// To handle all the network operations
	com.os.pm.netwrok.ServerManager svrMgr = null;
	
	public ServerOperator(int port) {
		// new thread to accept incoming clients 
		svrMgr = new ServerManager(port);
		new Thread(svrMgr).start();
	}

	//this function is used to Accept user input and to execute them.
	public void startServer() {
		System.out.println("Type 'help' for more information");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("> ");
        while (true) {
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException e) {
            	System.out.println("ERROR: read line failed!");
            	return;
            }
            executeCommands(line.split("\\s+"));
            System.out.print("> ");
        }
	}
	
	private void executeCommands(String[] arg) {
		switch(arg[0]) {
		case "migrate":
			if (arg.length != 4) {
				System.out.println("Invalid command format.");
				break;
			}
			migrateProcess(arg);
			break;
		case "ps":
			displayProcesses();
			break;
		case "exit":
			exit();
			break;
		case "help":
			help();
			break;
		default:
			break;	
		}
	}
	
	private void migrateProcess(String[] arg) {
		int srcCid, srcPid, dstCid;
		try {
			srcCid = Integer.parseInt(arg[1]);
			srcPid = Integer.parseInt(arg[2]);
			dstCid = Integer.parseInt(arg[3]);
		} catch (NumberFormatException e) {
			System.out.println("Invalid command format!");
			return;
		}
		
		svrMgr.sendMigrationRequest(srcCid, srcPid, dstCid);
	}
	
	private void displayProcesses() {
		svrMgr.showClientProcesses();
	}
	
	private void exit() {
		svrMgr.close();
		System.exit(0);
	}
	
	private void help() {
		System.out.println("Support input command:");
		System.out.println("\tmigrate SRC_CID SRC_PID DST_CID: ");
		System.out.println("\t\tmigrate the process SRC_PID in client SRC_CID to client DST_CID.");
		System.out.println("\t\tAll three arguments should be specified.");
		System.out.println("\tps:");
		System.out.println("\t\tShow all the processes running on all clients.");
		System.out.println("\thelp:");
		System.out.println("\t\tShow all the accepted input commands.");
		System.out.println("\texit:");
		System.out.println("\t\tClose the server program. All clients connected to this server will");
		System.out.println("\t\tbe CLOSED once server exits.");
		
	}

}
