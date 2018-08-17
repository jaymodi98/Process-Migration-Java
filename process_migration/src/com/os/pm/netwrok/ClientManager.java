package com.os.pm.netwrok;

import java.io.IOException;
import java.net.Socket;

import com.os.pm.processes.MigratableProcess;
import com.os.pm.processes.ProcessManager;

public class ClientManager extends NetworkManager {
	
	Socket socket = null;
	public ProcessManager procMgr = null;

	public ClientManager(String addr, int port) {
		try {
			socket = new Socket(addr, port);
			System.out.println("Connected to server: " + addr + ", PORT:" + port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Cannot connect to server " + addr + ", PORT:" + port);
			e.setStackTrace(e.getStackTrace());
			System.exit(0);
		}

	}

	@Override
	public void msgHandler(Socket socket, MessageStructure msg) {
		// TODO Auto-generated method stub
		switch(msg.code)
		{
		case 0:
			// send process info to server
			procMgr.displayToServer();
			break;
		case 1:
			//this message is sent by client to server
			break;
		case 2:
			//server requesting to migrate a process
			if (msg.content instanceof Integer) {
				int pid = ((Integer)msg.content).intValue();
				System.out.println("\nServer is requesting to emigrate process " + pid);
				procMgr.emigrateToServer(pid);
				
			}
			
			break;
		case 3:
			//this message is sent by client to server
			break;
		case 4:
			//immigrating process sent from server
			if (msg.content instanceof MigratableProcess) {
				procMgr.immigrateFromServer((MigratableProcess)msg.content); //Resuming received migrated process
			}
			break;
		case 5:
			//get client id from server
			if (msg.content instanceof Integer) {
				procMgr.cmd = "#" + ((Integer)msg.content).intValue() + " > ";
			}
		default:
			break;
		}
		
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				msgReceive(socket);
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Connection to server is broken. Restart the client.");
				close(socket);
				System.exit(-1);
			}
		}
	}
	
	
	public void msgSend(MessageStructure msg) throws IOException {
		msgSend(socket,msg);
	}
	
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
