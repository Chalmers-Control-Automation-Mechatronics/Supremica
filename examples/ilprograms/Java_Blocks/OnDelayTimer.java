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
		/* Om vi befinner oss i fasen då IN är true och tiden
         * som har gått sedan IN sattes till true inte har uppnått
         * PT, ska denna tid läggas in i ET (rampfunktion)
         */
		if (IN && ET < PT) {
			ET = (int)(System.currentTimeMillis()-zeroTime);
		}

		return ET;
	}


	public void run() {
		/*
         * En ny timer ska endast startas om insignalen går från false
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
			/* Om IN är false, stoppa timern *
			 * och sätt ET till 0            */
			timer.cancel();
			Q = IN;
			ET = 0;
		}

	}


	class DelayTask extends TimerTask {
		public void run() {
			/* När tiden specificerad av PT har gått,
			   sätts utvärdet Q till invärdet IN */
			Q = IN;
			ET = PT;

			/* Terminate the timer thread */
			timer.cancel();
		}
	}

}
