//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   InstanceEditorDialog
//###########################################################################
//# $Id: InstanceEditorDialog.java,v 1.5 2007-05-11 02:44:46 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;


public class InstanceEditorDialog
	extends JDialog
	implements ActionListener
{

	//#######################################################################
	//# Constructor
	public InstanceEditorDialog(ModuleWindowInterface root)
	{
		setTitle("Instance Component Editor");
		mRoot = root;

		// TODO: Change the selection mode for the JList component
		// (Single selection)

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
		r1.add(new JLabel("Name: "));
		r1.add(mNameInput);

		//TODO: Make this a file selector
		JPanel r2 = new JPanel();

		b.add(r2);
		r2.add(new JLabel("Module Name: "));
		r2.add(mModuleInput);

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
	public void actionPerformed(ActionEvent e)
	{
		if ("OK".equals(e.getActionCommand()))
		{
			final ExpressionParser parser = mRoot.getExpressionParser();
			final String nameText = mNameInput.getText();
			final IdentifierProxy ident;
			try {
				ident = parser.parseIdentifier(nameText);
			} catch (final ParseException exception) {
				ErrorWindow.askRevert(exception, nameText);
				return;
			}
			final String moduleText = mModuleInput.getText();
			final InstanceSubject inst =
				new InstanceSubject(ident, moduleText);
			final ModuleSubject module = mRoot.getModuleSubject();
			// *** BUG *** must add to proper subtree !!!
			module.getComponentListModifiable().add(inst);
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
	private final JTextField mModuleInput = new JTextField(16);
	private final JButton okButton = new JButton("OK");
	private final ModuleWindowInterface mRoot;

}
