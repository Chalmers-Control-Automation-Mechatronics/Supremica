//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventParameterEditorDialog
//###########################################################################
//# $Id: SimpleParameterEditorDialog.java,v 1.4 2005-11-03 01:24:15 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;
import javax.swing.event.*;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.subject.module.IntParameterSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ParameterSubject;
import net.sourceforge.waters.subject.module.RangeParameterSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;


public class SimpleParameterEditorDialog
	extends JDialog
	implements ActionListener, ItemListener
{

	//#######################################################################
	//# Constructor
	public SimpleParameterEditorDialog(final ModuleWindow root)
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

		JRadioButton integerButton, rangeButton;

		mTypeGroup.add(integerButton = new JRadioButton("Integer Expression"));
		mTypeGroup.add(rangeButton = new JRadioButton("Range"));
		integerButton.setSelected(true);
		integerButton.setActionCommand("integer");
		rangeButton.setActionCommand("range");

		JPanel buttons = new JPanel();

		buttons.setLayout(new GridLayout(1, 2));
		buttons.add(integerButton);
		buttons.add(rangeButton);
		b.add(buttons);

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
				mRoot.logEntry("ParseException in event name: " +
							   exception.getMessage());
				return;
			}
			SimpleExpressionSubject defaultExpr = null;
			int mask = 0;
			final String defaultText = mDefaultInput.getText();
			final String key = mTypeGroup.getSelection().getActionCommand();
			if (key.equals("integer"))	{
				mask = Operator.TYPE_INT;
			} else if (key.equals("range")) {
				mask = Operator.TYPE_RANGE;
			} else {
				throw new IllegalStateException
					("Parameter type not selected!");
			}
			try {
				defaultExpr =
					(SimpleExpressionSubject) parser.parse(defaultText, mask);
			} catch (final ParseException exception) {
				ErrorWindow.askRevert(exception, defaultText);
				mRoot.logEntry("ParseException in default value: " +
							   exception.getMessage());
				return;
			}

			ParameterSubject param;
			switch (mask) {
			case Operator.TYPE_INT:
				param = new IntParameterSubject
					(nameText, mIsRequired, defaultExpr);
				break;
			case Operator.TYPE_RANGE:
				param = new RangeParameterSubject
					(nameText, mIsRequired, defaultExpr);
				break;
			default:
				throw new IllegalStateException
					("Unexpected type mask " + mask + "!");
			}

			try	{
				final ModuleSubject module = mRoot.getModuleSubject();
				final IndexedList<ParameterSubject> params =
					module.getParameterListModifiable();
				params.insert(param);
			} catch (final DuplicateNameException exception) {
				JOptionPane.showMessageDialog(this, "Duplicate parameter");
				mRoot.logEntry("DuplicateNameException: " +
							   exception.getMessage());
				return;
			}

			final DefaultListModel paramData = mRoot.getParameterDataList();
			paramData.addElement(param);
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
	private final ButtonGroup mTypeGroup = new ButtonGroup();
	private final ModuleWindow mRoot;

	private DefaultListModel data = null;
	private JList dataList = null;
	private JCheckBox requiredBox = null;
	private boolean mIsRequired = false;

}
