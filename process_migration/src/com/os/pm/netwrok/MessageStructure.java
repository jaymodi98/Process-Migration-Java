package com.os.pm.netwrok;

import java.io.Serializable;


/*
 * 	msgType		Description					
 * 	0			Server requests for process info from clients
 * 	1			Clients respond process info
 * `2			Server requests the clients to migrate process
 * 	3			Client emigrates the process to server
 * 	4			Server immigrates the process to a client
 * 	5			Server sets the client id
 */
public class MessageStructure extends Object implements Serializable {

	private static final long serialVersionUID = 2127267870016120426L;
	public int code;
	public Object content;
	
	public MessageStructure()
	{
		this.code=0;
		this.content=null;
	}
	
	public MessageStructure (int code, Object content)
	{
		this.code=code;
		this.content=content;
	}
	
}
