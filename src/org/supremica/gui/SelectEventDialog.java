//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;


public class SelectEventDialog
	extends JDialog
{
	private static final long serialVersionUID = 1L;
	private org.supremica.automata.LabeledEvent selectedEvent = null;
	private JPanel contentPane = null;
	private Alphabet theAlphabet = null;
	@SuppressWarnings("unused")
	private Automaton theAutomaton = null;

	public SelectEventDialog(final Frame owner, final Automaton theAutomaton)
		throws Exception
	{
		super(owner, "Select event", true);

		this.theAutomaton = theAutomaton;
		theAlphabet = theAutomaton.getAlphabet();
		contentPane = (JPanel) getContentPane();

		contentPane.setLayout(new BorderLayout());
		setSize(400, 300);

		final JLabel introText = new JLabel("Select an event. If necessary, create the event first");

		contentPane.add(introText, BorderLayout.NORTH);

		final Box horizBox1 = Box.createHorizontalBox();
		final JButton okButton = new JButton("OK");
		final JButton cancelButton = new JButton("Cancel");

		horizBox1.add(Box.createGlue());
		horizBox1.add(okButton);
		horizBox1.add(Box.createGlue());
		horizBox1.add(cancelButton);
		horizBox1.add(Box.createGlue());
		contentPane.add(horizBox1, BorderLayout.SOUTH);

		final Box horizBox2 = Box.createHorizontalBox();
		final AlphabetPanel alphabetPanel = new AlphabetPanel(theAlphabet);
		final CreateEventPanel createEventPanel = new CreateEventPanel(theAlphabet);

		horizBox2.add(alphabetPanel);
		horizBox2.add(createEventPanel);
		contentPane.add(horizBox2, BorderLayout.CENTER);
		okButton.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				setVisible(false);
			}
		});
		cancelButton.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				selectedEvent = null;

				setVisible(false);
			}
		});

		// Center the window
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension frameSize = getSize();

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
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private final Alphabet theAlphabet;

	// private JList theEventList = new JList();
	private final JLabel test1 = new JLabel("test1");

	public AlphabetPanel(final Alphabet theAlphabet)
	{
		this.theAlphabet = theAlphabet;

		add(test1, BorderLayout.CENTER);

		final Border border = BorderFactory.createTitledBorder("Select event");

		setBorder(border);

		// add(theEventList, BorderLayout.CENTER);
	}
}

class CreateEventPanel
	extends JPanel
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(CreateEventPanel.class);
	private Alphabet theAlphabet;
	private JTextField labelField;
	private JCheckBox controllableCheckBox;
	private JCheckBox prioritizedCheckBox;
	private JButton createEventButton;

	public CreateEventPanel(final Alphabet theAlphabet)
		throws Exception
	{
		this.theAlphabet = theAlphabet;

		// setLayout(Box.createHorizontalBox());
		final Border border = BorderFactory.createTitledBorder("Create event");

		setBorder(border);

		final Box vertBox = Box.createVerticalBox();

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
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				final Alphabet alph = getAlphabet();
				final String currLabel = labelField.getText();

				if (alph.contains(currLabel))
				{
					JOptionPane.showMessageDialog(null, "Existing event", "An event " + currLabel + "does already exists", JOptionPane.ERROR_MESSAGE);
				}
				else    //it does not contain an event with this label, just add it
				{
					final LabeledEvent newEvent = new LabeledEvent(currLabel);

					// newEvent.setId(alph.getUniqueId("e"));
					try
					{
						alph.addEvent(newEvent);
					}
					catch (final Exception ex)
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

	@Override
  public void setVisible(final boolean visible)
	{
		labelField.setText("");
		controllableCheckBox.setSelected(true);
		prioritizedCheckBox.setSelected(true);
		super.setVisible(visible);
	}
}
