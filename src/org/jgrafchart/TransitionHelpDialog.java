package org.jgrafchart;



import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.nwoods.jgo.*;


public class TransitionHelpDialog
	extends JDialog
{

	JPanel panel1 = new JPanel();
	JTextArea nameField = new JTextArea();
	JButton OKButton = new JButton();

	public TransitionHelpDialog(Frame frame, GCView view)
	{

		super(frame, "Transition Help", true);

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

	public TransitionHelpDialog()
	{
		super((Frame) null, "Transition Help", true);
	}

	private final void init()
	{

		panel1.setLayout(null);
		panel1.setMinimumSize(new Dimension(450, 550));
		panel1.setPreferredSize(new Dimension(450, 550));
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
		OKButton.setBounds(new Rectangle(180, 500, 79, 22));
		panel1.add(nameField);
		nameField.setBounds(new Rectangle(50, 40, 350, 450));
		nameField.setEnabled(true);
		nameField.setOpaque(true);
		nameField.setText("\n Boolean condition or event expression\n\n Operators:\n  !  - NOT\n  &  - AND\n  |  - OR\n  == - Equal\n  != - NOT Equal\n  <  - Less than\n  > - Greater than\n  <= - Less or equal\n  >= - Greater or equal\n\n  <Step>.x - true if <Step> is active\n  <Step>.t - the number of ticks that <Step> has been\n    active since it was last activated\n\n Parentheses may be used\n\n Event expressions:\n  /<booleanvariable-or-input> - positive trigger event\n \\<booleanvariable-or-input> - negative trigger event\n\n 0 and 1 are used both as boolean and integer literals\n (context-dependent semantics)");
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
