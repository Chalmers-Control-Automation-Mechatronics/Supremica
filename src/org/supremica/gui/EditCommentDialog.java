
/********************** EditCommentDialog.java ************************/
package org.supremica.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class EditCommentDialog
	extends JDialog
{
	private static final long serialVersionUID = 1L;
	String newComment = null;
	JEditorPane text;

	public EditCommentDialog(JFrame frame, String oldComment)
	{
		super(frame, "Edit comment", true);

		setSize(new Dimension(350, 250));

		//setResizable(false);
		// Design labels and buttons
		JPanel labelPane = new JPanel();
		JLabel label;

		if (oldComment.equals(""))
		{
			label = new JLabel("Enter new project comment");
		}
		else
		{
			label = new JLabel("Edit the project comment");
		}

		labelPane.add(label);

		text = new JEditorPane("text/plain", oldComment);

		JScrollPane textPane = new JScrollPane(text);
		JPanel buttonPane = new JPanel();
		JButton okButton = new JButton("OK");

		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				newComment = text.getText();

				dispose();
			}
		});

		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		buttonPane.add(okButton);
		buttonPane.add(cancelButton);

		// Add the stuff to the dialog
		Container pane = getContentPane();

		pane.setLayout(new BorderLayout(10, 10));
		pane.add(labelPane, BorderLayout.NORTH);
		pane.add(textPane, BorderLayout.CENTER);
		pane.add(buttonPane, BorderLayout.SOUTH);

		// Center over the Supremica window
		Point point = Utility.getPosForCenter(getSize());

		setLocation(point);
		show();
	}

	public String getComment()
	{
		return newComment;
	}
}
