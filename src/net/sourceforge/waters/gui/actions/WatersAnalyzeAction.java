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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.analysis.ModelVerifierFactoryLoader;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.properties.Config;
import org.supremica.properties.SupremicaPropertyChangeEvent;
import org.supremica.properties.SupremicaPropertyChangeListener;


public abstract class WatersAnalyzeAction
  extends WatersAction
  implements SupremicaPropertyChangeListener
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
  public void actionPerformed(final ActionEvent e)
  {
    @SuppressWarnings("unused")
    final AnalyzerDialog dialog = new AnalyzerDialog();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    updateEnabledStatus();
  }


  //#########################################################################
  //# Interface org.supremica.properties.SupremicaPropertyChangeListener
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
  ModelVerifierFactory getModelVerifierFactory()
    throws ClassNotFoundException
  {
    final ModelVerifierFactoryLoader loader =
      (ModelVerifierFactoryLoader) Config.GUI_ANALYZER_USED_FACTORY.get();
    return loader.getModelVerifierFactory();
  }

  ModelVerifier getModelVerifier()
  {
    try {
      final ProductDESProxyFactory desfactory =
        ProductDESElementFactory.getInstance();
      final ModelVerifierFactory vfactory = getModelVerifierFactory();
      return getModelVerifier(vfactory, desfactory);
    } catch (final NoClassDefFoundError error) {
      return null;
    } catch (final ClassNotFoundException exception) {
      return null;
    } catch (final UnsupportedOperationException exception) {
      return null;
    } catch (final UnsatisfiedLinkError exception) {
      return null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void updateProductDES()
  {
    if (getIDE() != null) {
      final DocumentContainer container = getIDE().getActiveDocumentContainer();
      if (container == null || !(container instanceof ModuleContainer)) {
        mProductDES = null;
        return;
      }
      final ModuleContainer mContainer = (ModuleContainer)container;
      try {
        mProductDES = mContainer.recompile();
      } catch (final EvalException exception) {
        mProductDES = null;
      }
    } else {
      mProductDES = null;
    }
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static String wrapInHTML(final String raw)
  {
    return "<html><P STYLE=\"text-align:center;word-wrap:break-word;width:100%;left:0\">" + raw + "</p></html>";
  }


  //#########################################################################
  // # Abstract Methods
  protected abstract String getCheckName();
  protected abstract String getFailureDescription();
  protected abstract String getSuccessDescription();
  protected abstract ModelVerifier getModelVerifier
    (ModelVerifierFactory factory, ProductDESProxyFactory desFactory);


  //#########################################################################
  //# Inner Class AnalyzerDialog
  private class AnalyzerDialog extends JDialog
  {
    //#######################################################################
    //# Constructor
    public AnalyzerDialog()
    {
      setSize(DEFAULT_DIALOG_SIZE);
      setLocation(DEFAULT_DIALOG_LOCATION);
      setVisible(true);
      setTitle(getCheckName() + " Check");
      mBottomPanel = new JPanel();
      mInformationLabel =
        new WrapperLabel(getCheckName() + " Check is running...");
      mInformationLabel.setHorizontalAlignment(SwingConstants.CENTER);
      final Border outer = BorderFactory.createRaisedBevelBorder();
      final Border inner = BorderFactory.createEmptyBorder(4, 4, 4, 4);
      final Border border = BorderFactory.createCompoundBorder(outer, inner);
      mInformationLabel.setBorder(border);
      mExitButton = new JButton("Abort");
      mExitButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          runner.abort();
          dispose();
        }
      });
      mBottomPanel.add(mExitButton, BorderLayout.WEST);
      final Container pane = getContentPane();
      pane.add(mInformationLabel, BorderLayout.CENTER);
      pane.add(mBottomPanel, BorderLayout.SOUTH);
      updateProductDES();
      runner = new AnalyzerThread();
      runner.setPriority(Thread.MIN_PRIORITY);
      runner.start();
    }

    public void succeed()
    {
      mInformationLabel.setText("Model " + mProductDES.getName() + " " +
                                getSuccessDescription() + ".");
      mExitButton.setText("OK");
      mExitButton.removeActionListener(mExitButton.getActionListeners()[0]);
      mExitButton.addActionListener(new ActionListener(){
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
      traceButton = new JButton("Show Trace");
      mExitButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
        }
      });
      traceButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
          final TraceProxy counterexample = mVerifier.getCounterExample();
          /*
          if (verifier instanceof MonolithicSCCControlLoopChecker) {
            final Collection<EventProxy> nonLoop = ((MonolithicSCCControlLoopChecker)verifier).getNonLoopEvents();
            String info = "The non loop events are:";
            for (final EventProxy evt : nonLoop)
            {
              info += evt.getName() + ",";
            }
            getIDE().info(info);
          }
          */
          final IDE ide = getIDE();
          final ModuleContainer container =
            (ModuleContainer) ide.getActiveDocumentContainer();
          container.switchToTraceMode(counterexample);
          final String comment = counterexample.getComment();
          if (comment != null && comment.length() > 0) {
            ide.info(comment);
          }
          /*
          catch (final NonDeterministicException exception)
          {
            final DocumentManager docManager = getIDE().getDocumentContainerManager().getDocumentManager();
            final ProxyMarshaller<TraceProxy> marshaller =
              docManager.findProxyMarshaller(TraceProxy.class);
            final String ext = marshaller.getDefaultExtension();
            final File outdir = getOutputDirectory();
            final String outname = getIDE().getActiveDocumentContainer().getName();
            final File outfile = new File(outdir, outname + ext);
            getIDE().error(("The trace data is missing Non-Deterministic information, and thus, it cannot be compiled. The trace has been saved to " + outfile.getAbsolutePath()));
            try {
              docManager.saveAs(counterexample, outfile);
            } catch (final WatersMarshalException exception1) {
              getIDE().error("Waters Marshal Error prevented the file from being written.");
            } catch (final IOException exception1) {
              getIDE().error("IO Exception prevented the file from being written: " + exception1);
            }
          }
          */
        }

        @SuppressWarnings("unused")
        private File getOutputDirectory()
        {
          return getIDE().getActiveDocumentContainer().getFileLocation().getParentFile();
        }
      });
      final String comment = mVerifier.getCounterExample().getComment();
      if (comment == null || comment.length() == 0) {
        mInformationLabel.setText("Model " + mProductDES.getName() + " " +
                                  getFailureDescription() + ".");
      } else {
        mInformationLabel.setText(comment);
      }
      mBottomPanel.add(traceButton, BorderLayout.EAST);
      repaint();
    }

    public void error(final Throwable exception)
    {
      if (exception instanceof OutOfMemoryError)
        mInformationLabel.setText("ERROR: Out of Memory");
      else
        mInformationLabel.setText("ERROR: " + exception.getMessage());
      mExitButton.setText("OK");
      mExitButton.removeActionListener(mExitButton.getActionListeners()[0]);
      mExitButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
        }
      });
      repaint();
    }


    //#######################################################################
    //# Inner Class AnalyzerThread
    private class AnalyzerThread extends Thread
    {

      public void run()
      {
        super.run();
        if (mProductDES == null) {
          final Exception exception = new IllegalArgumentException
            ("The model was not be compiled successfully.");
          SwingUtilities.invokeLater
            (new Runnable() {public void run() {error(exception);}});
          return;
        }
        try {
          mVerifier = getModelVerifier();
          mVerifier.setModel(mProductDES);
          mVerifier.run();
        } catch (final AbortException exception) {
          // Do nothing: Aborted
          return;
        } catch (final AnalysisException exception) {
          SwingUtilities.invokeLater
            (new Runnable() {public void run() {error(exception);}});
          return;
        } catch (final OutOfMemoryError error) {
          mVerifier = null;
          System.gc();
          SwingUtilities.invokeLater
            (new Runnable() {public void run() {error(error);}});
          return;
        }
        final boolean result = mVerifier.isSatisfied();
        if (result) {
          SwingUtilities.invokeLater
            (new Runnable() {public void run() {succeed();}});
        } else {
          SwingUtilities.invokeLater
            (new Runnable() {public void run() {fail();}});
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

    //######################################################################
    //# Data Members
    private final AnalyzerThread runner;
    private ModelVerifier mVerifier;
    private final JPanel mBottomPanel;
    private final JButton mExitButton;
    private JButton traceButton;
    private final WrapperLabel mInformationLabel;

    //######################################################################
    //# Class Constants
    private static final long serialVersionUID = -2478548485525996982L;
  }


  //########################################################################
  //# Inner Class WrapperLabel
  private class WrapperLabel extends JLabel
  {

    //######################################################################
    //# Constructor
    private WrapperLabel(final String e)
    {
      super(wrapInHTML(e));
    }

    //######################################################################
    //# Overrides for javax.swing.JLabel
    public void setText(final String e)
    {
      super.setText(wrapInHTML(e));
    }

    //######################################################################
    //# Class Constants
    private static final long serialVersionUID = -6693747793242415495L;

  }


  //########################################################################
  //# Data Members
  private ProductDESProxy mProductDES;


  //########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3797986885054648213L;

  private static final Dimension DEFAULT_DIALOG_SIZE = new Dimension(290, 160);
  private static final Point DEFAULT_DIALOG_LOCATION = new Point(250, 150);

}