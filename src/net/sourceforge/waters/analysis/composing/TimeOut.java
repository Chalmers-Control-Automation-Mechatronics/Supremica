package net.sourceforge.waters.analysis.composing;

public class TimeOut extends Thread {	

	public TimeOut (int length) {
		m_length = length;
		m_elapsed = 0;
	}
	
	public synchronized void reset() {
		m_elapsed = 0;
	}
	
	public void run()	{		
		while (true) {			
			try	{ 
				Thread.sleep(m_rate);
				//System.out.println("Sleeping..."+m_elapsed);
			}catch (InterruptedException ioe) {
				return;
			}
			
			synchronized (this) {				
				m_elapsed += m_rate;
				// Check to see if the time has been exceeded
				if (m_elapsed > m_length){					
					System.err.println ("Timeout occurred.... terminating");
					System.exit(1);
				}
			}
		}
	}
	
	protected long m_rate = 100;
	private long m_length;
	private long m_elapsed;
}
