package com.os.pm.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class SynchronizedFileOutputStream  extends OutputStream implements Serializable {
	
	
	
	private static final long serialVersionUID = -2615966902694975193L;
	private File dst = null;
	private long pos = 0;
	private transient RandomAccessFile hdl = null;
	private boolean migrating = false;	
	private boolean isWriting = false;
	
	public SynchronizedFileOutputStream(String path) throws IOException {
		this.dst = new File(path);
		/* to allow all other users can write */
		Runtime.getRuntime().exec("chmod 777 " + path);
        this.pos = 0;
        this.migrating = false;
	}
	
	@Override
    public synchronized void write(int b) throws IOException {
		/* if is migrating, wait */
		while (migrating == true) {
			println("waiting for completion of migration");
			try {
				wait();
			} catch(InterruptedException e) { } 
			finally { }
		}
		
		/* set the reading lock to make sure it won't enter migrating mode */
		setWriting();
		
        if (hdl == null) {
        	hdl = new RandomAccessFile(dst, "rw");
        	hdl.seek(pos);
        }
        hdl.write(b);
        pos++;
        
        /* notifyAll other waiting threads before migrating */
		notifyAll();
		
		/* reset the reading lock to make it ready to enter migrating mode */
		resetWriting();
    }
	
	/* suspend before migrate */
	public synchronized void suspend() 
			throws IOException {
		/* ensure one instance is suspended only once */
		if (migrating == true) {
			return;
		}
		migrating = true;
		
		/* ensure no writing operation is working */
		while (isWriting == true) {
			println("waiting for writing lock");
			try{
				wait();
			} catch(InterruptedException e) { } 
			finally { }
		}
		
		close();
		hdl = null;
		
		println("out stream suspended");
	}
	
	/* resume after migrate */
	public synchronized void resume() 
			throws IOException {
		/* resuming a non-migrating stream is meaningless */
		if (migrating == false) {
			println("WARNING: try to resume a non-migrating out stream!");
			return;
		}
		
		hdl = new RandomAccessFile(dst, "rw");
    	hdl.seek(pos);
		
		/* mark the end of the migration and notifyAll other waiting threads */
		migrating = false;
		notifyAll();
		
		println("out stream resumed");
	}
	
	/**
     * close the handle
     */
    @Override
    public void close() throws IOException {
    	if(hdl != null) {
    		hdl.close();
    	}
    }
    
    public void setWriting() {
		isWriting = true;
	}
	public synchronized void resetWriting() {
		isWriting = false;
		notifyAll();
	}
	
	private void println(String msg) {
		System.out.println("SynchronizedFileOutputStream: " + msg);
	}
}