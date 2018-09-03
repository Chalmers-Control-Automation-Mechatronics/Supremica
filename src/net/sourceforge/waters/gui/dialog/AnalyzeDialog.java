//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesisResult;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;
import net.sourceforge.waters.gui.HTMLPrinter;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.properties.Config;


/**
 * @author Andrew Holland, Robi Malik
 */

//#########################################################################
//# Inner Class AnalyzerDialog
public abstract class AnalyzeDialog extends JDialog
{
  //#######################################################################
  //# Constructor
  public AnalyzeDialog(final JFrame owner, final ProductDESProxy des,
                       final ModelAnalyzer Verifier, final IDE ide)
  {
    super(owner);
    setTitle(getCheckName() + " Check");
    mRunner = new AnalyzerThread();
    mBottomPanel = new JPanel();
    mInformationLabel = new JLabel();
    mInformationLabel.setHorizontalAlignment(JLabel.CENTER);
    setInformationText(getCheckName() + " check is running...");
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
    mIDE = ide;
    mAnalyzer = getModelVerifier();
    mAnalyzer.setModel(des);
    mRunner.setPriority(Thread.MIN_PRIORITY);
    mRunner.start();
  }

  //#########################################################################
  // # Abstract Methods
  protected abstract String getCheckName();

  protected abstract String getFailureDescription();

  protected abstract String getSuccessDescription();

  protected abstract ModelAnalyzer getModelVerifier(ModelAnalyzerFactory factory,
                                                    ProductDESProxyFactory desFactory)
    throws AnalysisConfigurationException;

  //#########################################################################
  //# Factory Access
  ModelAnalyzerFactory getModelVerifierFactory() throws ClassNotFoundException
  {
    final ModelAnalyzerFactoryLoader loader =
      Config.GUI_ANALYZER_USED_FACTORY.get();
    return loader.getModelAnalyzerFactory();
  }

  ModelAnalyzer getModelVerifier()
  {
    try {
      final ProductDESProxyFactory desFactory =
        ProductDESElementFactory.getInstance();
      final ModelAnalyzerFactory vFactory = getModelVerifierFactory();
      final ModelAnalyzer verifier = getModelVerifier(vFactory, desFactory);
      vFactory.configureFromOptions(verifier);
      return verifier;
    } catch (final NoClassDefFoundError | ClassNotFoundException
      | UnsupportedOperationException | UnsatisfiedLinkError
      | AnalysisConfigurationException exception) {
      return null;
    }
  }

  //#######################################################################
  //# Buttons
  public void succeed()
  {
    final ProductDESProxy des = mAnalyzer.getModel();
    final String name = des.getName();
    setInformationText("Model " + name + " " + getSuccessDescription() + ".");
    mExitButton.setText("OK");
    mExitButton.removeActionListener(mExitButton.getActionListeners()[0]);
    mExitButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        AnalyzeDialog.this.dispose();
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
        AnalyzeDialog.this.dispose();
      }
    });
    String comment = null;
    if (mAnalyzer instanceof ModelVerifier) {
      final ModelVerifier modelVerifier = (ModelVerifier) mAnalyzer;
      final CounterExampleProxy counterexample =
        modelVerifier.getCounterExample();
      if (counterexample != null) {
        traceButton = new JButton("Show Trace");
        traceButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e)
          {
            AnalyzeDialog.this.dispose();
            final ModuleContainer container =
              (ModuleContainer) mIDE.getActiveDocumentContainer();
            container.switchToTraceMode(counterexample);
            final String comment = counterexample.getComment();
            if (comment != null && comment.length() > 0) {
              final Logger logger =
                LogManager.getLogger(AnalyzeDialog.this.getClass());
              logger.info(comment);
            }
          }
        });
        mBottomPanel.add(traceButton, BorderLayout.EAST);
        comment = counterexample.getComment();
      }
    }
    if (comment == null || comment.length() == 0) {
      final ProductDESProxy des = mAnalyzer.getModel();
      final String name = des.getName();
      comment = "Model " + name + " " + getFailureDescription() + ".";
    }
    setInformationText(comment);
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
        AnalyzeDialog.this.dispose();
      }
    });
    setLocationAndSize();
  }

  //#######################################################################
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


  //#######################################################################
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
      } catch (final AnalysisException | NoClassDefFoundError
        | UnsatisfiedLinkError exception) {
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
      final boolean result;
      if (mAnalyzer instanceof ModelVerifier) {
        final ModelVerifier verifier = (ModelVerifier) mAnalyzer;
        result = verifier.isSatisfied();
      } else if (mAnalyzer instanceof MonolithicSynthesizer) {
        mSynResult =
          (MonolithicSynthesisResult) mAnalyzer.getAnalysisResult();
        result = mSynResult.isSatisfied();
      } else {
        result = false;
      }
      if (result) {
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

  //#######################################################################
  //# Data Members
  protected MonolithicSynthesisResult mSynResult;
  private final IDE mIDE;
  private final AnalyzerThread mRunner;
  protected ModelAnalyzer mAnalyzer;
  private final JPanel mBottomPanel;
  protected final JButton mExitButton;
  private JButton traceButton;
  private final JLabel mInformationLabel;

  //#######################################################################
  //# Class Constants
  private static final long serialVersionUID = -2478548485525996982L;
  private static final int DEFAULT_WIDTH = 290;
}
