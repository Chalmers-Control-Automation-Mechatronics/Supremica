
/************************** Presenter.java *************************/
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.MonitorableThread;

abstract public class Presenter
	extends Thread
{
	MonitorableThread task;

	public Presenter(MonitorableThread task)
	{
		this.task = task;
	}

	abstract public void taskFinished();

	abstract public void taskStopped();

	public void run()
	{
		try
		{

			// System.err.println("Presenter::run() - Waiting for " + task.getName() + " to be done");
			task.join();
		}
		catch (InterruptedException excp)
		{
			System.err.println("Exception: " + excp);

			return;
		}

		final Runnable do_finish = new Runnable()
		{
			public void run()
			{
				if (!task.wasStopped())
				{
					taskFinished();
				}
				else
				{
					taskStopped();
				}
			}
		};

		SwingUtilities.invokeLater(do_finish);
	}
}
