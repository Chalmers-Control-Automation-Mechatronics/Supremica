
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

import com.nwoods.jgo.layout.JGoLayeredDigraphAutoLayout;
import com.nwoods.jgo.layout.JGoNetwork;


public class LayerDialog
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
	ButtonGroup group1 = new ButtonGroup();
	ButtonGroup group2 = new ButtonGroup();
	ButtonGroup group3 = new ButtonGroup();
	ButtonGroup group4 = new ButtonGroup();
	JRadioButton greedy = new JRadioButton();
	JRadioButton depthFirst = new JRadioButton();
	JRadioButton sink = new JRadioButton();
	JRadioButton source = new JRadioButton();
	JRadioButton length = new JRadioButton();
	JRadioButton naive = new JRadioButton();
	JRadioButton dfsout = new JRadioButton();
	JRadioButton dfsin = new JRadioButton();
	JRadioButton up = new JRadioButton();
	JRadioButton down = new JRadioButton();
	JRadioButton left = new JRadioButton();
	JRadioButton right = new JRadioButton();
	JTextField iter = new JTextField();
	JTextField layer = new JTextField();
	JTextField column = new JTextField();
	JCheckBox aggressive = new JCheckBox();
	JButton OKButton = new JButton();
	JButton CancelButton = new JButton();

	public LayerDialog(Frame frame, String title, boolean modal, AutomatonView view, AutomataEditor app)
	{

		super(frame, title, modal);

		try
		{
			myApp = app;
			myView = view;

			jbInit();
			pack();
			UpdateDialog();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public LayerDialog()
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
		panel1.setMinimumSize(new Dimension(480, 400));
		panel1.setPreferredSize(new Dimension(480, 400));
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
		OKButton.setBounds(new Rectangle(10, 370, 80, 22));
		CancelButton.setText("Cancel");
		CancelButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		CancelButton.setBounds(new Rectangle(390, 370, 80, 22));
		panel1.add(OKButton);
		panel1.add(CancelButton);
		panel2.setBounds(new Rectangle(10, 20, 220, 80));
		panel2.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel2);
		panel3.setBounds(new Rectangle(250, 20, 220, 80));
		panel3.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel3);
		panel4.setBounds(new Rectangle(10, 120, 220, 100));
		panel4.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel4);
		panel5.setBounds(new Rectangle(10, 240, 220, 100));
		panel5.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel5);
		panel6.setBounds(new Rectangle(250, 120, 220, 220));
		panel6.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel1.add(panel6);
		panel7.setBounds(new Rectangle(10, 20, 200, 80));
		panel7.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel6.add(panel7);
		panel8.setBounds(new Rectangle(10, 120, 200, 80));
		panel8.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		panel6.add(panel8);
		label1.setText("Cycle Remove Options");
		label1.setBounds(new Rectangle(15, 1, 200, 20));
		panel1.add(label1);
		label2.setText("Crossing Reduction Options");
		label2.setBounds(new Rectangle(255, 1, 200, 20));
		panel1.add(label2);
		label3.setText("Layering Options");
		label3.setBounds(new Rectangle(15, 101, 200, 20));
		panel1.add(label3);
		label4.setText("Layout Options");
		label4.setBounds(new Rectangle(255, 101, 200, 20));
		panel1.add(label4);
		label5.setText("Initialize Options");
		label5.setBounds(new Rectangle(15, 221, 150, 20));
		panel1.add(label5);
		label6.setText("Spacing");
		label6.setBounds(new Rectangle(15, 1, 200, 20));
		panel6.add(label6);
		label7.setText("Direction");
		label7.setBounds(new Rectangle(15, 101, 200, 20));
		panel6.add(label7);
		greedy.setText("Greedy");
		greedy.setBounds(new Rectangle(10, 10, 200, 20));
		group1.add(greedy);
		panel2.add(greedy);
		depthFirst.setText("Depth First Search");
		depthFirst.setBounds(new Rectangle(10, 40, 200, 20));
		depthFirst.setSelected(true);
		group1.add(depthFirst);
		panel2.add(depthFirst);
		label8.setText("Iterations");
		label8.setBounds(new Rectangle(10, 10, 150, 20));
		panel3.add(label8);
		iter.setText("4");
		iter.setBounds(new Rectangle(130, 10, 80, 20));
		panel3.add(iter);
		aggressive.setText("Aggressive");
		aggressive.setBounds(new Rectangle(10, 40, 200, 20));
		panel3.add(aggressive);
		sink.setText("Longest Path Sink");
		sink.setBounds(new Rectangle(10, 10, 200, 20));
		group2.add(sink);
		panel4.add(sink);
		source.setText("Longest Path Source");
		source.setBounds(new Rectangle(10, 40, 200, 20));
		group2.add(source);
		panel4.add(source);
		length.setText("Optimal Link Length");
		length.setBounds(new Rectangle(10, 70, 200, 20));
		length.setSelected(true);
		group2.add(length);
		panel4.add(length);
		naive.setText("Naive");
		naive.setBounds(new Rectangle(10, 10, 200, 20));
		group3.add(naive);
		panel5.add(naive);
		dfsout.setText("Depth First Search Outward");
		dfsout.setBounds(new Rectangle(10, 40, 200, 20));
		dfsout.setSelected(true);
		group3.add(dfsout);
		panel5.add(dfsout);
		dfsin.setText("Depth First Search Inward");
		dfsin.setBounds(new Rectangle(10, 70, 200, 20));
		group3.add(dfsin);
		panel5.add(dfsin);
		label9.setText("layerSpacing");
		label9.setBounds(new Rectangle(10, 10, 120, 20));
		panel7.add(label9);
		label10.setText("columnSpacing");
		label10.setBounds(new Rectangle(10, 40, 120, 20));
		panel7.add(label10);
		layer.setText("20");
		layer.setBounds(new Rectangle(120, 10, 70, 20));
		panel7.add(layer);
		column.setText("20");
		column.setBounds(new Rectangle(120, 40, 70, 20));
		panel7.add(column);
		up.setText("Up");
		up.setBounds(new Rectangle(10, 10, 90, 20));
		up.setSelected(true);
		group4.add(up);
		panel8.add(up);
		down.setText("Down");
		down.setBounds(new Rectangle(10, 40, 90, 20));
		group4.add(down);
		panel8.add(down);
		left.setText("Left");
		left.setBounds(new Rectangle(100, 10, 90, 20));
		group4.add(left);
		panel8.add(left);
		right.setText("Right");
		right.setBounds(new Rectangle(100, 40, 90, 20));
		group4.add(right);
		panel8.add(right);
	}

	void UpdateDialog() {}

	void UpdateControl()
	{

		if (myView != null)
		{
			int cycle, layering, initialize, direction, agr;

			if (greedy.isSelected())
			{
				cycle = JGoLayeredDigraphAutoLayout.LD_CYCLEREMOVE_GREEDY;
			}
			else
			{
				cycle = JGoLayeredDigraphAutoLayout.LD_CYCLEREMOVE_DFS;
			}

			if (sink.isSelected())
			{
				layering = JGoLayeredDigraphAutoLayout.LD_LAYERING_LONGESTPATHSINK;
			}
			else if (source.isSelected())
			{
				layering = JGoLayeredDigraphAutoLayout.LD_LAYERING_LONGESTPATHSOURCE;
			}
			else
			{
				layering = JGoLayeredDigraphAutoLayout.LD_LAYERING_OPTIMALLINKLENGTH;
			}

			if (naive.isSelected())
			{
				initialize = JGoLayeredDigraphAutoLayout.LD_INITIALIZE_NAIVE;
			}
			else if (dfsout.isSelected())
			{
				initialize = JGoLayeredDigraphAutoLayout.LD_INITIALIZE_DFSOUT;
			}
			else
			{
				initialize = JGoLayeredDigraphAutoLayout.LD_INITIALIZE_DFSIN;
			}

			if (up.isSelected())
			{
				direction = JGoLayeredDigraphAutoLayout.LD_DIRECTION_UP;
			}
			else if (down.isSelected())
			{
				direction = JGoLayeredDigraphAutoLayout.LD_DIRECTION_DOWN;
			}
			else if (left.isSelected())
			{
				direction = JGoLayeredDigraphAutoLayout.LD_DIRECTION_LEFT;
			}
			else
			{
				direction = JGoLayeredDigraphAutoLayout.LD_DIRECTION_RIGHT;
			}

			if (aggressive.isSelected())
			{
				agr = JGoLayeredDigraphAutoLayout.LD_AGGRESSIVE_TRUE;
			}
			else
			{
				agr = JGoLayeredDigraphAutoLayout.LD_AGGRESSIVE_FALSE;
			}

			AutomatonDocument doc = myView.getDoc();

			myAutoLayout = new SimpleLDAL(doc, Integer.parseInt(layer.getText()), Integer.parseInt(column.getText()), direction, cycle, layering, initialize, Integer.parseInt(iter.getText()), agr, myApp);
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

			setLocation(bounds.x + 100, bounds.y + 100);
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
			UpdateControl();
			this.dispose();		// Free system resources

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
			this.dispose();		// Free system resources
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
	private SimpleLDAL myAutoLayout = null;
}
