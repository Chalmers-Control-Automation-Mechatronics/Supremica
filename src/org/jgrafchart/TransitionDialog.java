package org.jgrafchart;



import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.nwoods.jgo.*;


public class TransitionDialog
	extends JDialog
{

	JPanel panel1 = new JPanel();
	JLabel label = new JLabel();
	JTextField nameField = new JTextField();
	JButton OKButton = new JButton();
	JButton CancelButton = new JButton();
	JButton HelpButton = new JButton();
	public GCDocument myObject;
	public GCTransition t;
	public GCView v;

	public TransitionDialog(Frame frame, GCDocument obj, GCTransition tin, GCView view)
	{

		super(frame, "Transition", true);

		try
		{
			myObject = obj;
			t = tin;
			v = view;

			init();
			pack();
			setLocationRelativeTo(view);
			updateDialog();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public TransitionDialog()
	{
		super((Frame) null, "Transition", true);
	}

	private final void init()
	{

		panel1.setLayout(null);
		panel1.setMinimumSize(new Dimension(294, 141));
		panel1.setPreferredSize(new Dimension(294, 141));
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
		HelpButton.addActionListener(new java.awt.event.ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				onHelp();
			}
		});
		getContentPane().add(panel1);
		OKButton.setText("OK");
		panel1.add(OKButton);
		OKButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		OKButton.setBounds(new Rectangle(30, 104, 79, 22));
		CancelButton.setText("Cancel");
		panel1.add(CancelButton);
		CancelButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		CancelButton.setBounds(new Rectangle(120, 104, 79, 22));
		HelpButton.setText("Help");
		panel1.add(HelpButton);
		HelpButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		HelpButton.setBounds(new Rectangle(210, 104, 79, 22));
		label.setText("Transition Condition:");
		label.setHorizontalAlignment(JLabel.LEFT);
		panel1.add(label);
		label.setBounds(new Rectangle(50, 10, 148, 24));
		panel1.add(nameField);
		nameField.setBounds(new Rectangle(50, 40, 200, 24));
		nameField.setEnabled(myObject.isModifiable());
	}

	void updateDialog()
	{

		if (myObject == null)
		{
			return;
		}

		nameField.setText(t.myLabel.getText());
		nameField.setFont(new Font("Dialog", Font.BOLD, 14));
	}

	void updateData()
	{

		if (myObject == null)
		{
			return;
		}

		t.myLabel.setText(nameField.getText());
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
			this.dispose();		// Free system resources
		}
		catch (Exception e) {}
	}

	void OnCancel()
	{

		try
		{
			this.dispose();		// Free system resources
		}
		catch (Exception e) {}
	}

	void onHelp()
	{
		new TransitionHelpDialog(v.getFrame(), v).setVisible(true);
	}
}
