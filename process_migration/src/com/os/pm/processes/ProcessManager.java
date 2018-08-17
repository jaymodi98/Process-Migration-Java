package com.os.pm.processes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.os.pm.netwrok.ClientManager;
import com.os.pm.netwrok.MessageStructure;

public class ProcessManager {

	private String packageName;
	private ClientManager cltMgr = null;
	public String cmd = "> ";
	
	private volatile Map<Integer, MigratableProcess> processMap = new ConcurrentSkipListMap<Integer, MigratableProcess>();

	private volatile AtomicInteger pidCnt; 	

	//assign new process an id and increment
	public ProcessManager(String svrAddr, int port) {
		pidCnt = new AtomicInteger(0);
		packageName = this.getClass().getPackage().getName();
		cltMgr = new ClientManager(svrAddr, port);
		cltMgr.procMgr = this;
		
		/* new thread to receive msg */
		new Thread(cltMgr).start();
	}
	
	//accept client's inputs and execute
	public void startClient() {
		System.out.println("Type 'help' for more information");
		
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(cmd);
        while (true) {
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException e) {
            	System.out.println("ERROR: read line failed!");
            	return;
            }
            executeCommands(line.split("\\s+"));
            System.out.print(cmd);
        }
	}
	
	private void executeCommands(String[] arg) {
		switch(arg[0]) {
		case "create":
			if (arg.length < 2) {
				System.out.println("Invalid command format.");
				break;
			}
			createProcess(arg);
			break;
		case "call":
			if (arg.length < 3) {
				System.out.println("Invalid command format.");
				break;
			}
			callMethod(arg);
		case "ps":
			display();
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
	
	private void createProcess(String[] str) {
    	String psName = str[1];
    	MigratableProcess ps = null;
		try {
			String[] s = Arrays.copyOfRange(str, 2, str.length);
			System.out.println(packageName+ "." + psName);
			Class<?> cls = Class.forName(packageName+ "." + psName);
			
			Constructor<?> ctor = cls.getConstructor(String[].class);
			
			ps = (MigratableProcess)ctor.newInstance((Object)s);
			
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Class " + psName + " not found.");
			return;
		} catch (NoSuchMethodException e) {
			System.out.println(psName + " should have a constructor with prototype " + psName + "(String[]);");
			return;
		} catch (InvocationTargetException e) {
			System.out.println("Please provide appropriate arguments to the constructor of " + psName + "!");
			return;
		}
		
		addProcess(ps);
		ps.pid = getPid(ps);
		System.out.println(psName + " class has been created. pid: " + getPid(ps));
    	Thread thread = new Thread(ps);
        thread.start();
        display();
	}
	
	private void addProcess(MigratableProcess ps) {
		processMap.put(Integer.valueOf(pidCnt.getAndIncrement()), ps);
	}
	
	private int getPid(MigratableProcess ps) {
		for (Map.Entry<Integer, MigratableProcess> entry : processMap.entrySet()) {
		    if (entry.getValue() == ps) {
		    	return entry.getKey().intValue();
		    }
		}
		return -1;
	}
	
	
	private void display() {
		if (processMap.size() == 0) {
			System.out.println("\tNo process is currently running.\n");
			return;
		}
		System.out.println("\tpid\tClass Name");
		for (Map.Entry<Integer, MigratableProcess> entry : processMap.entrySet()) {
		    System.out.println("\t" + entry.getKey() + "\t" + entry.getValue().getClass().getName());
		}
		System.out.println(" ");
	}
	
	private void callMethod(String[] argv) {
		int pid = 0;
		try {
			pid = Integer.parseInt(argv[1]);
		} catch (NumberFormatException e) {
			System.out.println("Pid is not a number!");
			return;
		}
		MigratableProcess ps = getProcess(pid);
		if (ps == null) {
			System.out.println("Invalid pid!");
			return;
		}
		
		Method method = null;
		try {
			System.out.println( ps.getClass().toString() + " " + argv[2]);
			if(argv.length>3)
				method = ps.getClass().getMethod(argv[2], new Class[]{String[].class} );
			else
				method = ps.getClass().getMethod(argv[2]);
		} catch (NoSuchMethodException e) {
			System.out.println("No such method named " + argv[2] + " found");
			e.printStackTrace();
			return;
		}catch (SecurityException e) {
			e.printStackTrace();
		}
		try {
			System.out.println(method.toString() + " "+ method.getName() +" "+method.getParameterCount());
			if(argv.length>3)
				method.invoke(ps, (Object)Arrays.copyOfRange(argv, 3, argv.length));
			else
				method.invoke(ps);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			System.out.println("Illegal argument!");
			e.printStackTrace();
			return;
		}
		
	}
	
	private MigratableProcess getProcess(int pid) {
		return (MigratableProcess)processMap.get(Integer.valueOf(pid));
	}
	
	public void help() {
		System.out.println("Here are the commands:");
		System.out.println("\tcommand\t\t\tdescription");
		System.out.println("\tcreate CLASSNAME\tcreate a new instance of CLASSNAME");
		System.out.println("\tmigrate PID\t\tmigrate a process with PID to another computer");
		System.out.println("\tps\t\t\tdisplay all the running processes");
		System.out.println("\texit\t\t\texit the program");
	}
	
	private void exit() {
		cltMgr.close();
		System.exit(0);
	}
	
	//now, the methods for client manager
	public void displayToServer() {
		ArrayList<ArrayList<String>> content = new ArrayList<ArrayList<String>>();
		for (Map.Entry<Integer, MigratableProcess> entry : processMap.entrySet()) {
			ArrayList<String> cur = new ArrayList<String>();
			cur.add(String.valueOf(entry.getKey()));
			cur.add(entry.getValue().getClass().getName());
			content.add(cur);
		}
		
		MessageStructure msg = new MessageStructure(1, content);
		try {
			cltMgr.msgSend(msg);
		} catch (IOException e) {
			System.out.println("Connection to server is broken. Restart the client.");
			cltMgr.close();
			System.exit(-1);
		}
	}
	
	public void emigrateToServer(int idx) {
		
		MigratableProcess ps = (MigratableProcess)processMap.get(idx);
		
		if (ps == null) {
			System.out.println("WARNING: try to migrate a non-existing pid(" + idx + ")!");
		} else {
			ps.suspend();
		}
		
		MessageStructure msg  = new MessageStructure(3, ps);
		try {
			cltMgr.msgSend(msg);
		} catch (IOException e) {
			System.out.println("Network problem. Cannot migrate now.");
			if (ps != null) {
				ps.resume();
			}
			return;
		} 
		
		if (ps != null) {
			deleteProcess(idx);
			System.out.println("Process " + idx + " has been emmigrated to server successfully!");
			display();
		}
		
	}
	
	private boolean deleteProcess(int idx) {
		if (processMap.remove(Integer.valueOf(idx)) == null) {
			System.out.println("delete failed!");
			return false;
		}
		return true;
	}
	
	//Resuming received migrated process
	public void immigrateFromServer(MigratableProcess proc) {
		proc.resume();
		new Thread(proc).start();
		
		addProcess(proc);
		proc.pid = getPid(proc);
		System.out.println("New process immigrated! PID: " + getPid(proc));
	}
	


}
