/********************** Monitor.java ************************/

// Class for doing usable progress monitoring
// See also Monitor/Monitor.java
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.Monitorable;

public class Monitor
	implements ActionListener
{
	private ProgressMonitor pm;
	private Timer timer;
	private Object message;
	private String note;
	private MonitorableThread task;

	public Monitor(Object message, String note, MonitorableThread task)
	{
		this.message = message;
		this.note = note;
		this.task = task;
	}

	public void startMonitor(Component c, int millisToDecideToPopup, int millisBetweenTimerFire)
	{
		pm = new ProgressMonitor(c, message, note, 0, 100);    // Note, we talk in percentage here!

		pm.setProgress(0);    // force popup
		pm.setMillisToDecideToPopup(millisToDecideToPopup);

		timer = new Timer(millisBetweenTimerFire, this);
		timer.start();
	}

	// called each time the Timer fires
	public void actionPerformed (ActionEvent event)
	{
		if(/* task.wasStopped() */ !task.isAlive() || pm.isCanceled())
		{
			pm.close();
			timer.stop();
			if(/* !task.wasStopped() */ task.isAlive())
			{
				task.stopTask(); // interrupt
				// task.join(); // ??
			}
		}
		else
		{
			pm.setNote(task.getActivity());
			pm.setProgress(task.getProgress());
		}
	}
}
