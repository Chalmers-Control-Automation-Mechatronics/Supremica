package org.jgrafchart;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.nwoods.jgo.*;

public class GrafcetDialog
	extends JDialog
{
	JPanel panel1 = new JPanel();
	JButton OKButton = new JButton();
	JButton CancelButton = new JButton();
	JLabel label1 = new JLabel();
	JTextField nameField = new JTextField();
	JLabel label2 = new JLabel();
	JTextField rateField = new JTextField();
	JCheckBox simulation = new JCheckBox();
	JCheckBox dimming = new JCheckBox();
	JTextField dimRate = new JTextField();
	JLabel label3 = new JLabel();
	public GCDocument myObject;
	public Basic2GC myApp;

	public GrafcetDialog(Frame frame, GCDocument obj, Basic2GC app)
	{
		super(frame, "Workspace Properties", true);

		try
		{
			myObject = obj;
			myApp = app;

			init();
			pack();
			updateDialog();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public GrafcetDialog()
	{
		super((Frame) null, "Workspace Properties", true);
	}

	private final void init()
	{
		panel1.setLayout(null);
		panel1.setMinimumSize(new Dimension(294, 300));
		panel1.setPreferredSize(new Dimension(294, 300));
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
		OKButton.setBounds(new Rectangle(60, 270, 79, 22));
		CancelButton.setText("Cancel");
		panel1.add(CancelButton);
		CancelButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		CancelButton.setBounds(new Rectangle(168, 270, 79, 22));
		label1.setText("Workspace Name:");
		label1.setHorizontalAlignment(JLabel.LEFT);
		panel1.add(label1);
		label1.setBounds(new Rectangle(50, 10, 148, 24));
		panel1.add(nameField);
		nameField.setBounds(new Rectangle(50, 40, 200, 24));
		nameField.setEnabled(myObject.isModifiable());
		label2.setText("Thread Sleep Interval (ms):");
		label2.setHorizontalAlignment(JLabel.LEFT);
		panel1.add(label2);
		label2.setBounds(new Rectangle(50, 80, 200, 24));
		panel1.add(rateField);
		rateField.setBounds(new Rectangle(50, 110, 200, 24));
		rateField.setEnabled(true);
		simulation.setText("Simulator Mode");
		simulation.setBounds(new Rectangle(50, 150, 220, 14));
		panel1.add(simulation);
		dimming.setText("Token luminance");
		dimming.setBounds(new Rectangle(50, 180, 220, 14));
		panel1.add(dimming);
		label3.setText("Dim interval (#thread sleep ticks)");
		label3.setHorizontalAlignment(JLabel.LEFT);
		panel1.add(label3);
		label3.setBounds(new Rectangle(50, 210, 220, 24));
		panel1.add(dimRate);
		dimRate.setBounds(new Rectangle(50, 240, 200, 24));
		dimRate.setEnabled(true);
	}

	void updateDialog()
	{
		if (myObject == null)
		{
			return;
		}

		nameField.setText(myObject.getName());
		rateField.setText(new Integer(myObject.getSpeed()).toString());
		simulation.setSelected(myObject.simulation);
		dimming.setSelected(myObject.dimming);
		dimRate.setText(new Integer(myObject.dimTicks).toString());
	}

	void updateData()
	{
		if (myObject == null)
		{
			return;
		}

		myObject.setName(nameField.getText());

		// myObject.setSpeed(Integer.parseInt(rateField.getText()));
		myObject.simulation = (simulation.isSelected());

		myObject.propagateDimmingInfo(dimming.isSelected(), Integer.parseInt(dimRate.getText()), Integer.parseInt(rateField.getText()));

		// myObject.dimming = dimming.isSelected();
		// myObject.dimTicks = Integer.parseInt(dimRate.getText());
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
