//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EventParameterEditorDialog
//###########################################################################
//# $Id: EventParameterEditorDialog.java,v 1.3 2005-02-20 23:32:54 robi Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.model.expr.SimpleIdentifierProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.module.SimpleComponentType;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import java.util.Vector;

public class EventParameterEditorDialog
	extends JDialog
	implements ActionListener, ItemListener
{
	private final JTextField name = new JTextField(16);
	private final JButton okButton = new JButton("OK");
	private ButtonGroup group = new ButtonGroup();
	private ModuleWindow root = null;
	private DefaultListModel data = null;
	private JList dataList = null;
	private JCheckBox requiredBox = null;
	private boolean isRequired = false;

	public EventParameterEditorDialog(ModuleWindow root)
	{
		setTitle("Event Parameter Editor");

		this.root = root;

		// TODO: Change the selection mode for the JList component (Single selection)
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
		r1.add(name);

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

		data = new DefaultListModel();
		dataList = new JList(data);

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

		JPanel r3b = new JPanel();

		b.add(r3b);
		r3b.add(requiredBox = new JCheckBox("Required?", false));
		requiredBox.addItemListener(this);

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

	public void actionPerformed(ActionEvent e)
	{
		EventDeclProxy event = null;
		EventParameterProxy eventParam = null;

		if ("okbutton".equals(e.getActionCommand()))
		{

			// addEvent(...);
			EventKind eventkind = null;

			if (group.getSelection().getActionCommand().equals("controllable"))
			{
				eventkind = EventKind.CONTROLLABLE;
			}

			if (group.getSelection().getActionCommand().equals("uncontrollable"))
			{
				eventkind = EventKind.UNCONTROLLABLE;
			}

			if (group.getSelection().getActionCommand().equals("proposition"))
			{
				eventkind = EventKind.PROPOSITION;
			}

			ExpressionParser parser = null;

			try
			{
				if (name.getText().length() != 0)
				{
					parser = new ExpressionParser();

					SimpleExpressionProxy expr = parser.parse(name.getText(), SimpleExpressionProxy.TYPE_NAME);

					root.logEntry("Event name passed validation: " + name.getText());
				}
				else
				{
					JOptionPane.showMessageDialog(this, "Invalid identifier");
					root.logEntry("Event name was found to be invalid: " + name.getText());
				}
			}
			catch (final ParseException exception)
			{
				ErrorWindow.askRevert(exception, name.getText());
				root.logEntry("ParseException in event name: " + exception.getMessage());

				return;
			}

			event = new EventDeclProxy(name.getText(), eventkind);

			for (int i = 0; i < data.getSize(); i++)
			{
				try
				{
					parser = new ExpressionParser();

					SimpleExpressionProxy expr = parser.parse((String) data.get(i), SimpleExpressionProxy.TYPE_RANGE);

					event.getRanges().add(expr);
				}
				catch (final ParseException exception)
				{
					ErrorWindow.askRevert(exception, (String) data.get(i));
					root.logEntry("ParseException in event range: " + exception.getMessage());

					return;
				}
			}

			eventParam = new EventParameterProxy(event, isRequired);

			try
			{
				root.getModuleProxy().getParameterList().add(eventParam);
			}
			catch (final Exception exn)
			{
				root.logEntry("Exception: " + exn.getMessage());
				JOptionPane.showMessageDialog(this, "Error with parameter event");

				return;
			}

			root.getParameterDataList().add(root.getParameterDataList().getSize(), eventParam);
			dispose();
		}

		if ("cancelbutton".equals(e.getActionCommand()))
		{
			dispose();
		}

		if ("add".equals(e.getActionCommand()))
		{
			String range = JOptionPane.showInputDialog("Please enter a range:");
			int index = dataList.getSelectedIndex();

			if ((index == -1) && (data.getSize() != 0))
			{
				index = data.getSize() - 1;
			}

			ExpressionParser parser = null;

			try
			{
				if (range.length() != 0)
				{
					parser = new ExpressionParser();

					SimpleExpressionProxy expr = parser.parse(range, SimpleExpressionProxy.TYPE_RANGE);

					root.logEntry("Event range passed validation: " + range);
				}
				else
				{
					JOptionPane.showMessageDialog(this, "Invalid range");
					root.logEntry("Event range was found to be invalid: " + range);
				}
			}
			catch (final ParseException exception)
			{
				ErrorWindow.askRevert(exception,  range);
				root.logEntry("ParseException in event range: " + exception.getMessage());

				return;
			}

			data.add(index + 1, range);

			//TODO: Finish prompting for input etc.
		}

		if ("remove".equals(e.getActionCommand()))
		{
			int index = dataList.getSelectedIndex();

			data.remove(index);
		}

		if ("up".equals(e.getActionCommand()))
		{
			int index = dataList.getSelectedIndex();
			Object o = data.get(index);

			if (index != 0)
			{
				data.remove(index);
				data.add(index - 1, o);
			}
		}

		if ("down".equals(e.getActionCommand()))
		{
			int index = dataList.getSelectedIndex();
			Object o = data.get(index);

			if (index < data.size() - 1)
			{
				data.remove(index);
				data.add(index + 1, o);
			}
		}
	}

	public void itemStateChanged(ItemEvent e)
	{
		Object source = e.getItemSelectable();

		if (source == requiredBox)
		{
			isRequired = !isRequired;
		}
	}
}
