package org.jgrafchart;



import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.nwoods.jgo.*;


public class StepHelpDialog
	extends JDialog
{

	JPanel panel1 = new JPanel();
	JTextArea nameField = new JTextArea();
	JButton OKButton = new JButton();

	public StepHelpDialog(Frame frame, GCView view)
	{

		super(frame, "Action Help", true);

		try
		{
			init();
			pack();
			setLocationRelativeTo(view);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public StepHelpDialog()
	{
		super((Frame) null, "Action Help", true);
	}

	private final void init()
	{

		panel1.setLayout(null);
		panel1.setMinimumSize(new Dimension(4600, 540));
		panel1.setPreferredSize(new Dimension(450, 540));
		OKButton.addActionListener(new java.awt.event.ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				OnOK();
			}
		});
		getContentPane().add(panel1);
		OKButton.setText("OK");
		panel1.add(OKButton);
		OKButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		OKButton.setBounds(new Rectangle(180, 480, 79, 22));
		panel1.add(nameField);
		nameField.setBounds(new Rectangle(50, 40, 350, 410));
		nameField.setEnabled(true);
		nameField.setOpaque(true);
		nameField.setText("\n Stored (impulse) actions: \n S <variable-or-digitaloutput> = <expression>;\n Executed once when the step is activated\n ----------------------------------\n Normal (level) actions:\n N <digitaloutput>;\n The truth value of the output is\n bound to the activation status of the step\n ----------------------------------\n Periodic (always) actions:\n A <variable-or-digitaloutput> = <expression>;\n Executed periodically while the step is active\n ----------------------------------\n Exit (finally) actions:\n X <variable-or-digitaloutput> = <expression>;\n Excuted once before the step is deactivated\n ----------------------------------\n Expression syntax:\n - transition condition syntax +\n  + - plus\n  - - minus\n  * - multiplication\n  / - (integer) division\n");
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
			this.dispose();		// Free system resources
		}
		catch (Exception e) {}
	}
}
