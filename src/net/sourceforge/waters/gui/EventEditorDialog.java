//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventEditorDialog
//###########################################################################
//# $Id: EventEditorDialog.java,v 1.4 2005-11-03 01:24:15 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.xsd.base.EventKind;


public class EventEditorDialog
	extends JDialog
	implements ActionListener
{

	//#######################################################################
	//# Constructor
	public EventEditorDialog(ModuleWindow root)
	{
		setTitle("Events Editor");
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

		JRadioButton controllable, uncontrollable, proposition;

		group.add(controllable = new JRadioButton("Controllable"));
		group.add(uncontrollable = new JRadioButton("Uncontrollable"));
		group.add(proposition = new JRadioButton("Proposition"));
		controllable.setSelected(true);
		controllable.setActionCommand("controllable");
		uncontrollable.setActionCommand("uncontrollable");
		proposition.setActionCommand("proposition");

		JPanel buttons = new JPanel();

		buttons.setLayout(new GridLayout(3, 1));
		buttons.add(controllable);
		buttons.add(uncontrollable);
		buttons.add(proposition);
		b.add(buttons);

		// Add some BorderFactory trickery to make a line separator
		JPanel r2 = new JPanel();

		r2.setLayout(new GridLayout(1, 2));
		b.add(r2);

		mData = new DefaultListModel();
		mDataList = new JList(mData);

		r2.add(new JScrollPane(mDataList));

		JPanel buttonBox = new JPanel();

		buttonBox.setLayout(new GridLayout(4, 1));
		r2.add(buttonBox);

		JButton tButton;

		buttonBox.add(tButton = new JButton("Add"));
		tButton.setActionCommand("add");
		tButton.addActionListener(this);
		buttonBox.add(tButton = new JButton("Remove"));
		tButton.setActionCommand("remove");
		tButton.addActionListener(this);
		buttonBox.add(tButton = new JButton("Up"));
		tButton.setActionCommand("up");
		tButton.addActionListener(this);
		buttonBox.add(tButton = new JButton("Down"));
		tButton.setActionCommand("down");
		tButton.addActionListener(this);

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
		setVisible(true);
	}


	//#######################################################################
	//# Interface java.awt.event.ActionListener
	public void actionPerformed(ActionEvent e)
	{
		if ("okbutton".equals(e.getActionCommand()))
		{

			// addEvent(...);
			final String key = group.getSelection().getActionCommand();
			EventKind eventkind = null;
			if (key.equals("controllable"))	{
				eventkind = EventKind.CONTROLLABLE;
			} else if (key.equals("uncontrollable")) {
				eventkind = EventKind.UNCONTROLLABLE;
			} else if (key.equals("proposition")) {
				eventkind = EventKind.PROPOSITION;
			} else {
				throw new IllegalStateException("Event kind not selected!");
			}

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

			final int numranges = mData.getSize();
			final List<SimpleExpressionSubject> ranges =
				new ArrayList<SimpleExpressionSubject>(numranges);
			for (int i = 0; i < numranges; i++)	{
				final String text = (String) mData.get(i);
				try	{
					final SimpleExpressionSubject range =
						(SimpleExpressionSubject)
						parser.parse(text, Operator.TYPE_RANGE);
					ranges.add(range);
				} catch (final ParseException exception) {
					ErrorWindow.askRevert(exception, text);
					mRoot.logEntry("ParseException in event range: " +
								   exception.getMessage());
					return;
				}
			}

			final EventDeclSubject decl =
				new EventDeclSubject(nameText, eventkind, false, ranges, null);
			try	{
				final ModuleSubject module = mRoot.getModuleSubject();
				final IndexedList<EventDeclSubject> decls =
					module.getEventDeclListModifiable();
				decls.insert(decl);
			} catch (final DuplicateNameException exception) {
				JOptionPane.showMessageDialog(this, "Duplicate event");
				mRoot.logEntry("DuplicateNameException: " +
							   exception.getMessage());
				return;
			}
	
			final DefaultListModel eventData = mRoot.getEventDataList();
			eventData.addElement(decl);
			dispose();
		}

		if ("cancelbutton".equals(e.getActionCommand()))
		{
			dispose();
		}

		if ("add".equals(e.getActionCommand()))
		{
			final String text =
				JOptionPane.showInputDialog("Please enter a range:");
			final int numranges = mData.getSize();
			int index = mDataList.getSelectedIndex();
			if (index == -1 && numranges != 0) {
				index = numranges - 1;
			}
			final ExpressionParser parser = mRoot.getExpressionParser();
			try {
				parser.parse(text, Operator.TYPE_RANGE);
			} catch (final ParseException exception) {
				ErrorWindow.askRevert(exception, text);
				mRoot.logEntry("ParseException in event range: " +
							   exception.getMessage());
				return;
			}
			mData.add(index + 1, text);
			//TODO: Finish prompting for input etc.
		}

		if ("remove".equals(e.getActionCommand()))
		{
			final int index = mDataList.getSelectedIndex();
			mData.remove(index);
		}

		if ("up".equals(e.getActionCommand()))
		{
			final int index = mDataList.getSelectedIndex();
			if (index != 0) {
				final Object o = mData.get(index);
				mData.remove(index);
				mData.add(index - 1, o);
			}
		}

		if ("down".equals(e.getActionCommand()))
		{
			final int index = mDataList.getSelectedIndex();
			if (index < mData.size() - 1) {
				final Object o = mData.get(index);
				mData.remove(index);
				mData.add(index + 1, o);
			}
		}
	}


	//#######################################################################
	//# Data Members
	private final JTextField mNameInput = new JTextField(16);
	private final JButton okButton = new JButton("OK");
	private final ButtonGroup group = new ButtonGroup();
	private final ModuleWindow mRoot;
	private final DefaultListModel mData;
	JList mDataList = null;

}
