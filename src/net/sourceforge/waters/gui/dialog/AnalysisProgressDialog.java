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

package net.sourceforge.waters.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;


/**
 * A dialog to be displayed while and after an analysis operation is
 * running. It informs the user of the running operation and provides
 * a button to abort the operation. When the operation is finished,
 * it may display information about the result and possibly further
 * options, e.g. switching to counterexample visualisation.
 *
 * @author George Hewlett, Andrew Holland, Robi Malik
 */

public abstract class AnalysisProgressDialog extends JDialog
{
  //#########################################################################
  //# Constructor
  private AnalysisProgressDialog(final IDE owner)
  {
    super(owner);
    mBottomPanel = new JPanel();
    mInformationLabel = new JLabel();
    mInformationLabel.setHorizontalAlignment(JLabel.CENTER);
    final Border outer = BorderFactory.createRaisedBevelBorder();
    final Border inner = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    final Border border = BorderFactory.createCompoundBorder(outer, inner);
    mInformationLabel.setBorder(border);
    mExitButton = new JButton("Abort");
    mBottomPanel.add(mExitButton, BorderLayout.WEST);
    final Container pane = getContentPane();
    pane.add(mInformationLabel, BorderLayout.CENTER);
    pane.add(mBottomPanel, BorderLayout.SOUTH);
  }

  protected AnalysisProgressDialog(final IDE owner,
                                   final ModelAnalyzer analyzer,
                                   final String title)
  {
    this(owner);
    setTitle("Running " + title);
    setInformationText(title + " is running...");
    mAnalyzer = analyzer;
    mRunner = new AnalyzerThread();
    mRunner.setPriority(Thread.MIN_PRIORITY);
    mExitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        mRunner.abort();
        dispose();
      }
    });
    setLocationAndSize();
    setVisible(true);
    mRunner.start();
  }

  protected AnalysisProgressDialog(final IDE owner,
                                   final Throwable exception)
  {
    this(owner);
    error(exception);
    setVisible(true);
  }


  //#########################################################################
  //# Simple Access
  protected IDE getIDE()
  {
    return (IDE) getOwner();
  }

  protected ModelAnalyzer getModelAnalyzer()
  {
    return mAnalyzer;
  }


  //#########################################################################
  //# Abstract Methods
  protected abstract String getWindowTitle();

  protected String getFailureText()
  {
    return getWindowTitle() + " Failed";
  }

  protected String getSuccessText()
  {
    return getWindowTitle() + " Successful";
  }


  //#########################################################################
  //# Buttons
  public void succeed()
  {
    final String title = getWindowTitle() + " Successful";
    setTitle(title);
    setInformationText(getSuccessText());
    mExitButton.setText("OK");
    mExitButton.removeActionListener(mExitButton.getActionListeners()[0]);
    mExitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        AnalysisProgressDialog.this.dispose();
      }
    });
    setLocationAndSize();
  }

  public void fail()
  {
    final String title = getWindowTitle() + " Failed";
    setTitle(title);
    setInformationText(getFailureText());
    mExitButton.setText("OK");
    mExitButton.removeActionListener(mExitButton.getActionListeners()[0]);
    mExitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        AnalysisProgressDialog.this.dispose();
      }
    });
    setLocationAndSize();
  }

  public void error(final Throwable exception)
  {
    final String title = getWindowTitle() + " Error";
    setTitle(title);
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
        AnalysisProgressDialog.this.dispose();
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

  protected void showRunTime(final AnalysisResult result)
  {
    final long time = result.getRunTime();
    if (time >= 0) {
      final Logger logger = LogManager.getFormatterLogger();
      if (logger.isInfoEnabled()) {
        logger.info("%s completed in %.1fs.",
                    getWindowTitle(), 0.001 * time);
      }
    }
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
      } catch (final AnalysisException exception) {
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
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run()
        {
          final AnalysisResult result = mAnalyzer.getAnalysisResult();
          showRunTime(result);
          if (result.isSatisfied()) {
            succeed();
          } else {
            fail();
          }
        }
      });
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
  private final JPanel mBottomPanel;
  private final JButton mExitButton;
  private final JLabel mInformationLabel;

  private AnalyzerThread mRunner;
  private ModelAnalyzer mAnalyzer;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -2478548485525996982L;
  private static final int DEFAULT_WIDTH = 290;
}
