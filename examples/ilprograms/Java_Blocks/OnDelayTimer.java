import java.util.Timer;
import java.util.TimerTask;


public class OnDelayTimer{
	Timer timer = new Timer();
	private long zeroTime;
	private boolean prevIN;

	public boolean tonIN;
	public int tonPT;
	public int tonET;
	public boolean tonQ;



	OnDelayTimer() {
	}


	public int gettonET() {
		/* Om vi befinner oss i fasen d� IN �r true och tiden
         * som har g�tt sedan IN sattes till true inte har uppn�tt
         * tonPT, ska denna tid l�ggas in i tonET (rampfunktion)
         */
		if (tonIN && tonET < tonPT) {
			tonET = (int)(System.currentTimeMillis()-zeroTime);
		}

		return tonET;
	}


	public void run() {
		/*
         * En ny timer ska endast startas om insignalen g�r fr�n false
         * till true
         */
		if (!prevIN && tonIN) {


			zeroTime = System.currentTimeMillis();
			timer = new Timer();

			timer.schedule(new DelayTask(), tonPT);
		}

		prevIN = tonIN;

		if (tonIN) {
			tonET = gettonET();
		} else {
			/* Om tonIN �r false, stoppa timern *
			 * och s�tt tonET till 0            */
			timer.cancel();
			tonQ = tonIN;
			tonET = 0;
		}

	}


	class DelayTask extends TimerTask {
		public void run() {
			/* N�r tiden specificerad av tonPT har g�tt,
			   s�tts utv�rdet tonQ till inv�rdet tonIN */
			tonQ = tonIN;
			tonET = tonPT;

			/* Terminate the timer thread */
			timer.cancel();
		}
	}

}
