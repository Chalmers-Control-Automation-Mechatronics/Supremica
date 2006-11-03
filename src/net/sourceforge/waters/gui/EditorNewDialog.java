//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorNewDialog
//###########################################################################
//# $Id: EditorNewDialog.java,v 1.9 2006-11-03 15:01:56 torda Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * <p>A dialog to help users create new components in a module.</p>
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

	public EditorNewDialog(ModuleWindow root, DefaultMutableTreeNode node)
	{
		setTitle("Component Editor");

		mRoot = root;
		mParentNode = node;

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
		r1.add(new JLabel("<html><b>Simple Component Editor</b></html>"));
		r2.add(new JLabel("Name: "));
		r2.add(mNameInput);
		mNameInput.addActionListener(this);

		JButton b2 = new JButton("Cancel");
		b2.setActionCommand("cancel");
		b2.addActionListener(this);
		//b2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		okButton = new JButton("OK");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);

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
		k1.setEnabled(!ModuleWindow.DES_COURSE_VERSION);
		group.add(k1);
		kBox.add(k1);

		r3.add(kBox);
		r4.add(okButton);
		r4.add(b2);
		mNameInput.requestFocusInWindow();
		setContentPane(b);
		pack();
		setVisible(true);
	}


	public void actionPerformed(ActionEvent e)
	{

		if ("ok".equals(e.getActionCommand())) {
			final ExpressionParser parser = mRoot.getExpressionParser();
			final String text = mNameInput.getText();
			IdentifierProxy ident = null;
			try {
				ident = parser.parseIdentifier(text);
			} catch (final ParseException exception) {
				ErrorWindow.askRevert(exception, text);
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
		}

		if ("cancel".equals(e.getActionCommand()))
		{
			setVisible(false);
			mNameInput.setText("");
		}

		if (e.getSource() == mNameInput)
		{
			okButton.doClick();
		}
	}


	//########################################################################
	//# Data Members
	private final ModuleWindow mRoot;
	private final DefaultMutableTreeNode mParentNode;
	private final JTextField mNameInput = new JTextField(16);
	private final JButton okButton;
	private final ButtonGroup group = new ButtonGroup();

	private static final int FOREACH = 1;
	private static final int SIMPLE = 2;

}
