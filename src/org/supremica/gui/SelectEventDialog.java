
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.log.*;


public class SelectEventDialog
	extends JDialog
{
	private org.supremica.automata.LabeledEvent selectedEvent = null;
	private JPanel contentPane = null;
	private Alphabet theAlphabet = null;
	private Automaton theAutomaton = null;

	public SelectEventDialog(Frame owner, Automaton theAutomaton)
		throws Exception
	{
		super(owner, "Select event", true);

		this.theAutomaton = theAutomaton;
		theAlphabet = theAutomaton.getAlphabet();
		contentPane = (JPanel) getContentPane();

		contentPane.setLayout(new BorderLayout());
		setSize(400, 300);

		JLabel introText = new JLabel("Select an event. If necessary, create the event first");

		contentPane.add(introText, BorderLayout.NORTH);

		Box horizBox1 = Box.createHorizontalBox();
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		horizBox1.add(Box.createGlue());
		horizBox1.add(okButton);
		horizBox1.add(Box.createGlue());
		horizBox1.add(cancelButton);
		horizBox1.add(Box.createGlue());
		contentPane.add(horizBox1, BorderLayout.SOUTH);

		Box horizBox2 = Box.createHorizontalBox();
		AlphabetPanel alphabetPanel = new AlphabetPanel(theAlphabet);
		CreateEventPanel createEventPanel = new CreateEventPanel(theAlphabet);

		horizBox2.add(alphabetPanel);
		horizBox2.add(createEventPanel);
		contentPane.add(horizBox2, BorderLayout.CENTER);
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				selectedEvent = null;

				setVisible(false);
			}
		});

		// Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();

		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}

		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}

		setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
	}

	public org.supremica.automata.LabeledEvent getEvent()
	{
		selectedEvent = null;

		setVisible(true);

		return selectedEvent;
	}
}

class AlphabetPanel
	extends JPanel
{
	private Alphabet theAlphabet;

	// private JList theEventList = new JList();
	private JLabel test1 = new JLabel("test1");

	public AlphabetPanel(Alphabet theAlphabet)
	{
		this.theAlphabet = theAlphabet;

		add(test1, BorderLayout.CENTER);

		Border border = BorderFactory.createTitledBorder("Select event");

		setBorder(border);

		// add(theEventList, BorderLayout.CENTER);
	}
}

class CreateEventPanel
	extends JPanel
{
	private static Logger logger = LoggerFactory.createLogger(CreateEventPanel.class);

	private Alphabet theAlphabet;
	private JTextField labelField;
	private JCheckBox controllableCheckBox;
	private JCheckBox prioritizedCheckBox;
	private JButton createEventButton;

	public CreateEventPanel(Alphabet theAlphabet)
		throws Exception
	{
		this.theAlphabet = theAlphabet;

		// setLayout(Box.createHorizontalBox());
		Border border = BorderFactory.createTitledBorder("Create event");

		setBorder(border);

		Box vertBox = Box.createVerticalBox();

		add(vertBox);
		vertBox.add(new JLabel("Label"));

		labelField = new JTextField();

		vertBox.add(labelField);
		vertBox.add(Box.createVerticalStrut(5));

		controllableCheckBox = new JCheckBox("controllable", true);

		vertBox.add(controllableCheckBox);

		prioritizedCheckBox = new JCheckBox("prioritized", true);

		vertBox.add(prioritizedCheckBox);
		add(Box.createGlue());

		createEventButton = new JButton("Create event");

		vertBox.add(createEventButton);
		createEventButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Alphabet alph = getAlphabet();
				String currLabel = labelField.getText();

				if (alph.containsEventWithLabel(currLabel))
				{
					JOptionPane.showMessageDialog(null, "Existing event", "An event " + currLabel + "does already exists", JOptionPane.ERROR_MESSAGE);
				}
				else //it does not contain an event with this label, just add it
				{
					LabeledEvent newEvent = new LabeledEvent(currLabel);

					// newEvent.setId(alph.getUniqueId("e"));

					try
					{
						alph.addEvent(newEvent);
					}
					catch (Exception ex)
					{
						logger.error("Exception in Alphabet.addEvent", ex);
						logger.debug(ex.getStackTrace());
						return;
					}
				}
			}
		});
	}

	private Alphabet getAlphabet()
	{
		return theAlphabet;
	}

	public void setVisible(boolean visible)
	{
		labelField.setText("");
		controllableCheckBox.setSelected(true);
		prioritizedCheckBox.setSelected(true);
		super.setVisible(visible);
	}
}
