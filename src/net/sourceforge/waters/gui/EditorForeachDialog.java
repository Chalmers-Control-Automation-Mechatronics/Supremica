
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorForeachDialog
//###########################################################################
//# $Id: EditorForeachDialog.java,v 1.2 2005-02-18 03:09:06 knut Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.*;
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

/** <p>A dialog to help users create new foreach components in a module.</p>
 *
 * <p>This dialog creates new components when the "new" option is selected
 * in the module window.</p>
 *
 * @author Gian Perrone
 */
public class EditorForeachDialog
	extends JDialog
	implements ActionListener
{
	private final JTextField name = new JTextField(5);
	private final JTextField range = new JTextField(25);
	private final JTextField guard = new JTextField(25);
	private ModuleWindow root = null;

	public EditorForeachDialog(ModuleWindow root)
	{
		this.root = root;

		this.setTitle("Foreach Component Editor");
		JFrame.setDefaultLookAndFeelDecorated(true);

		Box b = new Box(BoxLayout.PAGE_AXIS);
		JPanel r1 = new JPanel();
		JPanel r2 = new JPanel();
		JPanel r3 = new JPanel();
		JPanel r4 = new JPanel();
		JPanel r5 = new JPanel();

		b.add(r1);
		b.add(r2);
		b.add(r3);
		b.add(r4);
		b.add(r5);
		r1.add(new JLabel("<html><b>Foreach Component Editor</b></html>"));
		r2.add(new JLabel("Foreach "));
		r2.add(name);
		r3.add(new JLabel(" in "));
		r3.add(range);
		r4.add(new JLabel(" where "));
		r4.add(guard);

		JButton b1 = new JButton("OK");
		JButton b2 = new JButton("Cancel");

		b1.setActionCommand("ok");
		b2.setActionCommand("cancel");
		b1.addActionListener(this);
		b2.addActionListener(this);

		/*String[] kindStrings = { "Plant", "Specification", "Property" };

		kindList = new JComboBox(kindStrings);
		kindList.setSelectedIndex(0);
		kindList.addActionListener(this);

		r3.add(new JLabel("Kind: "));
		r3.add(kindList);*/
		r5.add(b1);
		r5.add(b2);
		this.setContentPane(b);
		this.pack();
		setLocationRelativeTo(root);
		this.show();
		this.setVisible(true);
		name.requestFocusInWindow();
	}

	public void actionPerformed(ActionEvent e)
	{
		String body = "";

		/*if(e.getSource() == kindList) {
			String kindName = (String)kindList.getSelectedItem();

			}*/
		if ("ok".equals(e.getActionCommand()))
		{

			//TODO: Make this create the component
			//this.setVisible(false);
			ExpressionParser parser = new ExpressionParser();

			try
			{
				body = name.getText();

				SimpleExpressionProxy expr = parser.parse(name.getText());

				if (!(expr instanceof SimpleIdentifierProxy))
				{
					JOptionPane.showMessageDialog(this, "This is not valid identifier");

					return;
				}
			}
			catch (final ParseException exception)
			{

				// an error has occurred ...
				JOptionPane.showMessageDialog(this, "Invalid Name for Component");

				ErrorWindow ew = new ErrorWindow("Parse error in identifier: " + exception.getMessage(), name.getText(), exception.getPosition());

				return;
			}

			//NamedElementProxyProxy ip = new NamedElementProxy(name.getText());
			ForeachComponentProxy fcp = null;

			try
			{
				if ((range.getText().length() != 0) && (guard.getText().length() != 0))
				{
					parser = new ExpressionParser();
					body = range.getText();

					SimpleExpressionProxy expr = parser.parse(range.getText(), SimpleExpressionProxy.TYPE_RANGE);

					parser = new ExpressionParser();
					body = guard.getText();
					fcp = new ForeachComponentProxy(name.getText(), expr, parser.parse(guard.getText(), SimpleExpressionProxy.TYPE_INT));

					root.logEntry("Foreach: " + name.getText() + " " + range.getText() + " " + guard.getText());
				}
				else if ((range.getText().length() != 0) && (guard.getText().length() == 0))
				{
					parser = new ExpressionParser();
					body = range.getText();
					fcp = new ForeachComponentProxy(name.getText(), parser.parse(range.getText()));

					root.logEntry("Foreach: " + name.getText() + " IN " + range.getText());
				}
			}
			catch (final ParseException exception)
			{
				ErrorWindow ew = new ErrorWindow("Parse error: " + exception.getMessage(), body, exception.getPosition());

				root.logEntry("ParseException: " + exception.getMessage());

				return;
			}

			dispose();
			root.addComponent(fcp);
		}

		if ("cancel".equals(e.getActionCommand()))
		{
			dispose();
			name.setText("");
		}
	}
}
