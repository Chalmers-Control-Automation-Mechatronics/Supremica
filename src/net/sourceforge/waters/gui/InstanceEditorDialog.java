//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   InstanceEditorDialog
//###########################################################################
//# $Id: InstanceEditorDialog.java,v 1.3 2005-02-20 23:32:54 robi Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.module.InstanceProxy;
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

public class InstanceEditorDialog
	extends JDialog
	implements ActionListener
{
	private final JTextField name = new JTextField(16);
	private final JTextField modName = new JTextField(16);
	private final JButton okButton = new JButton("OK");
	ModuleWindow root = null;
	DefaultListModel data = null;
	JList dataList = null;

	public InstanceEditorDialog(ModuleWindow root)
	{
		setTitle("Instance Component Editor");

		this.root = root;

		// TODO: Change the selection mode for the JList component (Single selection)
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
		r1.add(name);

		//TODO: Make this a file selector
		JPanel r2 = new JPanel();

		b.add(r2);
		r2.add(new JLabel("Module Name: "));
		r2.add(modName);

		JPanel r3 = new JPanel();

		b.add(r3);
		r3.add(okButton);
		r3.add(cancelButton);
		setContentPane(contentPanel);
		pack();
		show();
	}

	public void actionPerformed(ActionEvent e)
	{
		if ("OK".equals(e.getActionCommand()))
		{
			ExpressionParser parser = null;
			SimpleExpressionProxy expr = null;
			InstanceProxy ip = null;

			try
			{
				if (name.getText().length() != 0)
				{
					parser = new ExpressionParser();
					expr = parser.parse(name.getText(), SimpleExpressionProxy.TYPE_NAME);

					root.logEntry("Instance component name passed validation: " + name.getText());
				}
				else
				{
					JOptionPane.showMessageDialog(this, "Invalid identifier");
					root.logEntry("Instance component name was found to be invalid: " + name.getText());
				}
			}
			catch (final ParseException exception)
			{
				ErrorWindow.askRevert(exception,  name.getText());
				root.logEntry("ParseException in component name: " + exception.getMessage());

				return;
			}

			ip = new InstanceProxy((IdentifierProxy) expr, modName.getText());

			root.addComponent(ip);
			dispose();
		}

		if ("Cancel".equals(e.getActionCommand()))
		{
			dispose();
		}
	}
}
