//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   WatersAnalyzeAction
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.actions;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.sourceforge.waters.gui.compiler.CompilationObserver;
import net.sourceforge.waters.gui.dialog.MultilineLabel;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


/**
 * @author Andrew Holland, Robi Malik
 */

public abstract class WatersAnalyzeAction
  extends WatersAction
  implements SupremicaPropertyChangeListener, CompilationObserver
{

  //#########################################################################
  //# Constructor
  protected WatersAnalyzeAction(final IDE ide)
  {
    super(ide);
    ide.attach(this);
    putValue(Action.NAME, getCheckName() + " check");
    putValue(Action.SHORT_DESCRIPTION,
             "Check for " + getCheckName() + " issues");
    Config.GUI_ANALYZER_USED_FACTORY.addPropertyChangeListener(this);
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface java.awt.ActionListener
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    final ModuleContainer container = getActiveModuleContainer();
    container.compile(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.compiler.CompilationObserver
  @Override
  public void compilationSucceeded(final ProductDESProxy compiledDES)
  {
    @SuppressWarnings("unused")
    final AnalyzerDialog dialog = new AnalyzerDialog(compiledDES);
  }

  @Override
  public String getVerb()
  {
    return "verified";
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface org.supremica.properties.SupremicaPropertyChangeListener
  @Override
  public void propertyChanged(final SupremicaPropertyChangeEvent event)
  {
    updateEnabledStatus();
  }


  //#########################################################################
  //# Enablement
  void updateEnabledStatus()
  {
    final ModuleContainer container = getActiveModuleContainer();
    if (container == null) {
      setEnabled(false);
      return;
    }
    final ModelVerifier verifier = getModelVerifier();
    setEnabled(verifier != null);
  }


  //#########################################################################
  //# Factory Access
  ModelAnalyzerFactory getModelVerifierFactory()
    throws ClassNotFoundException
  {
    final ModelAnalyzerFactoryLoader loader =
      Config.GUI_ANALYZER_USED_FACTORY.get();
    return loader.getModelAnalyzerFactory();
  }

  ModelVerifier getModelVerifier()
  {
    try {
      final ProductDESProxyFactory desfactory =
        ProductDESElementFactory.getInstance();
      final ModelAnalyzerFactory vfactory = getModelVerifierFactory();
      return getModelVerifier(vfactory, desfactory);
    } catch (final NoClassDefFoundError |
                   ClassNotFoundException |
                   UnsupportedOperationException |
                   UnsatisfiedLinkError |
                   AnalysisConfigurationException exception) {
      return null;
    }
  }


  //#########################################################################
  // # Abstract Methods
  protected abstract String getCheckName();
  protected abstract String getFailureDescription();
  protected abstract String getSuccessDescription();
  protected abstract ModelVerifier getModelVerifier
    (ModelAnalyzerFactory factory, ProductDESProxyFactory desFactory) throws AnalysisConfigurationException;


  //#########################################################################
  //# Inner Class AnalyzerDialog
  private class AnalyzerDialog extends JDialog
  {
    //#######################################################################
    //# Constructor
    public AnalyzerDialog(final ProductDESProxy des)
    {
      super(getIDE());
      setLocationAndSize();
      setVisible(true);
      setTitle(getCheckName() + " Check");
      mRunner = new AnalyzerThread();
      mBottomPanel = new JPanel();
      mInformationLabel =
        new MultilineLabel(getCheckName() + " Check is running...");
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
      mVerifier = getModelVerifier();
      mVerifier.setModel(des);
      mRunner.setPriority(Thread.MIN_PRIORITY);
      mRunner.start();
    }

    //#######################################################################
    //# Buttons
    public void succeed()
    {
      final ProductDESProxy des = mVerifier.getModel();
      final String name = des.getName();
      mInformationLabel.setText
        ("Model " + name + " " + getSuccessDescription() + ".");
      mExitButton.setText("OK");
      mExitButton.removeActionListener(mExitButton.getActionListeners()[0]);
      mExitButton.addActionListener(new ActionListener(){
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
        }
      });
      repaint();
    }

    public void fail()
    {
      mExitButton.setText("OK");
      mExitButton.removeActionListener(mExitButton.getActionListeners()[0]);
      mExitButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
        }
      });
      final TraceProxy counterexample = mVerifier.getCounterExample();
      String comment = null;
      if (counterexample != null) {
        traceButton = new JButton("Show Trace");
        traceButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e)
          {
            AnalyzerDialog.this.dispose();
            final IDE ide = getIDE();
            final ModuleContainer container =
              (ModuleContainer) ide.getActiveDocumentContainer();
            container.switchToTraceMode(counterexample);
            final String comment = counterexample.getComment();
            if (comment != null && comment.length() > 0) {
              ide.info(comment);
            }
          }
        });
        mBottomPanel.add(traceButton, BorderLayout.EAST);
        comment = counterexample.getComment();
      }
      if (comment == null || comment.length() == 0) {
        final ProductDESProxy des = mVerifier.getModel();
        final String name = des.getName();
        comment = "Model " + name + " " + getFailureDescription() + ".";
      }
      mInformationLabel.setText(comment);
      repaint();
    }

    public void error(final Throwable exception)
    {
      if (exception instanceof OutOfMemoryError) {
        mInformationLabel.setText("ERROR: Out of Memory");
      } else {
        mInformationLabel.setText("ERROR: " + exception.getMessage());
      }
      mExitButton.setText("OK");
      mExitButton.removeActionListener(mExitButton.getActionListeners()[0]);
      mExitButton.addActionListener(new ActionListener(){
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
        }
      });
      repaint();
    }

    //#######################################################################
    //# Auxiliary Methods
    private void setLocationAndSize()
    {
      final Rectangle bounds = getIDE().getBounds();
      final int x = bounds.x + (bounds.width - DEFAULT_DIALOG_SIZE.width) / 2;
      final int y = bounds.y + (bounds.height - DEFAULT_DIALOG_SIZE.height) / 2;
      setLocation(x, y);
      setSize(DEFAULT_DIALOG_SIZE);
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
          mVerifier.run();
        } catch (final AnalysisAbortException exception) {
          // Do nothing: Aborted
          return;
        } catch (final AnalysisException exception) {
          SwingUtilities.invokeLater
            (new Runnable() {@Override
            public void run() {error(exception);}});
          return;
        } catch (final OutOfMemoryError error) {
          mVerifier = null;
          System.gc();
          SwingUtilities.invokeLater
            (new Runnable() {@Override
            public void run() {error(error);}});
          return;
        }
        final boolean result = mVerifier.isSatisfied();
        if (result) {
          SwingUtilities.invokeLater
            (new Runnable() {@Override
            public void run() {succeed();}});
        } else {
          SwingUtilities.invokeLater
            (new Runnable() {@Override
            public void run() {fail();}});
        }
      }

      public boolean abort()
      {
        if (mVerifier != null) {
          mVerifier.requestAbort();
          return true;
        } else {
          return false;
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final AnalyzerThread mRunner;
    private ModelVerifier mVerifier;
    private final JPanel mBottomPanel;
    private final JButton mExitButton;
    private JButton traceButton;
    private final MultilineLabel mInformationLabel;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -2478548485525996982L;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3797986885054648213L;

  private static final Dimension DEFAULT_DIALOG_SIZE = new Dimension(290, 160);

}
