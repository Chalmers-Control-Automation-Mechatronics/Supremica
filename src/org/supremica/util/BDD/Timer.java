package org.supremica.util.BDD;

public class Timer
{
	private final long MUL_SEC = 1000, MUL_MIN = MUL_SEC * 60,
					   MUL_HOUR = MUL_MIN * 60, MUL_DAY = MUL_HOUR * 24,
					   MAX_MILLISEC = 50000, MAX_SEC = 360, MAX_MIN = 80, MAX_HOUR = 36
	;
	private long clock;
	private String name;

	public Timer()
	{
		this(null);
	}

	public Timer(String name)
	{
		this.name = name;

		reset();
	}

	public void reset()
	{
		clock = System.currentTimeMillis();
	}

	public long getElapsed()
	{
		return System.currentTimeMillis() - clock;
	}

	public void report(String str)
	{
		report(str, true);
	}

	public void report(String str, boolean must_report)
	{
		if (!Options.debug_on)
		{
			return;    // No output anyway, forget it
		}

		long t = getElapsed();

		if ((t <= 0) && (must_report == false))
		{
			return;
		}

		System.out.print("--> ");

		if (name != null)
		{
			System.out.print('[' + name + "] ");
		}

		System.out.print(str);

		if (t > 0)
		{
			System.out.print(" in ");
			showTime(t);
			System.out.println();
		}
		else
		{
			System.out.println(" (not measureable)");
		}
	}

	private void showTime(long t)
	{
		if (t > MUL_HOUR * MAX_HOUR)
		{
			System.out.print(t / MUL_DAY + " days");
		}
		else if (t > MUL_MIN * MAX_MIN)
		{
			System.out.print(t / MUL_HOUR + " hours");
		}
		else if (t > MUL_SEC * MAX_SEC)
		{
			System.out.print(t / MUL_MIN + " minutes");
		}
		else if (t > MAX_MILLISEC)
		{
			System.out.print(t / MUL_SEC + " seconds");
		}
		else
		{
			System.out.print(t + " milliseconds");
		}
	}
}
