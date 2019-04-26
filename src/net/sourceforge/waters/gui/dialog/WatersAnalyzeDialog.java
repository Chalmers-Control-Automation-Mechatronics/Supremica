//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.sourceforge.waters.gui.HTMLPrinter;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * @author George Hewlett, Andrew Holland, Robi Malik
 */

public abstract class WatersAnalyzeDialog extends JDialog
{
  //#########################################################################
  //# Constructor
  public WatersAnalyzeDialog(final Frame owner,
                             final ProductDESProxy des)
  {
    super(owner);
    final String title = getAnalysisName();
    setTitle(title);
    mRunner = new AnalyzerThread();
    mBottomPanel = new JPanel();
    mInformationLabel = new JLabel();
    mInformationLabel.setHorizontalAlignment(JLabel.CENTER);
    setInformationText(title + " is running...");
    final Border outer = BorderFactory.createRaisedBevelBorder();
    final Border inner = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    final Border border = BorderFactory.createCompoundBorder(outer, inner);
    mInformationLabel.setBorder(border);
    mExitButton = new JButton("Abort");
    mExitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        mRunner.abort();
        dispose();
      }
    });
    mBottomPanel.add(mExitButton, BorderLayout.WEST);
    final Container pane = getContentPane();
    pane.add(mInformationLabel, BorderLayout.CENTER);
    pane.add(mBottomPanel, BorderLayout.SOUTH);
    setLocationAndSize();
    setVisible(true);
    mAnalyzer = createModelAnalyzer();
    mAnalyzer.setModel(des);
    mRunner.setPriority(Thread.MIN_PRIORITY);
    mRunner.start();
  }


  //#########################################################################
  //# Simple Access
  protected ModelAnalyzer getModelAnalyzer()
  {
    return mAnalyzer;
  }


  //#########################################################################
  //# Abstract Methods
  protected abstract String getAnalysisName();

  protected abstract String getFailureText();

  protected abstract String getSuccessText();

  protected abstract ModelAnalyzer createModelAnalyzer();


  //#########################################################################
  //# Buttons
  public void succeed()
  {
    setInformationText(getSuccessText());
    mExitButton.setText("OK");
    mExitButton.removeActionListener(mExitButton.getActionListeners()[0]);
    mExitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        WatersAnalyzeDialog.this.dispose();
      }
    });
    setLocationAndSize();
  }

  public void fail()
  {
    mExitButton.setText("OK");
    mExitButton.removeActionListener(mExitButton.getActionListeners()[0]);
    mExitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        WatersAnalyzeDialog.this.dispose();
      }
    });
    setInformationText(getFailureText());
    setLocationAndSize();
  }

  public void error(final Throwable exception)
  {
    if (exception instanceof OutOfMemoryError) {
      setInformationText("ERROR: Out of Memory");
    } else {
      setInformationText("ERROR: " + exception.getMessage());
    }
    mExitButton.setText("OK");
    mExitButton.removeActionListener(mExitButton.getActionListeners()[0]);
    mExitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        WatersAnalyzeDialog.this.dispose();
      }
    });
    setLocationAndSize();
  }


  //#########################################################################
  //# Auxiliary Methods
  protected void setLocationAndSize()
  {
    pack();
    setLocationRelativeTo(getOwner());
  }

  protected void setInformationText(final String text)
  {
    HTMLPrinter.setLabelText(mInformationLabel, text, DEFAULT_WIDTH);
  }

  protected void addButton(final JButton button)
  {
    mBottomPanel.add(button, BorderLayout.EAST);
  }


  //#########################################################################
  //# Inner Class AnalyzerThread
  private class AnalyzerThread extends Thread
  {
    @Override
    public void run()
    {
      super.run();
      try {
        mAnalyzer.run();
      } catch (final AnalysisAbortException exception) {
        // Do nothing: Aborted
        return;
      } catch (final AnalysisException | NoClassDefFoundError |
               UnsatisfiedLinkError exception) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run()
          {
            error(exception);
          }
        });
        return;
      } catch (final OutOfMemoryError error) {
        mAnalyzer = null;
        System.gc();
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run()
          {
            error(error);
          }
        });
        return;
      }
      final AnalysisResult result = mAnalyzer.getAnalysisResult();
      if (result.isSatisfied()) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run()
          {
            succeed();
          }
        });
      } else {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run()
          {
            fail();
          }
        });
      }
    }

    public boolean abort()
    {
      if (mAnalyzer != null) {
        mAnalyzer.requestAbort();
        return true;
      } else {
        return false;
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final AnalyzerThread mRunner;
  private ModelAnalyzer mAnalyzer;
  private final JPanel mBottomPanel;
  private final JButton mExitButton;
  private final JLabel mInformationLabel;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -2478548485525996982L;
  private static final int DEFAULT_WIDTH = 290;
}
