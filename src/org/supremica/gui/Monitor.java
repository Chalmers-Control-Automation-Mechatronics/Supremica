
/********************** Monitor.java ************************/

// Class for doing usable progress monitoring
// See also Monitor/Monitor.java
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.Monitorable;

/*
class Task
		extends Thread
		implements Measurable
{
		private int p;
		private int i = 0;
		private boolean mode = false;    // false is "no progressbar"
		private boolean requestStop = false;

		public void run()
		{
				try
				{
						while ((i < 100) &&!requestStop)
						{
								sleep(750);

								i += (int) (Math.random() * 20);
						}

						mode = !mode;

						while ((p < 100) &&!requestStop)
						{
								p += (int) (Math.random() * 20);

								sleep(500);
						}
				}
				catch (InterruptedException iexcp)
				{
						return;
				}
		}

		public int getProgress()
		{
				if (mode)
				{
						return p;
				}
				else
				{
						return 1;    // no progress
				}
		}

		public String getActivity()
		{
				if (mode)
				{
						return ("Building transitions: " + p + "% complete");
				}
				else
				{
						return ("Synchronizing: " + i + " number of states");
				}
		}

		public void stopTask()
		{
				requestStop = true;
		}

		public boolean getMode()
		{
				return mode;
		}
}
*/
public class Monitor
	implements ActionListener
{
	private ProgressMonitor pm;
	private Timer timer;
	private Object message;
	private String note;
	private Monitorable task;

	public Monitor(Object message, String note, Monitorable task)
	{
		this.message = message;
		this.note = note;
		this.task = task;
	}

	public void spawn(final JFrame frame)
	{
		Runnable worker = new Runnable()
		{
			public void run()
			{
				startMonitor(frame);
			}
		};
		Thread thread = new Thread(worker);

		thread.start();
	}

	private void startMonitor(Component c)
	{
		pm = new ProgressMonitor(c, message, note, 0, 100);    // Note, we talk in percentage here!

		pm.setProgress(0);    // force popup
		pm.setMillisToDecideToPopup(0);

		timer = new Timer(1000, this);    // 1 second

		timer.start();
	}

	// called each time the Timer fires
	public void actionPerformed(ActionEvent event)
	{
		int progress = task.getProgress();

		if ((progress >= 100) || pm.isCanceled())
		{
			pm.close();
			timer.stop();

			if (progress < 100)
			{
				task.stopTask();    // interrupt
			}
		}
		else
		{
			pm.setNote(task.getActivity());
			pm.setProgress(progress);
		}
	}

	/*
					public static void main(String[] args)    // demo
					{
									JFrame frame = new JFrame("Monitor Demo");

									frame.setSize(200, 200);
									frame.setVisible(true);

									Task task = new Task();

									task.start();

									Monitor monitor = new Monitor(task);

									// task.start();
									monitor.spawn(frame);

									// task.start();
									frame.setTitle("Back in main thread");

									try
									{
													task.join();
									}
									catch (InterruptedException iexcp) {}

									System.exit(0);
					}
	*/
}
