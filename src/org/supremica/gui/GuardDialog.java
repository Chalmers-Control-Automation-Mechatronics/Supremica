/*
 * GuardDialog.java
 *
 * Created on May 7, 2008, 6:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.automata.algorithms.Guard.*;


/**
 *
 * @author Sajed
 */
abstract class GuardPanel
	extends JPanel
{
	public abstract void update(GuardOptions s);

	public abstract void regain(GuardOptions s);
}

class GuardDialogStandardPanel
	extends GuardPanel
{
	private static final long serialVersionUID = 1L;
        private JRadioButton fromAllowedStatesButton;
        private JRadioButton fromForbiddenStatesButton;
	private JTextField eventField;

	public GuardDialogStandardPanel()
	{
		Box standardBox = Box.createVerticalBox();

		fromAllowedStatesButton = new JRadioButton("From allowed states");
		fromAllowedStatesButton.setToolTipText("Generate the expressions from the allowed states");
                
                fromForbiddenStatesButton = new JRadioButton("From forbidden states");
		fromForbiddenStatesButton.setToolTipText("Generate the expressions from the forbidden states");

		JLabel event = new JLabel("Event");
		eventField = new JTextField(15);
		eventField.setToolTipText("The name of the desired event");

                ButtonGroup group = new ButtonGroup();
                group.add(fromAllowedStatesButton);
                group.add(fromForbiddenStatesButton);

                JPanel expressionTypePanel = new JPanel();
                expressionTypePanel.add(fromAllowedStatesButton);
                expressionTypePanel.add(fromForbiddenStatesButton);
                
                standardBox.add(expressionTypePanel);
		standardBox.add(event);
                standardBox.add(eventField);
		this.add(standardBox);
	}

	public void update(GuardOptions guardOptions)
	{
                if(guardOptions.getExpressionType())
                {
                    fromAllowedStatesButton.setSelected(true);
                    fromForbiddenStatesButton.setSelected(false);
                }
                else
                {
                    fromAllowedStatesButton.setSelected(false);
                    fromForbiddenStatesButton.setSelected(true);
                }
                
                eventField.setText(guardOptions.getEvent());
	}

	public void regain(GuardOptions guardOptions)
	{
            if(fromAllowedStatesButton.isSelected())
            {
                guardOptions.setExpressionType(true);
            }
            if(fromForbiddenStatesButton.isSelected())
            {
                guardOptions.setExpressionType(false);
            }
            
            guardOptions.setEvent(eventField.getText());
	}
        
}

public class GuardDialog
	implements ActionListener
{
	private JButton okButton;
	private JButton cancelButton;
	GuardDialogStandardPanel standardPanel;
        private GuardOptions guardOptions;
	private JDialog dialog;
	private Frame parentFrame;

	/**
	 * Creates modal dialog box for input of synthesizer options.
	 */
	public GuardDialog(Frame parentFrame, GuardOptions guardOptions)
	{
		dialog = new JDialog(parentFrame, true);    // modal
		this.parentFrame = parentFrame;
                this.guardOptions = guardOptions;

		dialog.setTitle("Guard options");
		dialog.setSize(new Dimension(400, 200));

		Container contentPane = dialog.getContentPane();

		standardPanel = new GuardDialogStandardPanel();

		JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.addTab("Standard options", null, standardPanel, "Standard options");

		// buttonPanel
		JPanel buttonPanel = new JPanel();

		okButton = addButton(buttonPanel, "OK");
		cancelButton = addButton(buttonPanel, "Cancel");

		contentPane.add("Center", tabbedPane);
		contentPane.add("South", buttonPanel);
		Utility.setDefaultButton(dialog, okButton);

		// ** MF ** Fix to get the frigging thing centered
		Dimension dim = dialog.getMinimumSize();

		dialog.setLocation(Utility.getPosForCenter(dim));
		dialog.setResizable(false);
		update();
	}

	/**
	 * Updates the information in the dialog from what is recorded in synchronizationOptions.
	 * @see SynchronizationOptions
	 */
	public void update()
	{
		standardPanel.update(guardOptions);
	}

	private JButton addButton(Container container, String name)
	{
		JButton button = new JButton(name);

		button.addActionListener(this);
		container.add(button);

		return button;
	}

	public void show()
	{
		dialog.setVisible(true);
	}

	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();

		if (source == okButton)
		{
			standardPanel.regain(guardOptions);

			if (guardOptions.isValid())
			{
				dialog.setVisible(false);
				dialog.dispose();
			}
			else
			{
				JOptionPane.showMessageDialog(parentFrame, "Invalid combination", "Alert", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (source == cancelButton)
		{
			guardOptions.setDialogOK(false);    // Already done...
			dialog.setVisible(false);
			dialog.dispose();
		}
	}
}

