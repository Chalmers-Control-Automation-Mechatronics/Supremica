package net.sourceforge.waters.gui.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import net.sourceforge.waters.analysis.monolithic.MonolithicModelVerifierFactory;
import net.sourceforge.waters.gui.IconLoader;
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

public class AnalyzeConflictCheckAction
extends WatersAnalyzeAction
{
  protected AnalyzeConflictCheckAction(final IDE ide)
  {
    super(ide);
    des = null;
    this.ide = ide;
    System.out.println("DEBUG: ERROR: IDE is null: " + (ide == null));
    putValue(Action.NAME, "Controllability");
    putValue(Action.SHORT_DESCRIPTION, "Check for Controllability issues");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK)); // Get better Accelerator Key
    putValue(Action.SMALL_ICON, IconLoader.ICON_WARNING); // Placeholder, get better icon.
  }

  //##############################################################################
  // # Class WatersAnalyzeAction
  protected void updateEnabledStatus()
  {
    updateProductDES(ide);
    System.out.println("DEBUG: DES is non null: " + (des != null));
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
        if (container == null)
          System.out.println("DEBUG: ERROR: Document container is null");
        else
          System.out.println("DEBUG: ERROR: Document container is a class of type: " + container.getClass());
        return;
      }
      final ModuleContainer mContainer = (ModuleContainer)container;
      try {
        des = mContainer.recompile();
      } catch (final EvalException exception) {
        System.out.println("DEBUG: ERROR: Evaluation Exception: " + exception.getMessage());
        return;
      }
      System.out.println("DEBUG: SUCCESS: Des has been set to non-null: " + (des != null));
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
      running = false;
      final JPanel panel = new JPanel();
      final JPanel radioPanel = new JPanel();
      final JPanel buttonPanel = new JPanel();
      final JRadioButton conflict = new JRadioButton("Test for Conflict Problems");
      final JRadioButton controllable = new JRadioButton("Test for Controllability Problems");
      final JRadioButton controlLoop = new JRadioButton("Test for Control Loop Problems");
      final JRadioButton languageInclusion = new JRadioButton("Test for Language Inclusion Problems");
      final JButton runButton = new JButton("Run");
      final JButton cancelButton = new JButton("Cancel");
      runButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          cancelButton.setText("Abort");
          running = true;
          if (conflict.isSelected())
            run(0);
          if (controllable.isSelected())
            run(1);
          if (controlLoop.isSelected())
            run(2);
          if (languageInclusion.isSelected())
            run(3);
          if (running)
            ControllabilityJDialog.this.dispose();
        }
      });
      cancelButton.addActionListener(new ActionListener(){
        public void actionPerformed(final ActionEvent e)
        {
          if (running)
          {
            // Halt the process here
            cancelButton.setText("Cancel");
            running = false;
          }
          else
          {
            ControllabilityJDialog.this.dispose();
          }
        }
      });
      radioPanel.setLayout(new GridLayout(4,1));
      radioPanel.add(conflict);
      radioPanel.add(controllable);
      radioPanel.add(controlLoop);
      radioPanel.add(languageInclusion);
      buttonPanel.add(runButton, BorderLayout.WEST);
      buttonPanel.add(cancelButton, BorderLayout.EAST);
      panel.add(radioPanel, BorderLayout.CENTER);
      panel.add(buttonPanel, BorderLayout.SOUTH);
      this.add(panel);
      this.setSize(DEFAULT_DIALOG_SIZE);
      this.setLocation(DEFAULT_DIALOG_LOCATION);
    }

    private void run(final int mode)
    {
      boolean fatalError = false;
      final ProductDESProxyFactory  desfactory = ProductDESElementFactory.getInstance();
      final ModelVerifierFactory vfactory = MonolithicModelVerifierFactory.getInstance();
      final ModelVerifier verifier;
      String modeName;
      switch (mode)
      {
      case 0:
        verifier = vfactory.createConflictChecker(desfactory);
        modeName = "conflict problem";
        break;
      case 1:
        verifier = vfactory.createControllabilityChecker(desfactory);
        modeName = "controllability problem";
        break;
      case 2:
        verifier = vfactory.createControlLoopChecker(desfactory);
        modeName = "control loop problem";
        break;
      case 3:
        verifier = vfactory.createLanguageInclusionChecker(desfactory);
        modeName = "language inclusion problem";
        break;
      default:
        throw new UnsupportedOperationException("ERROR: Attempted mode for verification is not supported: " + mode);
      }
      verifier.setModel(des);

      try {
        verifier.run();
      } catch (final AnalysisException exception) {
        JOptionPane.showMessageDialog(ide, "Analysis Failed", "Failure!", JOptionPane.ERROR_MESSAGE);
        fatalError = true;
      }
      if (!fatalError)
      {
        final boolean result = verifier.isSatisfied();
        if (result) {
          JOptionPane.showMessageDialog(ide, "No " + modeName + "s could be detected", "Success!", JOptionPane.INFORMATION_MESSAGE);
        } else {
          final TraceProxy counterexample = verifier.getCounterExample();
          JOptionPane.showMessageDialog(ide,
            "ERROR: " + modeName.toUpperCase() + " detected. " + modeName.toUpperCase() + " has the trace: " + counterexample.getTraceSteps().toString(),
            "Failure!", JOptionPane.ERROR_MESSAGE);
          // This code will soon load the trade, and switch the screen to the trace menu, once that feature is implemented
        }
      }
    }

    // ######################################################################
    // # Data Members

    boolean running;

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
