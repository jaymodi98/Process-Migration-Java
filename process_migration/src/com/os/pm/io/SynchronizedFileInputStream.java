package com.os.pm.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;



public class SynchronizedFileInputStream extends InputStream implements Serializable {
	private static final long serialVersionUID = 8418840253669323271L;

	private static final String TAG = SynchronizedFileInputStream.class.getSimpleName();
	
	
	private File src;
	private long pos;
	private transient RandomAccessFile hdl;
	private boolean migrating;
	private boolean isReading = false;
	
	public SynchronizedFileInputStream(String path) throws IOException {
		this.src = new File(path);
        this.pos = 0;
        this.migrating = false;
	}
	
	/* read in one byte */
	@Override
    public synchronized int read() 
    		throws IOException {
		/* if is migrating, wait */
		while (migrating == true) {
			println("waiting for completion of migration");
			try {
				wait();
			} catch(InterruptedException e) { } 
			finally { }
		}
		
        /* set the reading lock to make sure it won't enter migrating mode */
		setReading();
		
		if (hdl == null) {
            hdl = new RandomAccessFile(src, "r");
            hdl.seek(pos);
        }
		
        int result = hdl.read();
        pos++;
				
        /* notify other waiting threads before migrating */
		notify();
		
		/* reset the reading lock to make it ready to enter migrating mode */
		resetReading();
        
        return result;
    }
	
	/* suspend before migrate */
	public synchronized void suspend() 
			throws IOException {
		/* ensure one instance is suspended only once */
		if (migrating == true) {
			println("WARNING: try to suspend a suspended in stream!");
			return;
		}
		
		/* ensure no reading operation is working */
		while (isReading == true) {
			println("waiting for reading lock");
			try{
				wait();
			} catch(InterruptedException e) { } 
			finally { }
		}
		migrating = true;
		
		close();
		hdl = null;
		
		println("in stream suspended");
	}
	
	/* resume after migrate */
	public synchronized void resume() 
			throws IOException {
		/* resuming a non-migrating stream is meaningless */
		if (migrating == false) {
			return;
		}
		
		hdl = new RandomAccessFile(src, "r");
        hdl.seek(pos);
		
		/* mark the end of the migration and notify other waiting threads */
		migrating = false;
		notify();
		
		println("in stream resumed");
	}
	
	private void println(String msg) {
		System.out.println(TAG + ": " + msg);
	}
	
	@Override
    public void close() throws IOException {
		if(hdl != null) {
    	    hdl.close();
		}
    }
	
	private void setReading() {
		isReading = true;
	}

	private synchronized void resetReading() {
		isReading = false;
		notify();
	}
}
