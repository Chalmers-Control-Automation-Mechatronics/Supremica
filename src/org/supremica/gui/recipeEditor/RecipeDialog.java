
/*
 *  Copyright © Northwoods Software Corporation, 2000-2002. All Rights
 *  Reserved.
 *
 *  Restricted Rights: Use, duplication, or disclosure by the U.S.
 *  Government is subject to restrictions as set forth in subparagraph
 *  (c) (1) (ii) of DFARS 252.227-7013, or in FAR 52.227-19, or in FAR
 *  52.227-14 Alt. III, as applicable.
 *
 */
package org.supremica.gui.recipeEditor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.nwoods.jgo.*;

public class RecipeDialog
	extends JDialog
{
	JPanel panel1 = new JPanel();
	JButton OKButton = new JButton();
	JButton CancelButton = new JButton();
	JLabel jLabel1 = new JLabel();
	JCheckBox readonly = new JCheckBox();
	public RecipeDocument myObject;

	public RecipeDialog(Frame frame, RecipeDocument obj)
	{
		super(frame, "Recipe Properties", true);

		try
		{
			myObject = obj;

			init();
			pack();
			updateDialog();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public RecipeDialog()
	{
		super((Frame) null, "Recipe Properties", true);
	}

	private final void init()
	{
		panel1.setLayout(null);
		panel1.setMinimumSize(new Dimension(294, 241));
		panel1.setPreferredSize(new Dimension(294, 241));
		OKButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				OnOK();
			}
		});
		CancelButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				OnCancel();
			}
		});
		getContentPane().add(panel1);
		OKButton.setText("OK");
		panel1.add(OKButton);
		OKButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		OKButton.setBounds(new Rectangle(60, 204, 79, 22));
		CancelButton.setText("Cancel");
		panel1.add(CancelButton);
		CancelButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		CancelButton.setBounds(new Rectangle(168, 204, 79, 22));
		jLabel1.setText("Add your own document-specific properties here");
		jLabel1.setBounds(new Rectangle(20, 20, 400, 50));
		panel1.add(jLabel1);
		readonly.setText("Read Only");
		readonly.setBounds(new Rectangle(50, 150, 100, 14));
		panel1.add(readonly);
	}

	void updateDialog()
	{
		if (myObject == null)
		{
			return;
		}

		readonly.setSelected(!myObject.isModifiable());
	}

	void updateData()
	{
		if (myObject == null)
		{
			return;
		}

		myObject.setModifiable(!readonly.isSelected());
	}

	public void addNotify()
	{

		// Record the size of the window prior to calling parents addNotify.
		Dimension d = getSize();

		super.addNotify();

		if (fComponentsAdjusted)
		{
			return;
		}

		// Adjust components according to the insets
		Insets insets = getInsets();

		setSize(insets.left + insets.right + d.width, insets.top + insets.bottom + d.height);

		Component components[] = getComponents();

		for (int i = 0; i < components.length; i++)
		{
			Point p = components[i].getLocation();

			p.translate(insets.left, insets.top);
			components[i].setLocation(p);
		}

		fComponentsAdjusted = true;
	}

	// Used for addNotify check.
	boolean fComponentsAdjusted = false;

	void OnOK()
	{
		try
		{
			updateData();
			this.dispose();    // Free system resources
		}
		catch (Exception e) {}
	}

	void OnCancel()
	{
		try
		{
			this.dispose();    // Free system resources
		}
		catch (Exception e) {}
	}
}
