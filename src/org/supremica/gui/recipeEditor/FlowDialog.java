
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
import javax.swing.*;
import java.awt.event.*;

public class FlowDialog
	extends JDialog
{
	JPanel panel1 = new JPanel();
	JLabel jLabel1 = new JLabel();
	JTextField labelField = new JTextField();
	public FlowLink myObject;
	JButton OKButton = new JButton();
	GridBagLayout gridBagLayout1 = new GridBagLayout();

	public FlowDialog(Frame frame, String title, boolean modal)
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

	public FlowDialog(Frame frame, FlowLink obj)
	{
		super(frame, "Flow Properties", true);

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

	public FlowDialog()
	{
		this(null, "", false);
	}

	void jbInit()
		throws Exception
	{
		panel1.setLayout(gridBagLayout1);
		jLabel1.setText("Label:");
		labelField.setText("jTextField1");
		OKButton.setText("OK");
		OKButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				OKButton_actionPerformed(e);
			}
		});
		getContentPane().add(panel1);
		panel1.add(OKButton, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(13, 57, 18, 87), 4, 0));
		panel1.add(labelField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(12, 0, 0, 22), 21, 0));
		panel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(12, 15, 0, 0), 5, 0));
	}

	void updateDialog()
	{
		if (myObject == null)
		{
			return;
		}

		labelField.setText(myObject.getText());
		labelField.setEnabled(myObject.getDocument().isModifiable());
	}

	void OKButton_actionPerformed(ActionEvent e)
	{
		try
		{
			this.dispose();    // Free system resources
			myObject.setText(labelField.getText());
		}
		catch (Exception ex) {}
	}
}
