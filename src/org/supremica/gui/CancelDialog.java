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

package org.supremica.gui;

import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.Stoppable;

import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
// import javax.swing.text.JTextComponent;
// import javax.swing.event.*;
// import javax.swing.table.*;

public class CancelDialog 
	implements ActionListener
	// extends JOptionPane
	// implements Runnable
{
	//private JButton cancelButton;
	private JDialog dialog;
	// private Thread[] executers;
	private ArrayList executers;
	private JOptionPane optionPane = new JOptionPane();
	private JButton stopButton;
	// private JTextComponent messageComponent;
	private JPanel counterPanel;
	private JLabel counterLabel;
	private JLabel headerLabel;
	private JProgressBar progressBar;
	private Supremica workbench;
	
	/**
	 * Creates (modal?) dialog box for canceling the threads in the supplied ArrayList
	 */
	public CancelDialog(Supremica workbench, ArrayList executers)
	{
		this.workbench = workbench;
		this.executers = executers;
		
		run();
	}

	public void run()
	{
		dialog = new JDialog(workbench); 
		dialog.setTitle("Stop execution");
		dialog.setSize(new Dimension(250, 120));
		// dialog.setLocation(200,100);
		dialog.setResizable(false);
		Container contentPane = dialog.getContentPane();

		// JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		// JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		// JPanel messagePanel;

		// standardPanel.setLayout(new GridLayout(1,2));
		// standardPanel.add(leftPanel);
		// standardPanel.add(rightPanel);
		
		// advancedPanel
		// null...

		JPanel messagePanel = new JPanel(new GridLayout(2,1));
		JPanel headerPanel = new JPanel();
		counterPanel = new JPanel();
		messagePanel.add(headerPanel);
		messagePanel.add(counterPanel);
		JPanel buttonPanel = new JPanel();
		stopButton = addButton(buttonPanel, "Stop execution");

		headerLabel = new JLabel();
		// headerLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		// headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		counterLabel = new JLabel();
		// counterLabel.setVerticalAlignment(SwingConstants.TOP);
		// counterLabel.setHorizontalAlignment(SwingConstants.CENTER);
		headerPanel.add(headerLabel, "South");
		counterPanel.add(counterLabel, "North");
		
	    contentPane.add(messagePanel, "Center");
		contentPane.add(buttonPanel, "South");		

		show();
	}

	public void makeCounter()
	{
		progressBar = null;
		counterLabel = new JLabel();
		counterPanel.removeAll();
		counterPanel.add(counterLabel);
	}

	public void makeProgressBar(int min, int max)
	{
		counterLabel = null;
		progressBar = new JProgressBar(min, max);
		counterPanel.removeAll();
		counterPanel.add(progressBar);
	}

	public void updateCounter(int value)
	{
		if (progressBar == null)
		{
			try
			{
				counterLabel.setText(String.valueOf(value));
			}
			catch (Exception e)
			{
				System.out.println("Error when updating counter.");
			}
		}
		else if (counterLabel == null)
		{
			try
			{
				progressBar.setValue(value);
			}
			catch (Exception e)
			{
				System.out.println("Error when updating progress bar.");
			}			
		}
	}

	public void updateHeader(String message)
	{
		try
		{
			headerLabel.setText(message);
		}
		catch (Exception e)
		{
			System.out.println("Error when updating header.");
		}
	}

   	JButton addButton(Container container, String name)
	{
	 	JButton button = new JButton(name);
		button.addActionListener(this);
		container.add(button);
		return button;
	}

	public void show()
	{
		dialog.show();
	}

	public void destroy()
	{
		dialog.setVisible(false);
	}
    
	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();
		if (source == stopButton)
		{
			for (int i = 0; i < executers.size(); i++)
			    ((Stoppable) executers.get(i)).requestStop();
			dialog.setVisible(false);
		}
		else
 		{
			System.out.println("What the hell was that? That was kinda' gross.");
		}
	}
}
