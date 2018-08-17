package com.os.pm.netwrok;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.os.pm.processes.MigratableProcess;

public class ServerManager extends NetworkManager{
	
	public class MigrateTask {
		int srcCid;
		int srcPid;
		int dstCid;
		MigrateTask(int srcCid, int srcPid, int dstCid) {
			this.srcCid = srcCid;
			this.srcPid = srcPid;
			this.dstCid = dstCid;
		}
	}
	
	private ServerSocket svrSocket = null;
	
	//to manage more clients and to assign them new client id
	private volatile AtomicInteger cid = null;
	
	//to maintain map between client and socket of the client
	private volatile Map<Integer, Socket> clients = null;

	ArrayList<MigrateTask> migrateTasks = null;
	
	public ServerManager(int svrPort) {
		try {
			clients = new ConcurrentSkipListMap<Integer, Socket>();
			cid = new AtomicInteger(0);
			migrateTasks = new ArrayList<MigrateTask>();
			
			svrSocket = new ServerSocket(svrPort);
			
			System.out.println("Waiting for clients...");
			System.out.println("Please connect to " + InetAddress.getLocalHost() + ":" + svrPort + ".");
		} catch (IOException e) {
			System.out.println("ERROR: failed to listen on port " + svrPort);
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				// accepting new clients
				Socket socket = svrSocket.accept();
				addClient(socket);
				System.out.println("New client(cid is " + getCid(socket) + ") connected!");
				
				// create a new instance of ClientListener to receive messages
				new ClientListener(this, socket).start();
				
				// send the client id to the new client
				msgSend(socket, new MessageStructure(5, Integer.valueOf(getCid(socket))));
			} catch (IOException e) {
				// (ServerSocket is closed)
				break;
			}
		}
	}
	
	private void addClient(Socket socket) {
		clients.put(Integer.valueOf(cid.getAndIncrement()), socket);
	}
	

	@Override
	public void msgHandler(Socket src, MessageStructure msg) {
		// TODO Auto-generated method stub
		switch (msg.code) {
		case 0:
			//this message is sent by server to client
			break;
		case 1:
			// show process info from clients
			if (msg.content instanceof ArrayList<?>) {
				ArrayList<ArrayList<String>> proc = (ArrayList<ArrayList<String>>)msg.content;
				displayFromClient(proc, getCid(src));
			}
			break;
		case 2:
			//this message is sent by server to client
			break;
		case 3:
			// send process from one client to another client */
			int cid = getCid(src);
			if (msg.content == null) {
				System.out.println("Client " + cid + " has no such process! Please check the pid again.");
				break;
			}
			if (msg.content instanceof MigratableProcess) {
				migrateToClient((MigratableProcess)msg.content, getCid(src));
			}
			break;
		case 4:
			//this message is sent by server to client
			break;
		default:
			break;
		}
	}
	
	//receive process info from all clients and display it
	private void displayFromClient(ArrayList<ArrayList<String>> proc, int srcCid) {
		if (proc.isEmpty()) {
			System.out.println("Client " + srcCid + " has no running process.");
			return;
		}
		for (ArrayList<String> p : proc) {
			System.out.println("\t" + srcCid + "\t" + p.get(0) + "\t" + p.get(1));
		}
	}
	
	private int getCid(Socket socket) {
		for (Map.Entry<Integer, Socket> entry : clients.entrySet()) {
		    if (entry.getValue() == socket) {
		    	return entry.getKey().intValue();
		    }
		}
		return -1;
	}
	
	//migrate process to another client. migratetask's arraylist is used to find destination client
	private void migrateToClient(MigratableProcess mp, int srcCid) {
		for (MigrateTask i: migrateTasks) {
			if (i.srcCid==srcCid && i.srcPid==mp.pid) {
				try {
					Socket dst = getClient(i.dstCid);
					if (dst == null) {
						System.out.println("Connection to " + i.dstCid + " is broken! Cannot migrate process to it. Process lost.");
						return;
					}
					
					msgSend(dst, new MessageStructure(4, mp));
				} catch (IOException e) {
					System.out.println("Connection to " + i.dstCid + " is broken! Cannot migrate process to it. Process lost.");
				}
				System.out.println("Migrate process successfully to client " + i.dstCid + ".");
				break;
			}
		}
	}
	
	private Socket getClient(int cid) {
		return (Socket)clients.get(Integer.valueOf(cid));
	}
	
	public void showClientProcesses() {
		System.out.println("Processes running on all clients: ");
		System.out.println("\tCID\tPID\tCLASSNAME");
		MessageStructure msg = new MessageStructure(0, null);
		
		for (Map.Entry<Integer, Socket> entry : clients.entrySet()) {
		    try {
				msgSend(entry.getValue(), msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void sendMigrationRequest(int srcCid, int srcPid, int dstCid) {
		Socket socket = getClient(srcCid);
		if (socket == null) {
			System.out.println("Cannot migrate. Client " + srcCid + " is not available!");
			return;
		}
		if (getClient(dstCid) == null) {
			System.out.println("Cannot migrate. Client " + dstCid + " is not available!");
			return;
		}
		
		
		migrateTasks.add(new MigrateTask(srcCid, srcPid, dstCid));
		try {
			msgSend(socket, new MessageStructure(2, Integer.valueOf(srcPid)));
		} catch (IOException e) {
			System.out.println("ERROR: Connection with " + srcCid + " is broken, message cannot be sent!");
			return;
		}
	}
	
	public void close() {
		System.out.println("Server is about to close. All connected clients will exit.");
		try {
			svrSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Bye~");
	}
	
	public void clientDisconnected(Socket client) {
		int cid = getCid(client);
		System.out.println("Client " + cid + " has disconnected.");
		
		//deleteClient(cid);
		if (clients.remove(Integer.valueOf(cid)) == null) {
			System.out.println("delete failed!");
			
		}
	}
	
	/*private boolean deleteClient(int idx) {
		if (_clients.remove(Integer.valueOf(idx)) == null) {
			System.out.println("delete failed!");
			return false;
		}
		return true;
	}*/



}
