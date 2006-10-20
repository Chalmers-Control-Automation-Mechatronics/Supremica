//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.springembedder
//# CLASS:   SpringAbortDialog
//###########################################################################
//# $Id: SpringAbortDialog.java,v 1.2 2006-10-20 05:20:55 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.springembedder;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.border.Border;


public class SpringAbortDialog
  extends JDialog
{
  
  //#########################################################################
  //# Constructors
  public SpringAbortDialog(final Frame owner,
			   final String name,
			   final SpringEmbedder embedder,
			   final long timeout)
  {
    super(owner, "Layouting " + name + " ...");
    mEmbedder = embedder;

    final Border border1 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    mProgress = new JProgressBar(0, SpringEmbedder.NUM_PASSES + 1);
    mProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
    mProgress.setBorder(border1);
    final Border border2 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    final JButton abort = new JButton("Abort");
    abort.setAlignmentX(Component.CENTER_ALIGNMENT);
    abort.setBorder(border2);
    abort.addActionListener(new ActionListener()
      {
	public void actionPerformed(final ActionEvent event) {
	  mEmbedder.stop();
	}
      });
    final Box box = new Box(BoxLayout.Y_AXIS);
    box.add(mProgress);
    box.add(abort);
    add(box);

    final RefreshTask task = new RefreshTask(timeout);
    mTimer = new Timer(false);
    mTimer.schedule(task, TIME_PERIOD, TIME_PERIOD);

    pack();
  }


  //#########################################################################
  //# Inner Class RefreshTask
  private class RefreshTask extends TimerTask
  {
    //#########################################################################
    //# Constructors
    private RefreshTask(final long timeout)
    {
      mStopTime = timeout >= 0 ? System.currentTimeMillis() + timeout : -1;
    }

    //#########################################################################
    //# Overrides for Abstract Base Class TimerTask
    public void run()
    {
      if (mEmbedder.isFinished()) {
	mTimer.cancel();
	dispose();
      } else if (mStopTime >= 0 && System.currentTimeMillis() >= mStopTime) {
	mEmbedder.stop();
      } else {
	mProgress.setValue(mEmbedder.getProgress());
      }
    }

    //#######################################################################
    //# Data Members
    private long mStopTime;
  }


  //#########################################################################
  //# Data Members
  private final JProgressBar mProgress;
  private final Timer mTimer;
  private final SpringEmbedder mEmbedder;


  //#########################################################################
  //# Class Constants
  private static final long TIME_PERIOD = 100;

}
