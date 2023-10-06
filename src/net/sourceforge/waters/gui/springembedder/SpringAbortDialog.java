//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
