package com.os.pm.processes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.os.pm.io.SynchronizedFileInputStream;
import com.os.pm.io.SynchronizedFileOutputStream;

/**
 * IOProcess
 * 
 * Read input file(shuffled alphabet) by one byte a time, put the character into an array and sort,
 * and then output to a file, in order to test the kind of process using the TransactionIO library.
 * 
 */
public class IOProcess extends MigratableProcess {	
	private static final long serialVersionUID = -5736138960128297174L;
	
	private int readCharNum;
	private int writeCharNum;
	
	private ArrayList<Integer> buffer = new ArrayList<Integer>();
	
	private enum PROCESS { READ, SORT, WRITE, FINISH };	
	private PROCESS proc  = PROCESS.READ;
	
	
	private SynchronizedFileInputStream inputStream = null;
	private SynchronizedFileOutputStream outputStream = null;
	
	private volatile boolean suspending;
	private int id;
	
	private static final int MAXLOOPNUM = 8;
	private int loopNum = 0;
	
	public IOProcess(String[] str) {
		this.suspending = false;
		try {
			File outFile = new File(str[1]);
			outFile.delete();
			
			this.inputStream = new SynchronizedFileInputStream(str[0]);
			this.outputStream = new SynchronizedFileOutputStream(str[1]);	
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		System.out.println("IOProcess : run() begin, readCharNum = " + readCharNum + 
				", writeCharNum = " + writeCharNum + ", proc = " + proc);
		Integer num = 0;
		while(!suspending) {
        	switch(proc) {
        	case READ:
        		try {
					while((num = inputStream.read()) != -1) {
						readCharNum++;
						buffer.add(num);
						//System.out.println(num);
					}
					proc = PROCESS.SORT;
					System.out.println("READ -> SORT");
					Thread.sleep(2000);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
        		break;
        	case SORT:
        		try {
        			Collections.sort(buffer);
            		proc = PROCESS.WRITE;
            		System.out.println("SORT -> WRITE");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}        		
        		break;
        	case WRITE:
        		try {
					for(Integer i : buffer) {
						outputStream.write(i);
						writeCharNum++;
						Thread.sleep(200);
					}
					proc = PROCESS.FINISH;
					System.out.println("WRITE -> FINISH");
					Thread.sleep(500);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				} 
        		break;
        	case FINISH:
        		loopNum++;
				if(loopNum == MAXLOOPNUM) {
					suspending = true;
					System.out.println("JOB FINISHED");
					return;
				}				
				proc = PROCESS.READ;
				System.out.println("FINISH -> READ");
				break;
        	default:
        		break;
        	}
		}
		suspending = false;
	}

	@Override
	public void suspend() {
		System.out.println("IOProcess : suspend(), readCharNum = " + readCharNum + 
				", writeCharNum = " + writeCharNum + ", proc = " + proc);
		
		suspending = true;
		outputStream.resetWriting();
		try {
			inputStream.suspend();
			outputStream.suspend();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void resume() {
		System.out.println("IOProcess : resume()");
		
		try {
			inputStream.resume();
			outputStream.resume();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		suspending = false;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName());
        sb.append("(" + id + "): ");        
        return sb.toString();
	}
}
