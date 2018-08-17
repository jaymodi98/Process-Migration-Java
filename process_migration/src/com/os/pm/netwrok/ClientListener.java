package com.os.pm.netwrok;

import java.io.IOException;
import java.net.Socket;

public class ClientListener extends Thread{
	// create a socket to receive messages from clients
	private Socket socket = null;
	
	public ServerManager svrMgr = null;
	
	public ClientListener(ServerManager svrMgr, Socket socket) {
		this.svrMgr = svrMgr;
		this.socket = socket;
	}
	
	//to receive messages
	@Override
	public void run() {
		while (true) {
			try {
				svrMgr.msgReceive(socket);
			} catch (IOException | ClassNotFoundException e) {
				svrMgr.clientDisconnected(socket);
				break;
			}
		}
		
	}
	
	
}
