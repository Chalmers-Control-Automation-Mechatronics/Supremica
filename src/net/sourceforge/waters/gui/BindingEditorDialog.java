
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   BindingEditorDialog
//###########################################################################
//# $Id: BindingEditorDialog.java,v 1.2 2005-02-18 03:09:06 knut Exp $
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

public class BindingEditorDialog
	extends JDialog
	implements ActionListener
{
	private final JTextField name = new JTextField(16);
	private final JTextField expr = new JTextField(16);
	private final JButton okButton = new JButton("OK");
	ModuleWindow root = null;
	DefaultListModel data = null;
	JList dataList = null;

	public BindingEditorDialog(ModuleWindow root)
	{
		setTitle("Parameter Binding Editor");

		this.root = root;

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
		r1.add(name);

		//TODO: Make this a file selector
		JPanel r2 = new JPanel();

		b.add(r2);
		r2.add(new JLabel("Expression: "));
		r2.add(expr);

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
			SimpleExpressionProxy exp = null;
			ParameterBindingProxy pb = null;

			try
			{
				if (name.getText().length() != 0)
				{
					parser = new ExpressionParser();
					exp = parser.parse(name.getText(), SimpleExpressionProxy.TYPE_NAME);

					root.logEntry("Binding name passed validation: " + name.getText());

					parser = new ExpressionParser();
					exp = parser.parse(expr.getText());

					root.logEntry("Binding expression passed validation: " + name.getText());
				}
				else
				{
					JOptionPane.showMessageDialog(this, "Invalid identifier");
					root.logEntry("Binding name was found to be invalid: " + name.getText());
				}
			}
			catch (final ParseException exception)
			{
				ErrorWindow ew = new ErrorWindow("Parse error: " + exception.getMessage(), expr.getText(), exception.getPosition());

				root.logEntry("ParseException in binding: " + exception.getMessage());

				return;
			}

			pb = new ParameterBindingProxy(name.getText(), exp);

			root.addComponent(pb);
			dispose();
		}

		if ("Cancel".equals(e.getActionCommand()))
		{
			dispose();
		}
	}
}
