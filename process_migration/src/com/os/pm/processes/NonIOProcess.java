package com.os.pm.processes;

public class NonIOProcess extends MigratableProcess{
	
	
	private static final long serialVersionUID = 4649901214913598949L;
	public int cnt;
	public volatile boolean suspended;
	
	public NonIOProcess(String[] str) {
		this.suspended = false;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Process "+pid+" run starts: cnt="+cnt);
		while(!suspended) 
		{
			try {
				Thread.sleep(500);
				cnt++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		suspended = false;
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		System.out.println("Process "+pid+" suspended: cnt="+cnt);
		suspended=true;
		while(suspended);
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		System.out.println("Process "+pid+" resumes: cnt="+cnt);
		suspended=false;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName());
        sb.append("(" + pid + "): ");        
        return sb.toString();
	}

}
