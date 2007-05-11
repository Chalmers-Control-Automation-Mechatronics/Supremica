//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventParameterEditorDialog
//###########################################################################
//# $Id: SimpleParameterEditorDialog.java,v 1.8 2007-05-11 02:44:46 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ParameterSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleParameterSubject;


public class SimpleParameterEditorDialog
	extends JDialog
	implements ActionListener, ItemListener
{

	//#######################################################################
	//# Constructor
	public SimpleParameterEditorDialog(final ModuleWindowInterface root)
	{
		setTitle("Simple Parameter Editor");
		mRoot = root;

		// TODO: Change the selection mode for the JList component
		// (Single selection)

		// Center this element on the screen
		setModal(true);
		setLocationRelativeTo(null);
		okButton.setActionCommand("OK");

		JPanel contentPanel = new JPanel();
		Box b = new Box(BoxLayout.PAGE_AXIS);

		contentPanel.add(b);

		JPanel r1 = new JPanel();

		b.add(r1);
		r1.add(new JLabel("Name: "));
		r1.add(mNameInput);

		JPanel r2 = new JPanel();

		b.add(r2);
		r2.add(requiredBox = new JCheckBox("Required?", false));
		requiredBox.addItemListener(this);

		JPanel r3 = new JPanel();

		b.add(r3);
		r3.add(new JLabel("Default Value: "));
		r3.add(mDefaultInput);

		JButton cancelButton = new JButton("Cancel");
		JPanel r4 = new JPanel();

		r4.add(okButton);
		okButton.setActionCommand("okbutton");
		okButton.addActionListener(this);
		r4.add(cancelButton);
		cancelButton.setActionCommand("cancelbutton");
		cancelButton.addActionListener(this);
		b.add(r4);
		setContentPane(contentPanel);
		pack();
		show();
	}


	//#######################################################################
	//# Interface java.awt.event.ActionListener
	public void actionPerformed(ActionEvent e)
	{
		if ("okbutton".equals(e.getActionCommand())) {
			final ExpressionParser parser = mRoot.getExpressionParser();
			final String nameText = mNameInput.getText();
			try {
				parser.parseSimpleIdentifier(nameText);
			} catch (final ParseException exception) {
				ErrorWindow.askRevert(exception, nameText);
				return;
			}
			SimpleExpressionSubject defaultExpr = null;
			final String defaultText = mDefaultInput.getText();
			try {
				defaultExpr =
					(SimpleExpressionSubject) parser.parse(defaultText);
			} catch (final ParseException exception) {
				ErrorWindow.askRevert(exception, defaultText);
				return;
			}
			final ParameterSubject param =
				new SimpleParameterSubject(nameText, mIsRequired, defaultExpr);
			try	{
				final ModuleSubject module = mRoot.getModuleSubject();
				final IndexedList<ParameterSubject> params =
					module.getParameterListModifiable();
				params.insert(param);
			} catch (final DuplicateNameException exception) {
				JOptionPane.showMessageDialog(this, "Duplicate parameter");
				return;
			}
			dispose();
		}

		if ("cancelbutton".equals(e.getActionCommand()))
		{
			dispose();
		}
	}


	//#######################################################################
	//# Interface java.awt.event.ItemListener
	public void itemStateChanged(final ItemEvent event)
	{
		final Object source = event.getItemSelectable();
		if (source == requiredBox) {
			mIsRequired = !mIsRequired;
		}
	}


	//#######################################################################
	//# Data Members
	private final JTextField mNameInput = new JTextField(16);
	private final JTextField mDefaultInput = new JTextField(16);
	private final JButton okButton = new JButton("OK");
	private final ModuleWindowInterface mRoot;

	private DefaultListModel data = null;
	private JList dataList = null;
	private JCheckBox requiredBox = null;
	private boolean mIsRequired = false;

}
