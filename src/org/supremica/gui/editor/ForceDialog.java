
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.gui.editor;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class ForceDialog
	extends JDialog
{
	JPanel panel1 = new JPanel();
	JPanel panel2 = new JPanel();
	JPanel panel3 = new JPanel();
	JPanel panel4 = new JPanel();
	JPanel panel5 = new JPanel();
	JPanel panel6 = new JPanel();
	JPanel panel7 = new JPanel();
	JPanel panel8 = new JPanel();
	JPanel panel9 = new JPanel();
	JPanel panel10 = new JPanel();
	JPanel panel11 = new JPanel();
	JPanel panel12 = new JPanel();
	JLabel label1 = new JLabel();
	JLabel label2 = new JLabel();
	JLabel label3 = new JLabel();
	JLabel label4 = new JLabel();
	JLabel label5 = new JLabel();
	JLabel label6 = new JLabel();
	JLabel label7 = new JLabel();
	JLabel label8 = new JLabel();
	JLabel label9 = new JLabel();
	JLabel label10 = new JLabel();
	JLabel label11 = new JLabel();
	JLabel label12 = new JLabel();
	JLabel label13 = new JLabel();
	JLabel label14 = new JLabel();
	JLabel label15 = new JLabel();
	JLabel label16 = new JLabel();
	JLabel label17 = new JLabel();
	JLabel label18 = new JLabel();
	JLabel label19 = new JLabel();
	JLabel label20 = new JLabel();
	JLabel label21 = new JLabel();
	JLabel label22 = new JLabel();
	JLabel label23 = new JLabel();
	JLabel label24 = new JLabel();
	JLabel label25 = new JLabel();
	JLabel label26 = new JLabel();
	JLabel label27 = new JLabel();
	JLabel label28 = new JLabel();
	JLabel label29 = new JLabel();
	JLabel label30 = new JLabel();
	JLabel label31 = new JLabel();
	JLabel label32 = new JLabel();
	JLabel label33 = new JLabel();
	JLabel label34 = new JLabel();
	JLabel label35 = new JLabel();
	JLabel label36 = new JLabel();
	JTextField iter = new JTextField();
	JTextField gravx = new JTextField();
	JTextField gravy = new JTextField();
	JTextField redCharge = new JTextField();
	JTextField redMass = new JTextField();
	JTextField greenCharge = new JTextField();
	JTextField greenMass = new JTextField();
	JTextField blueCharge = new JTextField();
	JTextField blueMass = new JTextField();
	JTextField rrlen = new JTextField();
	JTextField rrstiff = new JTextField();
	JTextField rglen = new JTextField();
	JTextField rgstiff = new JTextField();
	JTextField rblen = new JTextField();
	JTextField rbstiff = new JTextField();
	JTextField gglen = new JTextField();
	JTextField ggstiff = new JTextField();
	JTextField gblen = new JTextField();
	JTextField gbstiff = new JTextField();
	JTextField bblen = new JTextField();
	JTextField bbstiff = new JTextField();
	JCheckBox redFix = new JCheckBox();
	JCheckBox greenFix = new JCheckBox();
	JCheckBox blueFix = new JCheckBox();
	JButton OKButton = new JButton();
	JButton CancelButton = new JButton();

	public ForceDialog(Frame frame, String title, boolean modal, AutomatonView view, AutomataEditor app)
	{
		super(frame, title, modal);

		try
		{
			myView = view;
			myApp = app;

			jbInit();
			pack();
			UpdateDialog();
		}
		catch (Exception ex)
		{
			// logger.debug(ex.getStackTrace());
			throw new RuntimeException(ex);
		}
	}

	public ForceDialog()
	{
		this(null, "", false, null, null);
	}

	void jbInit()
		throws Exception
	{
		panel1.setLayout(null);
		panel2.setLayout(null);
		panel3.setLayout(null);
		panel4.setLayout(null);
		panel5.setLayout(null);
		panel6.setLayout(null);
		panel7.setLayout(null);
		panel8.setLayout(null);
		panel9.setLayout(null);
		panel10.setLayout(null);
		panel11.setLayout(null);
		panel12.setLayout(null);
		panel1.setMinimumSize(new Dimension(700, 460));
		panel1.setPreferredSize(new Dimension(700, 460));
		OKButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				OKButton_actionPerformed(e);
			}
		});
		CancelButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				CancelButton_actionPerformed(e);
			}
		});
		OKButton.addKeyListener(new java.awt.event.KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				OKButton_keyPressed(e);
			}
		});
		CancelButton.addKeyListener(new java.awt.event.KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				CancelButton_keyPressed(e);
			}
		});
		getContentPane().add(panel1);
		OKButton.setText("OK");
		OKButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		OKButton.setBounds(new Rectangle(10, 430, 80, 22));
		panel1.add(OKButton);
		CancelButton.setText("Cancel");
		CancelButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		CancelButton.setBounds(new Rectangle(610, 430, 80, 22));
		panel1.add(CancelButton);
		panel2.setBounds(new Rectangle(10, 20, 220, 80));
		panel2.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel2);
		panel3.setBounds(new Rectangle(240, 20, 220, 80));
		panel3.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel3);
		panel4.setBounds(new Rectangle(10, 120, 220, 100));
		panel4.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel4);
		panel5.setBounds(new Rectangle(240, 120, 220, 100));
		panel5.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel5);
		panel6.setBounds(new Rectangle(470, 120, 220, 100));
		panel6.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel6);
		panel7.setBounds(new Rectangle(10, 240, 220, 80));
		panel7.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel7);
		panel8.setBounds(new Rectangle(240, 240, 220, 80));
		panel8.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel8);
		panel9.setBounds(new Rectangle(470, 240, 220, 80));
		panel9.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel9);
		panel10.setBounds(new Rectangle(10, 340, 220, 80));
		panel10.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel10);
		panel11.setBounds(new Rectangle(240, 340, 220, 80));
		panel11.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel11);
		panel12.setBounds(new Rectangle(470, 340, 220, 80));
		panel12.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel12);
		label1.setText("Global Options");
		label1.setBounds(new Rectangle(15, 1, 150, 20));
		panel1.add(label1);
		label2.setText("Gravitational Options");
		label2.setBounds(new Rectangle(245, 1, 150, 20));
		panel1.add(label2);
		label3.setText("Red Options");
		label3.setBounds(new Rectangle(15, 101, 150, 20));
		panel1.add(label3);
		label4.setText("Green Options");
		label4.setBounds(new Rectangle(245, 101, 150, 20));
		panel1.add(label4);
		label5.setText("Blue Options");
		label5.setBounds(new Rectangle(475, 101, 150, 20));
		panel1.add(label5);
		label7.setText("Red-Red Options");
		label7.setBounds(new Rectangle(15, 221, 150, 20));
		panel1.add(label7);
		label8.setText("Red-Green Options");
		label8.setBounds(new Rectangle(245, 221, 150, 20));
		panel1.add(label8);
		label9.setText("Red-Blue Options");
		label9.setBounds(new Rectangle(475, 221, 150, 20));
		panel1.add(label9);
		label10.setText("Green-Green Options");
		label10.setBounds(new Rectangle(15, 321, 150, 20));
		panel1.add(label10);
		label11.setText("Green-Blue Options");
		label11.setBounds(new Rectangle(245, 321, 150, 20));
		panel1.add(label11);
		label12.setText("Blue-Blue Options");
		label12.setBounds(new Rectangle(475, 321, 150, 20));
		panel1.add(label12);
		label13.setText("max_iterations");
		label13.setBounds(new Rectangle(10, 10, 150, 20));
		panel2.add(label13);
		label14.setText("gravitationalFieldX");
		label14.setBounds(new Rectangle(10, 10, 150, 20));
		panel3.add(label14);
		label15.setText("gravitationalFieldY");
		label15.setBounds(new Rectangle(10, 40, 150, 20));
		panel3.add(label15);
		label16.setText("electricalCharge");
		label16.setBounds(new Rectangle(10, 10, 150, 20));
		panel4.add(label16);
		label17.setText("gravitationalMass");
		label17.setBounds(new Rectangle(10, 40, 150, 20));
		panel4.add(label17);
		label18.setText("fixed");
		label18.setBounds(new Rectangle(10, 70, 150, 20));
		panel4.add(label18);
		label19.setText("electricalCharge");
		label19.setBounds(new Rectangle(10, 10, 150, 20));
		panel5.add(label19);
		label20.setText("gravitationalMass");
		label20.setBounds(new Rectangle(10, 40, 150, 20));
		panel5.add(label20);
		label21.setText("fixed");
		label21.setBounds(new Rectangle(10, 70, 150, 20));
		panel5.add(label21);
		label22.setText("electricalCharge");
		label22.setBounds(new Rectangle(10, 10, 150, 20));
		panel6.add(label22);
		label23.setText("gravitationalMass");
		label23.setBounds(new Rectangle(10, 40, 150, 20));
		panel6.add(label23);
		label24.setText("fixed");
		label24.setBounds(new Rectangle(10, 70, 150, 20));
		panel6.add(label24);
		label25.setText("springLength");
		label25.setBounds(new Rectangle(10, 10, 150, 20));
		panel7.add(label25);
		label26.setText("springStiffness");
		label26.setBounds(new Rectangle(10, 40, 150, 20));
		panel7.add(label26);
		label27.setText("springLength");
		label27.setBounds(new Rectangle(10, 10, 150, 20));
		panel8.add(label27);
		label28.setText("springStiffness");
		label28.setBounds(new Rectangle(10, 40, 150, 20));
		panel8.add(label28);
		label29.setText("springLength");
		label29.setBounds(new Rectangle(10, 10, 150, 20));
		panel9.add(label29);
		label30.setText("springStiffness");
		label30.setBounds(new Rectangle(10, 40, 150, 20));
		panel9.add(label30);
		label31.setText("springLength");
		label31.setBounds(new Rectangle(10, 10, 150, 20));
		panel10.add(label31);
		label32.setText("springStiffness");
		label32.setBounds(new Rectangle(10, 40, 150, 20));
		panel10.add(label32);
		label33.setText("springLength");
		label33.setBounds(new Rectangle(10, 10, 150, 20));
		panel11.add(label33);
		label34.setText("springStiffness");
		label34.setBounds(new Rectangle(10, 40, 150, 20));
		panel11.add(label34);
		label35.setText("springLength");
		label35.setBounds(new Rectangle(10, 10, 150, 20));
		panel12.add(label35);
		label36.setText("springStiffness");
		label36.setBounds(new Rectangle(10, 40, 150, 20));
		panel12.add(label36);
		iter.setBounds(new Rectangle(140, 10, 70, 20));
		iter.setText("1000");
		panel2.add(iter);
		gravx.setBounds(new Rectangle(140, 10, 70, 20));
		gravx.setText("0.0");
		panel3.add(gravx);
		gravy.setBounds(new Rectangle(140, 40, 70, 20));
		gravy.setText("0.0");
		panel3.add(gravy);
		redCharge.setBounds(new Rectangle(140, 10, 70, 20));
		redCharge.setText("150.0");
		panel4.add(redCharge);
		redMass.setBounds(new Rectangle(140, 40, 70, 20));
		redMass.setText("0.0");
		panel4.add(redMass);
		redFix.setBounds(new Rectangle(140, 66, 24, 24));
		panel4.add(redFix);
		greenCharge.setBounds(new Rectangle(140, 10, 70, 20));
		greenCharge.setText("150.0");
		panel5.add(greenCharge);
		greenMass.setBounds(new Rectangle(140, 40, 70, 20));
		greenMass.setText("0.0");
		panel5.add(greenMass);
		greenFix.setBounds(new Rectangle(140, 66, 24, 24));
		panel5.add(greenFix);
		blueCharge.setBounds(new Rectangle(140, 10, 70, 20));
		blueCharge.setText("150.0");
		panel6.add(blueCharge);
		blueMass.setBounds(new Rectangle(140, 40, 70, 20));
		blueMass.setText("0.0");
		panel6.add(blueMass);
		blueFix.setBounds(new Rectangle(140, 66, 24, 24));
		panel6.add(blueFix);
		blueFix.setSelected(true);
		rrlen.setBounds(new Rectangle(140, 10, 70, 20));
		rrlen.setText("50.0");
		panel7.add(rrlen);
		rrstiff.setBounds(new Rectangle(140, 40, 70, 20));
		rrstiff.setText("0.05");
		panel7.add(rrstiff);
		rglen.setBounds(new Rectangle(140, 10, 70, 20));
		rglen.setText("50.0");
		panel8.add(rglen);
		rgstiff.setBounds(new Rectangle(140, 40, 70, 20));
		rgstiff.setText("0.05");
		panel8.add(rgstiff);
		rblen.setBounds(new Rectangle(140, 10, 70, 20));
		rblen.setText("50.0");
		panel9.add(rblen);
		rbstiff.setBounds(new Rectangle(140, 40, 70, 20));
		rbstiff.setText("0.05");
		panel9.add(rbstiff);
		gglen.setBounds(new Rectangle(140, 10, 70, 20));
		gglen.setText("50.0");
		panel10.add(gglen);
		ggstiff.setBounds(new Rectangle(140, 40, 70, 20));
		ggstiff.setText("0.05");
		panel10.add(ggstiff);
		gblen.setBounds(new Rectangle(140, 10, 70, 20));
		gblen.setText("50.0");
		panel11.add(gblen);
		gbstiff.setBounds(new Rectangle(140, 40, 70, 20));
		gbstiff.setText("0.05");
		panel11.add(gbstiff);
		bblen.setBounds(new Rectangle(140, 10, 70, 20));
		bblen.setText("50.0");
		panel12.add(bblen);
		bbstiff.setBounds(new Rectangle(140, 40, 70, 20));
		bbstiff.setText("0.05");
		panel12.add(bbstiff);
	}

	void UpdateDialog() {}

	void UpdateControl()
	{
		if (myView != null)
		{
			AutomatonDocument doc = myView.getDoc();

			myAutoLayout = new SimpleFDAL(doc, Integer.parseInt(iter.getText()), Double.parseDouble(gravx.getText()), Double.parseDouble(gravy.getText()), Double.parseDouble(redCharge.getText()), Double.parseDouble(redMass.getText()), redFix.isSelected(), Double.parseDouble(greenCharge.getText()), Double.parseDouble(greenMass.getText()), greenFix.isSelected(), Double.parseDouble(blueCharge.getText()), Double.parseDouble(blueMass.getText()), blueFix.isSelected(), Double.parseDouble(rrlen.getText()), Double.parseDouble(rrstiff.getText()), Double.parseDouble(rglen.getText()), Double.parseDouble(rgstiff.getText()), Double.parseDouble(rblen.getText()), Double.parseDouble(rbstiff.getText()), Double.parseDouble(gglen.getText()), Double.parseDouble(ggstiff.getText()), Double.parseDouble(gblen.getText()), Double.parseDouble(gbstiff.getText()), Double.parseDouble(bblen.getText()), Double.parseDouble(bbstiff.getText()), myApp);
		}
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

	/**
	 * Shows or hides the component depending on the boolean flag b.
	 * @param b  if true, show the component; otherwise, hide the component.
	 * @see javax.swing.JComponent#isVisible
	 */
	public void setVisible(boolean b)
	{
		if (b)
		{
			Rectangle bounds = getParent().getBounds();

			setLocation(bounds.x + 100, bounds.y + 60);
		}

		super.setVisible(b);
	}

	void OKButton_actionPerformed(ActionEvent e)
	{
		OnOK();
	}

	void OnOK()
	{
		try
		{
			setVisible(false);
			UpdateControl();
			this.dispose();    // Free system resources

			if (myAutoLayout != null)
			{
				myView.setDefaultCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				myAutoLayout.performLayout();
				myView.zoomToFit();
				myView.setDefaultCursor(null);
			}
		}
		catch (Exception e) {}
	}

	void CancelButton_actionPerformed(ActionEvent e)
	{
		OnCancel();
	}

	void OnCancel()
	{
		try
		{
			this.dispose();    // Free system resources
		}
		catch (Exception e) {}
	}

	void OKButton_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
		{
			OnOK();
		}
		else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE)
		{
			OnCancel();
		}
	}

	void CancelButton_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER)
		{
			OnCancel();
		}
		else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE)
		{
			OnCancel();
		}
	}

	private AutomatonView myView = null;
	private AutomataEditor myApp = null;
	private SimpleFDAL myAutoLayout = null;
}
