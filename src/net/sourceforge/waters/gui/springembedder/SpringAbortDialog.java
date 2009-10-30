//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.springembedder
//# CLASS:   SpringAbortDialog
//###########################################################################
//# $Id$
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
  implements ActionListener, EmbedderObserver
{
  
  //#########################################################################
  //# Constructors
  public SpringAbortDialog(final Frame owner,
			   final String name,
			   final SpringEmbedder embedder,
			   final long timeout)
  {
    super(owner, "Layouting " + name + " ...");
    mTimeout = timeout;
    mTimer = new Timer();
    mEmbedder = embedder;
    mEmbedder.addObserver(this);
    final Border border1 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    mProgress = new JProgressBar(0, SpringEmbedder.getMaxProgress());
    mProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
    mProgress.setBorder(border1);
    final Border border2 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    final JButton abort = new JButton("Abort");
    abort.setAlignmentX(Component.CENTER_ALIGNMENT);
    abort.setBorder(border2);
    abort.addActionListener(this);
    final Box box = new Box(BoxLayout.Y_AXIS);
    box.add(mProgress);
    box.add(abort);
    add(box);
    pack();
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    mEmbedder.stop();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.springembedder.EmbedderObserver
  public void embedderChanged(final EmbedderEvent event)
  {
    switch (event.getType()) {
    case EMBEDDER_START:
      final TimerTask task = new AbortTask();
      mTimer.schedule(task, mTimeout);
      break;
    case EMBEDDER_STOP:
      mTimer.cancel();
      mEmbedder.removeObserver(this);
      dispose();
      break;
    case EMBEDDER_PROGRESS:
      mProgress.setValue(mEmbedder.getProgress());
      break;
    default:
      throw new IllegalArgumentException
        ("Unknown embedder event: " + event.getType() + "!");
    }
  }


  //#########################################################################
  //# Inner Class AbortTask
  private class AbortTask extends TimerTask
  {

    //#########################################################################
    //# Overrides for Abstract Base Class TimerTask
    public void run()
    {
      mEmbedder.stop();
    }

  }


  //#########################################################################
  //# Data Members
  private final long mTimeout;
  private final Timer mTimer;
  private final JProgressBar mProgress;
  private final SpringEmbedder mEmbedder;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
