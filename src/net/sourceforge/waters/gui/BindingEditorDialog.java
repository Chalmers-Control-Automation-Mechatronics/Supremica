//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   BindingEditorDialog
//###########################################################################
//# $Id: BindingEditorDialog.java,v 1.7 2007-05-24 06:21:18 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.*;
import javax.swing.*;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;


public class BindingEditorDialog
	extends JDialog
	implements ActionListener
{

	//#######################################################################
	//# Constructor
	public BindingEditorDialog(final ModuleWindowInterface root)
	{
		setTitle("Parameter Binding Editor");
		mRoot = root;

		// Center this element on the screen
		setModal(true);
		setLocationRelativeTo(null);

		JButton cancelButton = new JButton("Cancel");

		okButton.setActionCommand("OK");
		cancelButton.setActionCommand("Cancel");
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);

		JPanel contentPanel = new JPanel();
		Box b = new Box(BoxLayout.PAGE_AXIS);

		contentPanel.add(b);

		JPanel r1 = new JPanel();

		b.add(r1);
		r1.add(new JLabel("Parameter Name: "));
		r1.add(mNameInput);

		//TODO: Make this a file selector
		JPanel r2 = new JPanel();

		b.add(r2);
		r2.add(new JLabel("Expression: "));
		r2.add(mExprInput);

		JPanel r3 = new JPanel();

		b.add(r3);
		r3.add(okButton);
		r3.add(cancelButton);
		setContentPane(contentPanel);
		pack();
		show();
	}


	//#######################################################################
	//# Interface java.awt.event.ActionListener
	public void actionPerformed(final ActionEvent e)
	{
		if ("OK".equals(e.getActionCommand()))
		{
			final ExpressionParser parser = mRoot.getExpressionParser();
			final String nameText = mNameInput.getText();
			final String exprText = mExprInput.getText();
			SimpleExpressionSubject expr = null;
			try {
				parser.parseSimpleIdentifier(nameText);
			} catch (final ParseException exception) {
				JOptionPane.showMessageDialog(this, "Invalid identifier");
				return;
			}
			try {
				expr = (SimpleExpressionSubject) parser.parse(exprText);
			} catch (final ParseException exception) {
				ErrorWindow.askRevert(this, exception, exprText);
				return;
			}
			final ParameterBindingSubject binding =
				new ParameterBindingSubject(nameText, expr);
			// *** TODO *** Add binding to module ...
			// mRoot.addComponent(binding);
			dispose();
		}

		if ("Cancel".equals(e.getActionCommand()))
		{
			dispose();
		}
	}


	//#######################################################################
	//# Data Members
	private final JTextField mNameInput = new JTextField(16);
	private final JTextField mExprInput = new JTextField(16);
	private final JButton okButton = new JButton("OK");
	private final ModuleWindowInterface mRoot;

}