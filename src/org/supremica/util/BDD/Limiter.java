package org.supremica.util.BDD;

/**
 * used to limit the execution time of algorithms or make them interruptible.
 *
 *
 */
public class Limiter
{
	private static boolean global_stop = false;
	private boolean has_stopped;
	private long end_time, maxtime;
	private static Limiter no_limiter = new Limiter(-1);    // dont limit time, only global interrupts

	// -------------------------------------------------------------------
	// for global interrupts
	public static void stopAll()
	{
		global_stop = true;
	}

	public static boolean allStopped()
	{
		return global_stop;
	}

	// ----------------------------------------------------------
	// a factory function:
	public static Limiter createNew()
	{
		if (Options.algo_time_limit <= 0)
		{
			return no_limiter;
		}

		return new Limiter(Options.algo_time_limit);
	}

	// ----------------------------------------------------------

	/**
	 * create a new limiter with the "maxtime" running time (in seconds)
	 *
	 *
	 */
	public Limiter(long maxtime)
	{
		this.maxtime = maxtime * 1000;    // make it in milliseconds

		reset();
	}

	/**
	 *
	 * reset the timer and start all over
	 *
	 */
	public void reset()
	{
		has_stopped = false;
		global_stop = false;    // XXX: RACE CONDITION, affects parallell processes!

		if (maxtime < 0)
		{
			end_time = -1;    // disabled
		}
		else
		{
			end_time = maxtime + System.currentTimeMillis();
		}
	}

	/**
	 * have we reached our limit or has the algorithm been stopped from somewhere else?
	 *
	 *
	 */
	public boolean stopped()
	{
		if (global_stop)
		{
			return true;
		}

		if ((end_time > 0) && (end_time < System.currentTimeMillis()))
		{
			if (!has_stopped)
			{
				String txt = "\n\n*** Algorithm stopped due to time limit ***\n\n";

				System.err.println(txt);
				System.out.println(txt);
				Options.out.println(txt);

				has_stopped = true;
			}

			return true;
		}

		return false;
	}

	/**
	 * returns true if the algorithm has been stopped due to time limit
	 *
	 */
	public boolean wasStopped()
	{
		return has_stopped;
	}
}
