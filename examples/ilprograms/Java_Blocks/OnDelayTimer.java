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
		/* Om vi befinner oss i fasen då IN är true och tiden
         * som har gått sedan IN sattes till true inte har uppnått
         * tonPT, ska denna tid läggas in i tonET (rampfunktion)
         */
		if (tonIN && tonET < tonPT) {
			tonET = (int)(System.currentTimeMillis()-zeroTime);
		}

		return tonET;
	}


	public void run() {
		/*
         * En ny timer ska endast startas om insignalen går från false
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
			/* Om tonIN är false, stoppa timern *
			 * och sätt tonET till 0            */
			timer.cancel();
			tonQ = tonIN;
			tonET = 0;
		}

	}


	class DelayTask extends TimerTask {
		public void run() {
			/* När tiden specificerad av tonPT har gått,
			   sätts utvärdet tonQ till invärdet tonIN */
			tonQ = tonIN;
			tonET = tonPT;

			/* Terminate the timer thread */
			timer.cancel();
		}
	}

}
