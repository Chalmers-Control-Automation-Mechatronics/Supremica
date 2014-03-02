package net.sourceforge.waters.gui.compiler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

import net.sourceforge.waters.gui.dialog.MultilineLabel;
import net.sourceforge.waters.model.expr.EvalException;

import org.supremica.gui.ide.IDE;


/**
 * A dialog which initially says "Compiling..." with an Abort button and can
 * later be changed to display an error message with an OK button.
 */
public class CompilationDialog extends JDialog
{

  //##########################################################################
  //# Constructors
  /**
   * Creates a dialog.
   * @param ide     The parent window.
   * @param action  The action to perform when the Abort/OK button is pressed.
   *                If <CODE>null</CODE>, the button closes the dialog.
   */
  public CompilationDialog(final IDE ide, final ActionListener action)
  {
    super(ide, "Compilation");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(DEFAULT_DIALOG_SIZE);
    setLocationRelativeTo(ide);
    mInformationLabel = new MultilineLabel("Compiling...");
    final Border outer = BorderFactory.createRaisedBevelBorder();
    final Border inner = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    final Border border = BorderFactory.createCompoundBorder(outer, inner);
    mInformationLabel.setBorder(border);
    mExitButton = new JButton("Abort");
    if (action != null) {
      mExitButton.addActionListener(action);
    } else {
      mExitButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          dispose();
        }
      });
    }
    final JPanel exitPanel = new JPanel();
    exitPanel.add(mExitButton);
    final Container pane = getContentPane();
    pane.add(mInformationLabel, BorderLayout.CENTER);
    pane.add(exitPanel, BorderLayout.SOUTH);
    setVisible(true);
  }

  /**
   * Displays an error message.
   * @param exception   The <CODE>EvalException</CODE> for which to display
   *                    the message.
   * @param taskVerb    Should fit in the sentence "The module cannot be
   *                    <I>verb</I> because it has errors".
   */
  public void setEvalException(final EvalException exception,
                               final String taskVerb)
  {
    final StringBuilder sb = new StringBuilder();
    sb.append("The module cannot be ");
    sb.append(taskVerb);
    sb.append(" because it has ");
    final EvalException[] all = exception.getAll();
    if (all.length == 1) {
      sb.append("an error: " );
      sb.append(all[0].getMessage());
    } else {
      sb.append(all.length);
      sb.append(" errors.");
    }
    mInformationLabel.setText(sb.toString());
    mExitButton.setText("OK");
  }


  //##########################################################################
  //# Data Members
  private final JButton mExitButton;
  private final MultilineLabel mInformationLabel;


  //##########################################################################
  //# Class Constants
  private static final Dimension DEFAULT_DIALOG_SIZE = new Dimension(290, 160);
  private static final long serialVersionUID = 1L;

}
