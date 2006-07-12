//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EventEditorDialog
//###########################################################################
//# $Id: EventEditorDialog.java,v 1.5 2006-07-12 03:59:29 knut Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import net.sourceforge.waters.gui.ErrorWindow;
import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedCollection;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.log.*;


public class EventEditorDialog
	extends JDialog
	implements ActionListener
{

	public EventEditorDialog(EditorPanelInterface editor)
	{
		setTitle("Event Editor");
		this.editor = editor;

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
		r1.add(mName);

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
		dataList = new JList(mData);
/*
		r2.add(new JScrollPane(dataList));

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
*/
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

	public void actionPerformed(ActionEvent e)
	{
		if ("okbutton".equals(e.getActionCommand()))
		{

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

			//final ModuleContainer root = editor.getModuleContainer();
			final ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());
			final String text = mName.getText();
			try	{
				IdentifierProxy ident = parser.parseSimpleIdentifier(text);
			} catch (final ParseException exception) {
				logger.error("ParseException in event name: " +
							 exception.getMessage(), exception);
				ErrorWindow.askRevert(exception, text);
				return;
			}
			final int numranges = mData.getSize();
			final List<SimpleExpressionSubject> ranges =
				new ArrayList<SimpleExpressionSubject>(numranges);
			for (int i = 0; i < numranges; i++) {
				final String rtext = (String) mData.get(i);
				try {
					final SimpleExpressionSubject expr =
						(SimpleExpressionSubject)
						parser.parse(rtext, Operator.TYPE_RANGE);
					ranges.add(expr);
				} catch (final ParseException exception) {
					ErrorWindow.askRevert(exception, rtext);
					logger.error("ParseException in event range: " +
								 exception.getMessage(), exception);
					return;
				}
			}

			newEvent = new EventDeclSubject(text, eventkind, true, ranges, null);

			//decl =
			//	new EventDeclSubject(text, eventkind, true, ranges, null);
			try
			{
				final ModuleSubject module = editor.getModuleSubject(); // Changed from root to editor here
				final IndexedCollection<EventDeclSubject> decls =
					module.getEventDeclListModifiable();
				decls.insert(newEvent);
			} catch (final DuplicateNameException exception) {
				logger.debug("DuplicateNameException: " +
							 exception.getMessage());
				JOptionPane.showMessageDialog(this, "Duplicate event");
				return;
			}
			//mData.addElement(decl);
			dispose();
		}

		if ("cancelbutton".equals(e.getActionCommand()))
		{
			dispose();
		}
/*
		if ("add".equals(e.getActionCommand()))
		{
			final String text =
				JOptionPane.showInputDialog("Please enter a range:");
		    int index = dataList.getSelectedIndex();
			if (index == -1 && mData.getSize() != 0) {
				index = mData.getSize() - 1;
			}
			//final ModuleContainer root = editor.getActiveModuleContainer();
			final ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());

			try {
				final SimpleExpressionProxy range =
					parser.parse(text, Operator.TYPE_RANGE);
				mData.add(index + 1, range);
			}
			catch (final ParseException exception)
			{
				ErrorWindow.askRevert(exception, text);
				logger.error("ParseException in event range: " +
							 exception.getMessage(), exception);
				return;
			}
			//TODO: Finish prompting for input etc.
		}

		if ("remove".equals(e.getActionCommand()))
		{
			int index = dataList.getSelectedIndex();

			mData.remove(index);
		}

		if ("up".equals(e.getActionCommand()))
		{
			int index = dataList.getSelectedIndex();
			Object o = mData.get(index);

			if (index != 0)
			{
				mData.remove(index);
				mData.add(index - 1, o);
			}
		}

		if ("down".equals(e.getActionCommand()))
		{
			int index = dataList.getSelectedIndex();
			Object o = mData.get(index);

			if (index < mData.size() - 1)
			{
				mData.remove(index);
				mData.add(index + 1, o);
			}
		}
*/
	}

	public EventDeclSubject getEventDeclSubject()
	{
		return newEvent;
	}

	//#######################################################################
	//# Data Members
	private final JTextField mName = new JTextField(16);
	private final JButton okButton = new JButton("OK");
	private ButtonGroup group = new ButtonGroup();
	private EventDeclSubject newEvent = null;
	EditorPanelInterface editor = null;
	DefaultListModel mData = null;
	JList dataList = null;

	private static final long serialVersionUID = 1L;
	private static final Logger logger =
		LoggerFactory.createLogger(EventEditorDialog.class);
}
