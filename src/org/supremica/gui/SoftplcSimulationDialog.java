package org.supremica.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

import org.supremica.properties.Config;

public class SoftplcSimulationDialog
	extends JDialog
{
	private static final long serialVersionUID = 1L;
	private boolean ok = false;
	private final JComboBox<Object> interfaces;
	private final Vector<Object> interfacesVector = new Vector<Object>();

	public SoftplcSimulationDialog(final Frame frame, final String title, final boolean modal)
	{
		super(frame, title, modal);

		final JPanel panel1 = new JPanel();
		final GridBagLayout gridBagLayout1 = new GridBagLayout();
		final JLabel cycleTimeLabel = new JLabel("Cycle time (ms)");
		final JLabel runSimulationLabel = new JLabel("Run simulation...");
		final JTextField cycleTime = new JTextField();
		final JLabel interfaceLabel = new JLabel("I/O interface");
		final JButton cancelButton = new JButton("Cancel");
		final JButton simulateButton = new JButton("Simulate");
		final JLabel tempLabel = new JLabel(" ");

		interfacesVector.add(Config.SOFTPLC_INTERFACES.get()); // TO DO - Divide string into multiple entries - they are separated by colon
		interfaces = new JComboBox<Object>(interfacesVector);

		try
		{
			panel1.setLayout(gridBagLayout1);
			runSimulationLabel.setFont(new java.awt.Font("Dialog", 1, 18));
			cancelButton.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(final ActionEvent e)
				{
					cancelButton_actionPerformed(e);
				}
			});
			simulateButton.addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(final ActionEvent e)
				{
					simulateButton_actionPerformed(e);
				}
			});
			interfaces.setBackground(Color.white);
			panel1.setBackground(Color.white);
			getContentPane().add(panel1);
			panel1.add(cycleTimeLabel, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(7, 0, 0, 0), 0, 0));
			panel1.add(cycleTime, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 67, 0));
			panel1.add(interfaces, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			panel1.add(interfaceLabel, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(12, 0, 4, 0), 0, 0));
			panel1.add(runSimulationLabel, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(7, 2, 4, 0), 77, 12));
			panel1.add(cancelButton, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(17, 0, 25, 0), 0, 0));
			panel1.add(simulateButton, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(19, 0, 28, 0), 0, 0));
			panel1.add(tempLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 29, 0));
			pack();
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public boolean showDialog()
	{
		this.setVisible(true);

		return ok;
	}

	public org.supremica.gui.SoftplcInterface getIOInterface()
	{
		return (org.supremica.gui.SoftplcInterface) interfacesVector.get(interfaces.getSelectedIndex());
	}

	void cancelButton_actionPerformed(final ActionEvent e)
	{
		this.setVisible(false);

		ok = false;
	}

	void simulateButton_actionPerformed(final ActionEvent e)
	{
		this.setVisible(false);

		ok = true;
	}
}
