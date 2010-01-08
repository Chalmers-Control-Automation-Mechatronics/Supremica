package net.sourceforge.waters.gui.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
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
    putValue(Action.NAME, getAnalyzeMethod()[0] + " check");
    putValue(Action.SHORT_DESCRIPTION, "Check for " + getAnalyzeMethod()[0] + " issues");
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

  protected abstract void updateEnabledStatus();
  /**
   * @return Returns 3 Strings, a capitalised description, a lower-case description, and either
   * ANALYZE_CONFLICT, ANALYZE_CONTROLLABLE, or ANALYZE_CONTROL_LOOP
   */
  protected abstract String[] getAnalyzeMethod();

  public void actionPerformed(final ActionEvent e)
  {
    updateProductDES();
    final AnalyzerDialog dialog = new AnalyzerDialog(getAnalyzeMethod());
    dialog.setVisible(true);
  }

  // ##############################################################################
  // # Inner class

  private class AnalyzerDialog extends JDialog
  {
    // #######################################################################
    // # Constructor

    public AnalyzerDialog(final String[] dialogInfo)
    {
      this.setTitle(dialogInfo[0] + " Check");
      info = dialogInfo;
      final JPanel panel = new JPanel();
      final JButton cancelButton = new JButton("Abort");
      run();
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          runner.abort();
          AnalyzerDialog.this.dispose();
        }
      });
      panel.add(cancelButton, BorderLayout.CENTER);
      this.add(panel);
      this.setSize(DEFAULT_DIALOG_SIZE);
      this.setLocation(DEFAULT_DIALOG_LOCATION);
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
        if (info[2] == WatersAnalyzeAction.ANALYZE_CONFLICT)
          verifier = vfactory.createConflictChecker(desfactory);
        if (info[2] == WatersAnalyzeAction.ANALYZE_CONTROL_LOOP)
          verifier = vfactory.createControlLoopChecker(desfactory);
        if (info[2] == WatersAnalyzeAction.ANALYZE_CONTROLLABLE)
          verifier = vfactory.createControllabilityChecker(desfactory);
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
              JOptionPane.showMessageDialog(getIDE(), "No " + info[1] + "s could be detected", "Success!", JOptionPane.INFORMATION_MESSAGE);
            } else {
              final TraceProxy counterexample = verifier.getCounterExample();
              // This code will soon load the trade, and switch the screen to the trace menu, once that feature is implemented
              if (JOptionPane.showConfirmDialog(getIDE(),
                 "ERROR: " + info[0] + " detected. Do you wish to view the trace?",
                 "Failure", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
              {
                ((ModuleContainer)getIDE().getActiveDocumentContainer()).getTabPane().setSelectedIndex(1);
                ((ModuleContainer)getIDE().getActiveDocumentContainer()).getSimulatorPanel().switchToTraceMode(counterexample);
              }
            }
          }
          SwingUtilities.invokeLater(new Runnable(){public void run(){AnalyzerDialog.this.dispose();}});
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

        ModelVerifier verifier;
    }

    // ######################################################################
    // # Data Members

    AnalyzerThread runner;
    private final String[] info;

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
