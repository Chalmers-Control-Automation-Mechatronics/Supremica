import java.util.Timer;
import java.util.TimerTask;


public class OnDelayTimer{
	Timer timer = new Timer();
	private long zeroTime;
	private boolean prevIN;

	public boolean IN;
	public int PT;
	public int ET;
	public boolean Q;



	OnDelayTimer() {
	}


	public int getET() {
		/* Om vi befinner oss i fasen d� IN �r true och tiden
         * som har g�tt sedan IN sattes till true inte har uppn�tt
         * PT, ska denna tid l�ggas in i ET (rampfunktion)
         */
		if (IN && ET < PT) {
			ET = (int)(System.currentTimeMillis()-zeroTime);
		}

		return ET;
	}


	public void run() {
		/*
         * En ny timer ska endast startas om insignalen g�r fr�n false
         * till true
         */
		if (!prevIN && IN) {


			zeroTime = System.currentTimeMillis();
			timer = new Timer();

			timer.schedule(new DelayTask(), PT);
		}

		prevIN = IN;

		if (IN) {
			ET = getET();
		} else {
			/* Om IN �r false, stoppa timern *
			 * och s�tt ET till 0            */
			timer.cancel();
			Q = IN;
			ET = 0;
		}

	}


	class DelayTask extends TimerTask {
		public void run() {
			/* N�r tiden specificerad av PT har g�tt,
			   s�tts utv�rdet Q till inv�rdet IN */
			Q = IN;
			ET = PT;

			/* Terminate the timer thread */
			timer.cancel();
		}
	}

}
