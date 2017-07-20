//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.sourceforge.waters.model.analysis.Abortable;


public final class ExecutionDialog
  extends JDialog
  implements ActionListener, Runnable
{

  //#########################################################################
  //# Constructors
  /**
   * Creates dialog box for cancelling the {@link Abortable} objects in the
   * supplied list.
   */
  public ExecutionDialog(final Frame owner,
                         final String title,
                         final List<Abortable> threadsToStop)
  {
    super(owner);
    setVisible(false);
    this.threadsToStop = threadsToStop;
    init(title);
  }

  /**
   * Creates dialog box for cancelling the given single {@link Abortable}.
   */
  public ExecutionDialog(final Frame owner,
                         final String title,
                         final Abortable threadToStop)
  {
    this(owner, title, new ArrayList<Abortable>());
    addThreadToStop(threadToStop);
  }

  private void init(final String title)
  {
    final Rectangle bounds = getOwner().getBounds();
    final int x = bounds.x + (bounds.width - DEFAULT_DIALOG_SIZE.width) / 2;
    final int y = bounds.y + (bounds.height - DEFAULT_DIALOG_SIZE.height) / 2;
    setLocation(x, y);
    setSize(DEFAULT_DIALOG_SIZE);

    setTitle(title);
    setSize(new Dimension(250, 120));
    setResizable(false);

    // Center the window
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);

    final JPanel operationPanel = new JPanel(new GridLayout(2, 1));
    operationHeader = new JLabel();
    operationHeader.setHorizontalAlignment(JLabel.LEFT);
    operationPanel.add(operationHeader);
    operationSubheader = new JLabel();
    operationSubheader.setHorizontalAlignment(JLabel.CENTER);
    operationPanel.add(operationSubheader);

    // We have two panels that we switch between, infoPanel and progressPanel

    // The infoPanel
    infoPanel = new JPanel();
    infoValue = new JLabel();
    infoPanel.add(infoValue, BorderLayout.CENTER);

    // The progressPanel
    progressPanel = new JPanel();
    progressBar = new JProgressBar();
    progressBar.setStringPainted(true);
    progressPanel.add(progressBar, BorderLayout.CENTER);

    // And there is a button
    final JPanel buttonPanel = new JPanel();
    stopButton = new JButton("Abort");
    stopButton.addActionListener(this);
    buttonPanel.add(stopButton);

    // And all is shown in one panel, the contentPanel
    contentPanel = (JPanel) getContentPane();
    contentPanel.add(operationPanel, BorderLayout.NORTH);
    contentPanel.add(buttonPanel, BorderLayout.SOUTH);

    // Hit it!
    setMode(ExecutionDialogMode.UNINITIALIZED);
    setVisible(true);
  }

  public void addThreadToStop(final Abortable threadToStop)
  {
    threadsToStop.add(threadToStop);
  }

  /**
   * Sets the mode of the dialog.
   */
  public void setMode(final ExecutionDialogMode mode)
  {
    newMode = mode;
    updateMode();
  }

  /**
   * Changes the subheader to the supplied string.
   */
  public void setSubheader(final String string)
  {
    operationSubheader.setText(string);
  }

  /**
   * This must be called before changing mode to a progressMode.
   */
  public void initProgressBar(final int min, final int max)
  {
    // progressMin = min;
    // progressMax = max;
    progressBar.setMinimum(min);
    progressBar.setMaximum(max);
    this.progressValue = 0;
    update();
  }

  /**
   * Sets value of progress bar. The value is shown as % of completion (with
   * respect to the initialised min and max ).
   */
  public void setProgress(final int progressValue)
  {
    this.progressValue = progressValue;
    update();
  }

  public void setValue(final int value)
  {
    this.value = value;

    update();
  }

  private void update()
  {
    java.awt.EventQueue.invokeLater(this);
  }

  private void updateMode()
  {
    // Should we replace the "value panel"
    if (currCenterPanel != null) {
      contentPanel.remove(currCenterPanel);
    }
    update();
  }

  @Override
  public void run()
  {
    // Update labels
    if (newMode != currentMode) {
      currentMode = newMode;
      if (currentMode == ExecutionDialogMode.HIDE) {
        dispose();
        return;
      }
      setVisible(true);
      // Should we replace the "value panel"
      if (currCenterPanel != null) {
        contentPanel.remove(currCenterPanel);
      }

      // Update the dialog with the current mode
      operationHeader.setText(currentMode.getId());
      operationSubheader.setText(currentMode.getText());

      if (currentMode.showValue()) {
        contentPanel.add(infoPanel, BorderLayout.CENTER);
        currCenterPanel = infoPanel;
      } else if (currentMode.showProgress()) {
        contentPanel.add(progressPanel, BorderLayout.CENTER);
        currCenterPanel = progressPanel;
      }
    }

    // Update labels
    final boolean showValues = currentMode.showValue();
    final boolean showProgress = currentMode.showProgress();

    if (showValues) {
      // Don't show negative values in the dialog
      if (value >= 0) {
        infoValue.setText(String.valueOf(value));
      } else {
        infoValue.setText("");
      }
    } else if (showProgress) {
      progressBar.setValue(progressValue);

      //progressBar.setString(String.valueOf(Math.round(progressBar.getPercentComplete()*1000)/10.0) + "%");
      progressBar.setString(String.valueOf(Math.round(progressBar
        .getPercentComplete() * 100)) + "%");
    }
  }

  public void stopAllThreads()
  {
    for (final Iterator<Abortable> exIt = threadsToStop.iterator(); exIt
      .hasNext();) {
      final Abortable threadToStop = exIt.next();
      if (!threadToStop.isAborting()) {
        threadToStop.requestAbort();
      }
    }
  }

  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final Object source = event.getSource();

    if (source == stopButton) {
      if (threadsToStop != null) {
        stopAllThreads();
        threadsToStop = null; // Helping the garbage collector...
      }
      setMode(ExecutionDialogMode.HIDE);
    } else {
      System.err.println("Error in ExecutionDialog, unknown event occurred.");
    }
  }


  //#########################################################################
  //# Data Members
  private List<Abortable> threadsToStop;
  private JPanel contentPanel = null;

  /** The header of the operation. */
  private JLabel operationHeader = null;
  /** The subheader of the operation */
  private JLabel operationSubheader = null;

  private JPanel infoPanel = null;
  private JPanel progressPanel = null;
  private JLabel infoValue = null;
  private JProgressBar progressBar = null;
  private JPanel currCenterPanel = null;
  private JButton stopButton = null;

  private int progressValue = -1;
  private int value = -1;

  private ExecutionDialogMode currentMode = null;
  private ExecutionDialogMode newMode = null;
  @SuppressWarnings("unused")
  private final int nbrOfFoundStates = -1;


  //#########################################################################
  //# Class Constants
  private static final Dimension DEFAULT_DIALOG_SIZE = new Dimension(250, 120);
  private static final long serialVersionUID = 1L;

}
