//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorNewDialog
//###########################################################################
//# $Id: EditorNewComponentDialog.java,v 1.15 2006-11-03 15:01:57 torda Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.awt.event.*;
import javax.swing.*;

import net.sourceforge.waters.gui.*;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;


/** <p>A dialog to help users create new components in a module.</p>
 *
 * <p>This dialog creates new components when the "new" option is selected
 * in the module window.</p>
 *
 * @author Gian Perrone
 */

public class EditorNewComponentDialog
	extends JDialog
	implements ActionListener
{
	private static final long serialVersionUID = 1L;

	public static int FOREACH = 1;
	public static int SIMPLE = 2;
	private final JTextField mNameInput = new JTextField(16);
	private final JButton okButton = new JButton("OK");
	private ButtonGroup group = new ButtonGroup();

	//  final JComboBox kindList;
	EditorPanelInterface mRoot = null;
//	private DefaultMutableTreeNode parentNode = null;

	public EditorNewComponentDialog(EditorPanelInterface root)
	{
		this.mRoot = root;
//		parentNode = node;

		this.setTitle("Create Component");

		// Center this element on the screen
		setModal(true);
		setLocationRelativeTo(null);

		Box b = new Box(BoxLayout.PAGE_AXIS);
		JPanel r1 = new JPanel();
		JPanel r2 = new JPanel();
		JPanel r3 = new JPanel();
		JPanel r4 = new JPanel();

		b.add(r1);
		b.add(r2);
		b.add(r3);
		b.add(r4);
//		r1.add(new JLabel("<html><b>Create Component</b></html>"));
		r2.add(new JLabel("Name: "));
		r2.add(mNameInput);
		mNameInput.addActionListener(this);

		JButton b2 = new JButton("Cancel");

		okButton.setActionCommand("ok");
		b2.setActionCommand("cancel");
		okButton.addActionListener(this);
		b2.addActionListener(this);

		Box kBox = new Box(BoxLayout.PAGE_AXIS);
		JRadioButton k1 = new JRadioButton("Plant", false);

		k1.setActionCommand("Plant");
		group.add(k1);
		kBox.add(k1);

		k1 = new JRadioButton("Specification", true);

		k1.setActionCommand("Specification");
		group.add(k1);
		kBox.add(k1);
/*
		k1 = new JRadioButton("Property");

		k1.setActionCommand("Property");
		group.add(k1);
		kBox.add(k1);
*/
		//String[] kindStrings = { "Plant", "Specification", "Property" };

		/*kindList = new JComboBox(kindStrings);
		kindList.setSelectedIndex(0);
		kindList.addActionListener(this);

		r3.add(new JLabel("Kind: "));
		r3.add(kindList);*/
		r3.add(kBox);
		r4.add(okButton);
		r4.add(b2);
		mNameInput.requestFocusInWindow();
		this.setContentPane(b);
		this.pack();
		this.setVisible(true);
		mNameInput.requestFocusInWindow();
	}

	public void actionPerformed(ActionEvent e)
	{

		if ("ok".equals(e.getActionCommand()))
		{

			//TODO: Make this create the component
			final ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());
			final String nameText = mNameInput.getText();
			if (nameText == null || nameText.equals(""))
			{
            	JOptionPane.showMessageDialog(this, "No name specified", "No name", JOptionPane.ERROR_MESSAGE);
				return;
			}

			IdentifierProxy ident = null;
			try {
				ident = parser.parseIdentifier(nameText);
			} catch (final ParseException exception) {
				ErrorWindow.askRevert(exception, nameText);
				return;
			}

			if (!mRoot.componentNameAvailable(nameText))
			{
            	JOptionPane.showMessageDialog(this, "Duplicate name: " + nameText, "Duplicate Name", JOptionPane.ERROR_MESSAGE);
				return;
			}


			ComponentKind kind;
			final String key = group.getSelection().getActionCommand();
			if (key.equals("Plant")) {
				kind = ComponentKind.PLANT;
			} else if (key.equals("Specification")) {
				kind = ComponentKind.SPEC;
			} else if (key.equals("Property")) {
				kind = ComponentKind.PROPERTY;
			} else {
				throw new IllegalStateException("No component kind selected!");
			}
			final GraphSubject graph = new GraphSubject();

			final SimpleComponentSubject comp =
				new SimpleComponentSubject(ident, kind, graph);
			//TODO: Make this create the component

			mRoot.addComponent(comp);
			dispose();
/*
			final Collection<Proxy> empty = Collections.emptyList();
			final LabelBlockSubject blocked =
				new LabelBlockSubject(empty, null);
			final Collection<NodeProxy> nodes = Collections.emptyList();
			final Collection<EdgeProxy> edges = Collections.emptyList();
			final GraphSubject graph =
				new GraphSubject(true, blocked, nodes, edges);
			final SimpleComponentSubject comp =
				new SimpleComponentSubject(ident, kind, graph);
			dispose();
			mRoot.addComponent(comp);
*/
		}

		if ("cancel".equals(e.getActionCommand()))
		{
			this.setVisible(false);
			mNameInput.setText("");
		}

		if (e.getSource() == mNameInput)
		{
			okButton.doClick();
		}
	}
}
