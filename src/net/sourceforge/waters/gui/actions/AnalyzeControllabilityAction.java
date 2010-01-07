package net.sourceforge.waters.gui.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
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

public class AnalyzeControllabilityAction extends WatersAnalyzeAction
{
  protected AnalyzeControllabilityAction(final IDE ide)
  {
    super(ide);
    des = null;
    this.ide = ide;
    putValue(Action.NAME, "Controllability");
    putValue(Action.SHORT_DESCRIPTION, "Check for Controllability issues");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK)); // Get better Accelerator Key
  }

  //##############################################################################
  // # Class WatersAnalyzeAction
  protected void updateEnabledStatus()
  {
    updateProductDES(ide);
    this.setEnabled(des != null);
  }

  public void actionPerformed(final ActionEvent e)
  {
    final ControllabilityJDialog dialog = new ControllabilityJDialog(ide);
    dialog.setVisible(true);
  }

  // ###################################################################
  // # Auxillary Methods

  public void updateProductDES(final IDE ide)
  {
    if (ide != null)
    {
      final DocumentContainer container = ide.getActiveDocumentContainer();
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

  // #####################################################################
  // # Inner classes

  private class ControllabilityJDialog extends JDialog
  {
    // #######################################################################
    // # Constructor

    public ControllabilityJDialog(final IDE ide)
    {
      final JPanel panel = new JPanel();
      final JButton cancelButton = new JButton("Cancel");
      run();
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          runner.abort();
          ControllabilityJDialog.this.dispose();
        }
      });
      panel.add(cancelButton, BorderLayout.CENTER);
      this.add(panel);
      this.setSize(DEFAULT_DIALOG_SIZE);
      this.setLocation(DEFAULT_DIALOG_LOCATION);
    }

    private void run()
    {
      runner = new ControllabilityThread();
      runner.setPriority(Thread.MIN_PRIORITY);
      runner.start();
    }

    // ######################################################################
    // # Inner Classes

    private class ControllabilityThread extends Thread
    {
      public ControllabilityThread()
      {
        final ProductDESProxyFactory  desfactory = ProductDESElementFactory.getInstance();
        final ModelVerifierFactory vfactory = MonolithicModelVerifierFactory.getInstance();
        verifier = vfactory.createControllabilityChecker(desfactory);
      }

        public void run()
        {
          super.run();
          boolean fatalError = false;
          final String modeName = "controllability problem";
          verifier.setModel(des);
          try {
            verifier.run();
          }
          catch (final AbortException exception)
          {
            // Do nothing: Aborted
            fatalError = true;
          } catch (final AnalysisException exception) {
            JOptionPane.showMessageDialog(ide, "Analysis Failed:\r\n" + exception.getMessage(), "Failure!", JOptionPane.ERROR_MESSAGE);
            fatalError = true;
          }
          if (!fatalError)
          {
            final boolean result = verifier.isSatisfied();
            if (result) {
              JOptionPane.showMessageDialog(ide, "No " + modeName + "s could be detected", "Success!", JOptionPane.INFORMATION_MESSAGE);
            } else {
              final TraceProxy counterexample = verifier.getCounterExample();
              // This code will soon load the trade, and switch the screen to the trace menu, once that feature is implemented
              if (ide == null)
                System.out.println("DEBUG: IDE is NULL");
              if (JOptionPane.showConfirmDialog(ide,
                 "ERROR: " + modeName.toUpperCase() + " detected. Do you wish to view the trace?",
                 "Failure", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
              {
                ((ModuleContainer)ide.getActiveDocumentContainer()).getTabPane().setSelectedIndex(1);
                ((ModuleContainer)ide.getActiveDocumentContainer()).getSimulatorPanel().switchToTraceMode(counterexample);
              }
            }
          }
          SwingUtilities.invokeLater(new Runnable(){public void run(){ControllabilityJDialog.this.dispose();}});
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

    ControllabilityThread runner;

    // #####################################################################
    // # Class Constants

    private final Dimension DEFAULT_DIALOG_SIZE = new Dimension(290, 190);
    private final Point DEFAULT_DIALOG_LOCATION = new Point(250, 150);
    private static final long serialVersionUID = -2478548485525996982L;
  }

  // ###################################################################
  // # Data Members

  IDE ide;
  ProductDESProxy des;

  // ###################################################################
  // # Class Constants

  private static final long serialVersionUID = -4339676103986484641L;
}
