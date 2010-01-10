package net.sourceforge.waters.gui.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.sourceforge.waters.analysis.monolithic.MonolithicModelVerifierFactory;
import net.sourceforge.waters.model.analysis.AbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.expr.EvalException;
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
  // # Auxillary Methods

  public void updateProductDES()
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
        return;
      }
    }
    else
      des = null;
  }

  // ##############################################################################
  // # Abstract Methods

  protected void updateEnabledStatus()
  {
    setEnabled(true);
  }

  protected abstract String getCheckName();
  protected abstract String getFailureDescription();
  protected abstract String getSuccessDescription();
  protected abstract ModelVerifier getModelVerifier(ModelVerifierFactory factory, ProductDESProxyFactory desFactory);

  public void actionPerformed(final ActionEvent e)
  {
    updateProductDES();
    if (des != null)
    {
      final AnalyzerDialog dialog = new AnalyzerDialog();
      dialog.setVisible(true);
    }
    else
    {
      getIDE().error("ERROR: DES was not able to be successfully compiled");
    }
  }

  // ##############################################################################
  // # Inner class

  private class AnalyzerDialog extends JDialog
  {
    // #######################################################################
    // # Constructor

    public AnalyzerDialog()
    {
      this.setTitle(getCheckName() + " Check");
      majorPanel = new JPanel();
      topPanel = new JPanel();
      bottomPanel = new JPanel();
      informationLabel = new JLabel();
      informationLabel.setText(getCheckName() + " Check is running...");
      cancelButton = new JButton("Abort");
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          runner.abort();
          AnalyzerDialog.this.dispose();
        }
      });
      bottomPanel.add(cancelButton, BorderLayout.WEST);
      topPanel.add(informationLabel, BorderLayout.CENTER);
      majorPanel.add(topPanel, BorderLayout.CENTER);
      majorPanel.add(bottomPanel, BorderLayout.SOUTH);
      this.add(majorPanel);
      this.setSize(DEFAULT_DIALOG_SIZE);
      this.setLocation(DEFAULT_DIALOG_LOCATION);
      run();
    }

    public void succeed()
    {
      informationLabel.setText("Model " + " " + getSuccessDescription());
      cancelButton.setText("OK");
      cancelButton.removeActionListener(cancelButton.getActionListeners()[0]);
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
        }
      });
      this.validate();
    }

    public void fail()
    {
      informationLabel.setText("Model " + " " + getFailureDescription());
      cancelButton.setText("OK");
      cancelButton.removeActionListener(cancelButton.getActionListeners()[0]);
      traceButton = new JButton("Show Trace");
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          AnalyzerDialog.this.dispose();
        }
      });
      traceButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          final TraceProxy counterexample = verifier.getCounterExample();
          ((ModuleContainer)getIDE().getActiveDocumentContainer()).getTabPane().setSelectedIndex(1);
          ((ModuleContainer)getIDE().getActiveDocumentContainer()).getSimulatorPanel().switchToTraceMode(counterexample);
          AnalyzerDialog.this.dispose();
        }
      });
      bottomPanel.add(traceButton, BorderLayout.EAST);
      this.validate();
    }

    private void run()
    {
      runner = new AnalyzerThread();
      runner.setPriority(Thread.MIN_PRIORITY);
      runner.start();
    }

    // ######################################################################
    // # Inner Classes

    private class AnalyzerThread extends Thread
    {
      public AnalyzerThread()
      {
        final ProductDESProxyFactory  desfactory = ProductDESElementFactory.getInstance();
        final ModelVerifierFactory vfactory = MonolithicModelVerifierFactory.getInstance();
        verifier = getModelVerifier(vfactory, desfactory);
      }

        public void run()
        {
          super.run();
          boolean fatalError = false;
          verifier.setModel(des);
          try {
            verifier.run();
          }
          catch (final AbortException exception)
          {
            // Do nothing: Aborted
            fatalError = true;
          } catch (final AnalysisException exception) {
            JOptionPane.showMessageDialog(getIDE(), "Analysis Failed:\r\n" + exception.getMessage(), "Failure!", JOptionPane.ERROR_MESSAGE);
            fatalError = true;
          }
          if (!fatalError)
          {
            final boolean result = verifier.isSatisfied();
            if (result) {
              SwingUtilities.invokeLater(new Runnable(){public void run(){succeed();}});
            } else {
              SwingUtilities.invokeLater(new Runnable(){public void run(){fail();}});
            }
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

    AnalyzerThread runner;
    ModelVerifier verifier;
    JPanel topPanel;
    JPanel bottomPanel;
    JPanel majorPanel;
    JButton cancelButton;
    JButton traceButton;
    JLabel informationLabel;

    // #####################################################################
    // # Class Constants

    private final Dimension DEFAULT_DIALOG_SIZE = new Dimension(290, 190);
    private final Point DEFAULT_DIALOG_LOCATION = new Point(250, 150);
    private static final long serialVersionUID = -2478548485525996982L;
  }

  // ##############################################################################
  // # Data Members

  ProductDESProxy des;

  // ##############################################################################
  // # Class Constants
  protected static final String ANALYZE_CONFLICT = "CONFLICT";
  protected static final String ANALYZE_CONTROLLABLE = "CONTROLLABLE";
  protected static final String ANALYZE_CONTROL_LOOP = "CONTROL LOOP";
  private static final long serialVersionUID = -3797986885054648213L;
}
