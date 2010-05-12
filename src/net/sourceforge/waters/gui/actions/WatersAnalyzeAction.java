package net.sourceforge.waters.gui.actions;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.sourceforge.waters.analysis.monolithic.MonolithicModelVerifierFactory;
import net.sourceforge.waters.analysis.monolithic.MonolithicSCCControlLoopChecker;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.simulator.NonDeterministicException;
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;

public abstract class WatersAnalyzeAction
  extends WatersAction
{
  // #######################################################################
  // # Constructor
  protected WatersAnalyzeAction(final IDE ide)
  {
    super(ide);
    ide.attach(this);
    this.setEnabled(true);
    putValue(Action.NAME, getCheckName() + " check");
    putValue(Action.SHORT_DESCRIPTION, "Check for " + getCheckName() + " issues");
  }

  // ###################################################################
  // # Class WatersAction
  public void actionPerformed(final ActionEvent e)
  {
    @SuppressWarnings("unused")
    final AnalyzerDialog dialog = new AnalyzerDialog();
  }

  //########################################################################
  //# Auxiliary Methods
  private void updateProductDES()
  {
    if (getIDE() != null)
    {
      final DocumentContainer container = getIDE().getActiveDocumentContainer();
      if (container == null || !(container instanceof ModuleContainer)) {
        des = null;
        return;
      }
      final ModuleContainer mContainer = (ModuleContainer)container;
      try {
        des = mContainer.recompile();
      } catch (final EvalException exception) {
        des = null;
      }
    }
    else
      des = null;
  }


  //########################################################################
  //# Auxiliary Static Methods
  private static String wrapInHTML(final String raw)
  {
    return "<html><P STYLE=\"text-align:center;word-wrap:break-word;width:100%;left:0\">" + raw + "</p></html>";
  }


  // ##############################################################################
  // # Abstract Methods
  protected void updateEnabledStatus()
  {
    setEnabled(true);
  }
  public void update(final EditorChangedEvent event)
  {
    setEnabled(true);
  }

  protected abstract String getCheckName();
  protected abstract String getFailureDescription();
  protected abstract String getSuccessDescription();
  protected abstract ModelVerifier getModelVerifier(ModelVerifierFactory factory, ProductDESProxyFactory desFactory);


  //#########################################################################
  //# Inner Class AnalyzerDialog
  private class AnalyzerDialog extends JDialog
  {
    // #######################################################################
    // # Constructor
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
      mInformationLabel.setText("Model " + des.getName() + " " + getSuccessDescription() + ".");
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
          final TraceProxy counterexample = verifier.getCounterExample();
          if (verifier instanceof MonolithicSCCControlLoopChecker)
          {
            final Collection<EventProxy> nonLoop = ((MonolithicSCCControlLoopChecker)verifier).getNonLoopEvents();
            String info = "The non loop events are:";
            for (final EventProxy evt : nonLoop)
            {
              info += evt.getName() + ",";
            }
            getIDE().info(info);
          }
          ((ModuleContainer)getIDE().getActiveDocumentContainer()).getTabPane().setSelectedIndex(1);
          try
          {
            ((ModuleContainer)getIDE().getActiveDocumentContainer()).getSimulatorPanel().switchToTraceMode(counterexample);
          }
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
          AnalyzerDialog.this.dispose();
        }

        private File getOutputDirectory()
        {
          return getIDE().getActiveDocumentContainer().getFileLocation().getParentFile();
        }
      });
      if (verifier.getCounterExample().getComment() == null)
        mInformationLabel.setText("Model " + des.getName() + " " + getFailureDescription());
      else if (verifier.getCounterExample().getComment().compareTo("") == 0)
        mInformationLabel.setText("Model " + des.getName() + " " + getFailureDescription());
      else
        mInformationLabel.setText(verifier.getCounterExample().getComment());
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


    //######################################################################
    //# Inner Class AnalyzerThread
    private class AnalyzerThread extends Thread
    {
      public AnalyzerThread()
      {
        final ProductDESProxyFactory  desfactory =
          ProductDESElementFactory.getInstance();
        final ModelVerifierFactory vfactory =
          MonolithicModelVerifierFactory.getInstance();
        // TODO Make this configurable.
        // NativeModelVerifierFactory.getInstance();
        verifier = getModelVerifier(vfactory, desfactory);
      }

      public void run()
      {
        super.run();
        verifier.setModel(des);
        if (des == null)
        {
          SwingUtilities.invokeLater(new Runnable() {public void run(){error(new IllegalArgumentException("The model was unable to be compiled"));}});
          return;
        }
        try {
          verifier.run();
        }
        catch (final AbortException exception)
        {
          // Do nothing: Aborted
          return;
        } catch (final AnalysisException exception) {
          SwingUtilities.invokeLater(new Runnable(){public void run(){error(exception);}});
          return;
        } catch (final OutOfMemoryError error)
        {
          SwingUtilities.invokeLater(new Runnable(){public void run(){error(error);}});
          return;
        }
        final boolean result = verifier.isSatisfied();
        if (result) {
          SwingUtilities.invokeLater(new Runnable(){public void run(){succeed();}});
        } else {
          SwingUtilities.invokeLater(new Runnable(){public void run(){fail();}});
        }
      }

      public boolean abort()
      {
        if (verifier != null)
        {
          verifier.requestAbort();
          return true;
        }
        else
        {
          return false;
        }
      }
    }

    // ######################################################################
    // # Data Members
    private final AnalyzerThread runner;
    private ModelVerifier verifier;
    private final JPanel mBottomPanel;
    private final JButton mExitButton;
    private JButton traceButton;
    private final WrapperLabel mInformationLabel;

    // #####################################################################
    // # Class Constants
    private final Dimension DEFAULT_DIALOG_SIZE = new Dimension(290, 160);
    private final Point DEFAULT_DIALOG_LOCATION = new Point(250, 150);
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
  private ProductDESProxy des;


  //########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3797986885054648213L;

}