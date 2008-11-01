//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ErrorWindow
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Container;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sourceforge.waters.model.expr.ParseException;


/**
 * <p>Provides an easy interface for viewing errors which occur.</p>
 *
 * <p>Features a descriptive message and an output text area.</p>
 *
 * @author Gian Perrone
 */

public class ErrorWindow
	extends JDialog
{

	//#######################################################################
	//# Modal Invocation
	/**
	 * Pops up an error dialog window.
	 * This method shows a dialog describing a parse error message
	 * and its position, and asks the user whether they want to
	 * continue their editing, or stop and revert to the last correct
	 * value of their input.
	 * @param  owner      The parent window from which the dialog is displayed.
	 *                    Must be a {@link Frame} or {@link Dialog},
	 *                    or <CODE>null</CODE>.
	 * @param  exception  The parse exception that caused the error.
	 * @param  input      The input which caused the error.
	 * @return <CODE>true</CODE> if the user chooses to cancel their edit,
	 *         <CODE>false</CODE> otherwise.
	 */
	public static boolean askRevert(final Container owner,
									final ParseException exception,
									final String input)
	{
		final String message = exception.getMessage();
		final int pos = exception.getErrorOffset();
		return askRevert(owner, message, input, pos);
	}


	/**
	 * Pops up an error dialog window.
	 * This method shows a dialog describing a parse error message
	 * and its position, and asks the user whether they want to
	 * continue their editing, or stop and revert to the last correct
	 * value of their input.
	 * @param  owner      The parent window from which the dialog is displayed.
	 *                    Must be a {@link Frame} or {@link Dialog},
	 *                    or <CODE>null</CODE>.
	 * @param  exception  The parse exception that caused the error.
	 * @param  input      The input which caused the error.
	 * @return <CODE>true</CODE> if the user chooses to cancel their edit,
	 *         <CODE>false</CODE> otherwise.
	 */
	public static boolean askRevert(final Container owner,
									final java.text.ParseException exception,
									final String input)
	{
		final String message = exception.getMessage();
		final int pos = exception.getErrorOffset();
		return askRevert(owner, message, input, pos);
	}


	/**
	 * Pops up an error dialog window.
	 * This method shows a dialog describing a parse error message
	 * and its position, and asks the user whether they want to
	 * continue their editing, or stop and revert to the last correct
	 * value of their input.
	 * @param  owner      The parent window from which the dialog is displayed.
	 *                    Must be a {@link Frame} or {@link Dialog},
	 *                    or <CODE>null</CODE>.
	 * @param  message    The error message to be shown.
	 * @param  input      The input which caused the error.
	 * @param  pos        The offset within the input where the error
	 *                    occurred, -1 if not applicable.
	 * @return <CODE>true</CODE> if the user chooses to cancel their edit,
	 *         <CODE>false</CODE> otherwise.
	 */
	public static boolean askRevert(final Container owner,
									final String message,
									final String input,
									final int pos)
	{
		ErrorWindow window;
		if (owner instanceof Frame) {
			final Frame frame = (Frame) owner;
			window = new ErrorWindow(frame, message, input, pos);
		} else if (owner instanceof Dialog) {
			final Dialog dialog = (Dialog) owner;
			window = new ErrorWindow(dialog, message, input, pos);
		} else {
			throw new ClassCastException
				("Unknown parent type: " + owner.getClass().getName() + "!");
		}
		return window.isCancelled();
	}


	//#######################################################################
	//# Constructors
	/**
	 * Creates an error dialog window.
	 * This method creates a dialog describing a parse error message
	 * and its position, and asking the user whether they want to
	 * continue their editing, or stop and revert to the last correct
	 * value of their input.
	 * @param  owner      The parent window from which the dialog is displayed.
	 * @param  message    The error message to be shown.
	 * @param  input      The input which caused the error.
	 * @param  pos        The offset within the input where the error
	 *                    occurred, -1 if not applicable.
	 */
	public ErrorWindow(final Frame owner,
					   final String message,
					   final String input,
					   final int pos)
	{
		super(owner, "Waters - Error!", true);
		initialize(owner, message, input, pos);
	}


	/**
	 * Creates an error dialog window.
	 * This method creates a dialog describing a parse error message
	 * and its position, and asking the user whether they want to
	 * continue their editing, or stop and revert to the last correct
	 * value of their input.
	 * @param  owner      The parent window from which the dialog is displayed.
	 * @param  message    The error message to be shown.
	 * @param  input      The input which caused the error.
	 * @param  pos        The offset within the input where the error
	 *                    occurred, -1 if not applicable.
	 */
	public ErrorWindow(final Dialog owner,
					   final String message,
					   final String input,
					   final int pos)
	{
		super(owner, "Waters - Error!", true);
		initialize(owner, message, input, pos);
	}


	private void initialize(final Component owner,
							final String message,
							final String input,
							final int pos)
	{
		setLocationRelativeTo(owner);

		final JPanel contentPane = new JPanel();
		final Box b = new Box(BoxLayout.PAGE_AXIS);

		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		JPanel messagePanel = new JPanel();

		messagePanel.add
			(new JLabel("<html><b>Error:</b> " + message + "</html>"));
		contentPane.add(messagePanel);

		final JPanel buttonPanel = new JPanel();
		final JButton okButton = new JButton("OK");
		final ActionListener okListener = new ButtonListener(false);
		okButton.addActionListener(okListener);
		buttonPanel.add(okButton);
		final JButton cancelButton = new JButton("Cancel");
		final ActionListener cancelListener = new ButtonListener(true);
		cancelButton.addActionListener(cancelListener);
		buttonPanel.add(cancelButton);

		final JEditorPane outArea = new JEditorPane("text/html", "");
		final StringBuffer buffer = new StringBuffer("<html>");
		if (pos < 0) {
			buffer.append("<b>Error:</b>\n");
			buffer.append(input);
		} else {
			buffer.append("<b>Error at position ");
			buffer.append(pos + 1);
			buffer.append(" in:</b><br><pre>");
			buffer.append(input);
			buffer.append('\n');
			for (int i = 0; i < pos; i++) {
				buffer.append('-');
			}
			buffer.append("^</pre></html>");
		}
		final String text = buffer.toString();
		outArea.setText(text);
		outArea.setEditable(false);
		contentPane.add(new JScrollPane(outArea));
		contentPane.add(buttonPanel);
		setContentPane(contentPane);
		pack();
		setVisible(true);
		okButton.requestFocus();
	}



	//#######################################################################
	//# Get the Answer
	/**
	 * Checks whether the user decided to cancel editing.
	 * @return <CODE>true</CODE> if the user chose to cancel their edit,
	 *         <CODE>false</CODE> otherwise.
	 */
	public boolean isCancelled()
	{
		return mIsCancelled;
	}



	//#######################################################################
	//# Local Class ButtonListener
	private class ButtonListener implements ActionListener
	{

		//###################################################################
		//# Constructors
		private ButtonListener(final boolean cancel)
		{
			mCancel = cancel;
		}


		//###################################################################
		//# Interface java.awt.event.ActionListener
		public void actionPerformed(final ActionEvent event)
		{
			mIsCancelled = mCancel;
			dispose();
		}


		//###################################################################
		//# Data Members
		private final boolean mCancel;

	}



	//#######################################################################
	//# Data Members
	private boolean mIsCancelled;

}
