
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorNewDialog
//###########################################################################
//# $Id: EditorNewDialog.java,v 1.2 2005-02-18 03:09:06 knut Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.tree.*;
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

/** <p>A dialog to help users create new components in a module.</p>
 *
 * <p>This dialog creates new components when the "new" option is selected
 * in the module window.</p>
 *
 * @author Gian Perrone
 */
public class EditorNewDialog
	extends JDialog
	implements ActionListener
{
	public static int FOREACH = 1;
	public static int SIMPLE = 2;
	private final JTextField name = new JTextField(16);
	private final JButton okButton = new JButton("OK");
	private ButtonGroup group = new ButtonGroup();

	//  final JComboBox kindList;
	ModuleWindow root = null;
	private DefaultMutableTreeNode parentNode = null;

	public EditorNewDialog(ModuleWindow root, DefaultMutableTreeNode node)
	{
		this.root = root;
		parentNode = node;

		this.setTitle("Component Editor");

		// Center this element on the screen
		setModal(true);
		setLocationRelativeTo(null);
		JFrame.setDefaultLookAndFeelDecorated(true);

		Box b = new Box(BoxLayout.PAGE_AXIS);
		JPanel r1 = new JPanel();
		JPanel r2 = new JPanel();
		JPanel r3 = new JPanel();
		JPanel r4 = new JPanel();

		b.add(r1);
		b.add(r2);
		b.add(r3);
		b.add(r4);
		r1.add(new JLabel("<html><b>Simple Component Editor</b></html>"));
		r2.add(new JLabel("Name: "));
		r2.add(name);
		name.addActionListener(this);

		JButton b2 = new JButton("Cancel");

		okButton.setActionCommand("ok");
		b2.setActionCommand("cancel");
		okButton.addActionListener(this);
		b2.addActionListener(this);

		Box kBox = new Box(BoxLayout.PAGE_AXIS);
		JRadioButton k1 = new JRadioButton("Plant", true);

		k1.setActionCommand("Plant");
		group.add(k1);
		kBox.add(k1);

		k1 = new JRadioButton("Specification");

		k1.setActionCommand("Specification");
		group.add(k1);
		kBox.add(k1);

		k1 = new JRadioButton("Property");

		k1.setActionCommand("Property");
		group.add(k1);
		kBox.add(k1);

		//String[] kindStrings = { "Plant", "Specification", "Property" };

		/*kindList = new JComboBox(kindStrings);
		kindList.setSelectedIndex(0);
		kindList.addActionListener(this);

		r3.add(new JLabel("Kind: "));
		r3.add(kindList);*/
		r3.add(kBox);
		r4.add(okButton);
		r4.add(b2);
		name.requestFocusInWindow();
		this.setContentPane(b);
		this.pack();
		this.show();
		name.requestFocusInWindow();
	}

	public void actionPerformed(ActionEvent e)
	{

		/*if(e.getSource() == kindList) {
			String kindName = (String)kindList.getSelectedItem();

			}*/
		if ("ok".equals(e.getActionCommand()))
		{

			//TODO: Make this create the component
			final ExpressionParser parser = new ExpressionParser();

			try
			{
				SimpleExpressionProxy expr = parser.parse(name.getText());

				if (!(expr instanceof IdentifierProxy))
				{
					JOptionPane.showMessageDialog(this, "This is not valid identifier");

					return;
				}
			}
			catch (final ParseException exception)
			{

				// an error has occurred ...
				ErrorWindow ew = new ErrorWindow("Parse error in identifier: " + exception.getMessage(), name.getText(), exception.getPosition());

				return;
			}

			SimpleIdentifierProxy ip = new SimpleIdentifierProxy(name.getText());
			SimpleComponentProxy scp = new SimpleComponentProxy(ip, ComponentKind.PLANT);

			if (group.getSelection().getActionCommand().equals("Plant"))
			{
				scp.setKind(ComponentKind.PLANT);
			}

			if (group.getSelection().getActionCommand().equals("Specification"))
			{
				scp.setKind(ComponentKind.SPEC);
			}

			if (group.getSelection().getActionCommand().equals("Property"))
			{
				scp.setKind(ComponentKind.PROPERTY);
			}

			this.dispose();
			root.addComponent(scp);
		}

		if ("cancel".equals(e.getActionCommand()))
		{
			this.setVisible(false);
			name.setText("");
		}

		if (e.getSource() == name)
		{
			okButton.doClick();
		}
	}
}
