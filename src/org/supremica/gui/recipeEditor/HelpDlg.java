
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
import java.net.URL;
import javax.swing.*;
import java.awt.event.*;
import org.supremica.gui.Supremica;

public class HelpDlg
	extends JDialog
{
	JPanel panel1 = new JPanel();
	JLabel LogoLabel = new JLabel();
	JLabel TextLabel = new JLabel();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	JButton OKButton = new JButton();

	public HelpDlg(Frame frame, String title, boolean modal)
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

	public HelpDlg()
	{
		this(null, "", false);
	}

	void jbInit()
		throws Exception
	{
		panel1.setLayout(gridBagLayout1);

		ImageIcon logo = null;
		URL url = Supremica.class.getResource("/splash_v1.gif");

		if (url != null)
		{
			logo = new ImageIcon(url);
		}

		LogoLabel.setMaximumSize(new Dimension(1200, 17));
		LogoLabel.setIcon(logo);
		LogoLabel.setText("<html><font face=\"Arial\" size=\"5\">Supremica Recipe Editor</font></html>");
		TextLabel.setText("<html>" + "Create and view recipes<br>" + "</html>");
		panel1.setMaximumSize(new Dimension(5000, 5000));
		panel1.setMinimumSize(new Dimension(100, 100));
		panel1.setPreferredSize(new Dimension(550, 350));
		OKButton.setText("OK");
		OKButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				OKButton_actionPerformed(e);
			}
		});
		getContentPane().add(panel1);
		panel1.add(LogoLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panel1.add(TextLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(10, 62, 0, 0), 0, 0));
		panel1.add(OKButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

	void OKButton_actionPerformed(ActionEvent e)
	{
		try
		{
			this.dispose();    // Free system resources
		}
		catch (Exception ex) {}
	}
}
