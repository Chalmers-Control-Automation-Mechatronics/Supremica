package org.supremica.util.BDD;

public class Timer
{
	private static final long MUL_SEC = 1000, MUL_MIN = MUL_SEC * 60,
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
		if (!Options.profile_on)
		{
			return;    // No output anyway, forget it
		}

		long t = getElapsed();

		if ((t <= 0) && (must_report == false))
		{
			return;
		}

		Options.out.print("--> ");

		if (name != null)
		{
			Options.out.print('[' + name + "] ");
		}

		Options.out.print(str);

		if (t > 0)
		{
			Options.out.print(" in ");
			showTime(t);
			Options.out.println();
		}
		else
		{
			Options.out.println(" (not measureable)");
		}
	}

	private void showTime(long t)
	{
		if (t > MUL_HOUR * MAX_HOUR)
		{
			Options.out.print(t / MUL_DAY + " days");
		}
		else if (t > MUL_MIN * MAX_MIN)
		{
			Options.out.print(t / MUL_HOUR + " hours");
		}
		else if (t > MUL_SEC * MAX_SEC)
		{
			Options.out.print(t / MUL_MIN + " minutes");
		}
		else if (t > MAX_MILLISEC)
		{
			Options.out.print(t / MUL_SEC + " seconds");
		}
		else
		{
			Options.out.print(t + " milliseconds");
		}
	}
}
