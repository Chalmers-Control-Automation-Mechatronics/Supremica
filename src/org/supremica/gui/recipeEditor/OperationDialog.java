
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

public class OperationDialog
	extends JDialog
{
	public OperationNode myObject;
	JPanel panel1 = new JPanel();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JButton OKButton = new JButton();
	JButton CancelButton = new JButton();
	GridBagLayout gridBagLayout1 = new GridBagLayout();

	public OperationDialog(Frame frame, String title, boolean modal)
	{
		super(frame, title, modal);

		try
		{
			jbInit();
			pack();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public OperationDialog()
	{
		this(null, "", false);
	}

	public OperationDialog(Frame frame, OperationNode obj)
	{
		super(frame, "Operation Properties", true);

		try
		{
			myObject = obj;

			jbInit();
			pack();
			updateDialog();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	void jbInit()
		throws Exception
	{
		panel1.setLayout(gridBagLayout1);
		jLabel1.setText("Add your node-specific properties here");
		OKButton.setText("OK");
		OKButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				OKButton_actionPerformed(e);
			}
		});
		CancelButton.setText("Cancel");
		CancelButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				CancelButton_actionPerformed(e);
			}
		});
		getContentPane().add(panel1);
		panel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(30, 32, 0, 13), 0, 0));
		panel1.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(31, 32, 0, 0), 0, 0));
		panel1.add(OKButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(18, 50, 32, 11), 0, 0));
		panel1.add(CancelButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(18, 7, 32, 72), 0, 0));
	}

	void updateDialog()
	{
		if (myObject == null)
		{
			return;
		}
	}

	void updateData()
	{
		if (myObject == null)
		{
			return;
		}
	}

	void OKButton_actionPerformed(ActionEvent e)
	{
		try
		{
			updateData();
			this.dispose();    // Free system resources
		}
		catch (Exception ex) {}
	}

	void CancelButton_actionPerformed(ActionEvent e)
	{
		try
		{
			this.dispose();    // Free system resources
		}
		catch (Exception ex) {}
	}
}
