package com.os.pm.main;

import java.util.Scanner;

import com.os.pm.processes.ProcessManager;

public class ProcMigMain {
	
	private static final String DEFAULT_SERVER_ADDR = "localhost";
	private static final int DEFAULT_PORT = 1234;

	public static void main(String[] args) {
		System.out.println("process_migration");
		
		System.out.println("Please choose a role you want to be: server or client.");
		System.out.println("server PORT - The port to listen to. 1234 is set default if not specified.");
		System.out.println("client SERVER_ADDRESS PORT - The server address and port to connect to. localhost:1234 is set default if not specified.");
		System.out.print("> ");

		Scanner in = new Scanner(System.in);
		String line = in.nextLine();
		String[] role = line.split("\\s+");
		
		if (role[0].equalsIgnoreCase("server")) {
			//acts as server
			int port = DEFAULT_PORT;
			if (role.length > 1) {
				try {
					port = Integer.parseInt(role[1]);
				} catch(NumberFormatException e) {
					System.out.println("Error: port is not a number!");
					in.close();
					return;
				}
			}
			
			ServerOperator serverOp = new ServerOperator(port);
			serverOp.startServer();
		} else if (role[0].equalsIgnoreCase("client")) {
			/* work as client */
			String svrAddr = DEFAULT_SERVER_ADDR;
			int port = DEFAULT_PORT;
			if (role.length > 2) {
				try {
					svrAddr = role[1];
					port = Integer.parseInt(role[2]);
				} catch(NumberFormatException e) {
					System.out.println("Error: port is not a number!");
					in.close();
					return;
				}
			}
			
			ProcessManager client  = new ProcessManager(svrAddr, port);
			client.startClient();
		} else {
			error();
			in.close();
			return;
		}
		in.close();
	}
	
	public static void error() {
		System.out.println("Restart and selct role as server or client.");
		System.exit(0);
	}
}
