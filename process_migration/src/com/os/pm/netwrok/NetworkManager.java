package com.os.pm.netwrok;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * NetworkManager
 * 
 * Base class of ServerManager and ClientManager to provide network operations.
 *
 */
public abstract class NetworkManager implements Runnable{
	
	public void msgSend(Socket socket, MessageStructure msg) throws IOException
	{
		ObjectOutputStream outStream;
		
		outStream= new ObjectOutputStream(socket.getOutputStream());
		outStream.writeObject(msg);
	}
	
	public void msgReceive(Socket socket) throws IOException, ClassNotFoundException
	{
		ObjectInputStream inStream;
		
		inStream = new ObjectInputStream(socket.getInputStream());
		Object inObj = inStream.readObject();
		
		if (inObj instanceof MessageStructure) 
		{
			MessageStructure msg = (MessageStructure) inObj;
			msgHandler(socket,msg);
		}
	}
	
	/*
	 * An interface for ServerManager and ClientManager to implement, handling all
	 * the incoming messages.
	 */
	public abstract void msgHandler(Socket socket,MessageStructure msg);
	
	public void close(Socket socket)
	{
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
