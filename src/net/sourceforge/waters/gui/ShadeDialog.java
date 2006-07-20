
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   ShadeDialog
//###########################################################################
//# $Id: ShadeDialog.java,v 1.3 2006-07-20 02:28:37 robi Exp $
//###########################################################################
/*
package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;

public class ShadeDialog
	extends JDialog
{
	private final JColorChooser cc;
	private final JTable Table;
	private ArrayList Shades;
	private final EditorWindow r;

	public ShadeDialog(EditorWindow root)
	{
		super(root.getFrame());

		r = root;

		Container pane = this.getContentPane();
		JPanel panel = new JPanel();

		cc = new JColorChooser();
		Shades = root.getControlledSurface().getShades();
		Table = new JTable(new ShadeTable(Shades));

		Table.setDefaultRenderer(Color.class, new ColorRenderer(true));

		JButton Button = new JButton("Update");

		Button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				EditorShade s = new EditorShade("New", cc.getColor().getRGB());
				ShadeTable t = (ShadeTable) Table.getModel();

				t.addRow(s, Shades);
				Table.setModel(t);
				Table.setRowHeight(Table.getRowHeight());
			}
		});
		panel.add(cc);
		panel.add(Table);
		panel.add(Button);

		Button = new JButton("Set");

		Button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int i = Table.getSelectedRow();

				if (i == -1)
				{
					return;
				}

				EditorShade s = (EditorShade) Shades.get(i);

				r.getControlledSurface().setShade(s);
			}
		});
		panel.add(Button);
		pane.add(panel);
		this.pack();
		this.setVisible(true);
	}
}*/
