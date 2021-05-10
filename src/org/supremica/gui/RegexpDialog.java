//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import org.supremica.automata.State;
import org.supremica.automata.Automaton;

//
public class RegexpDialog
	extends JDialog    /* implements ActionListener */
{
	private static final long serialVersionUID = 1L;
	private JTextField reg_exp;
	private boolean ok = false;

	private void setOk()
	{
		ok = true;
	}

	private void doRepaint()
	{
		repaint();
	}

	private void replaceSelection(String s)
	{
		reg_exp.replaceSelection(s);
	}

	private JButton setDefaultButton(JButton b)
	{
		getRootPane().setDefaultButton(b);

		return b;
	}

	class RegexpMenuItem
		extends JMenuItem
		implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		String pattern;

		public RegexpMenuItem(String s, String p)
		{
			super(s + " - " + p);

			pattern = p;

			addActionListener(this);
		}

		public void actionPerformed(ActionEvent event)
		{
			replaceSelection(pattern);
			doRepaint();
		}
	}

	class StatesMenuItem
		extends JMenuItem
		implements ActionListener
	{
		private static final long serialVersionUID = 1L;

		public StatesMenuItem(String s)
		{
			super(s);

			addActionListener(this);
		}

		public void actionPerformed(ActionEvent event)
		{
			String text = ((JMenuItem) event.getSource()).getText();

			replaceSelection(text);
			doRepaint();
		}
	}

	class RegexpMenuBar
		extends JMenuBar
	{
		private static final long serialVersionUID = 1L;

		private JMenu makeExpressionMenu()
		{
			JMenu menu = new JMenu("Expressions");

			menu.add(new RegexpMenuItem("any string", ".*"));
			menu.add(new RegexpMenuItem("any uppercase", "[A-Z]"));
			menu.add(new RegexpMenuItem("any lowercase", "[a-z]"));
			menu.add(new RegexpMenuItem("any alphabetic", "[a-zA-Z]"));
			menu.add(new RegexpMenuItem("any digit", "[0-9]"));

			return menu;
		}

		private JMenu makeStatesMenu(Automaton a)
		{
			JMenu menu = new JMenu("States");

			for (Iterator<State> it = a.stateIterator(); it.hasNext(); )
			{
				menu.add(new StatesMenuItem(((State) it.next()).getName()));
			}

			return menu;
		}

		@SuppressWarnings("unused")
		private JMenu makeHelpMenu()
		{
			JMenu help = new JMenu("Help");

			help.add(new JMenuItem("Help Topics"));
			help.add(new JSeparator());
			help.add(new JMenuItem("About..."));

			return help;
		}

		public RegexpMenuBar()
		{
			this.add(makeExpressionMenu());

			//this.add(makeHelpMenu());
		}

		public RegexpMenuBar(Automaton automaton)
		{
			this.add(makeExpressionMenu());
			this.add(makeStatesMenu(automaton));

			//this.add(makeHelpMenu());
		}
	}

	class OkButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public OkButton()
		{
			super("OK");

			setToolTipText("Exit, add regexp");
			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					action(e);
				}
			});
		}

		void action(ActionEvent e)
		{
			setOk();
			dispose();
		}
	}

	class CancelButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		public CancelButton()
		{
			super("Cancel");

			setToolTipText("Exit, do not add regexp");
			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					action(e);
				}
			});
		}

		void action(ActionEvent e)
		{
			dispose();
		}
	}

	private void setup(String txt)
	{
		JPanel p1 = new JPanel();

		p1.add(new JLabel("Regexp:"));
		p1.add(reg_exp = new JTextField(txt, 30));

		JPanel p2 = new JPanel();

		p2.add(setDefaultButton(new OkButton()));
		p2.add(new CancelButton());

		Container content_pane = getContentPane();

		content_pane.add(BorderLayout.CENTER, p1);
		content_pane.add(BorderLayout.SOUTH, p2);
	}

	private void goAhead(String txt)
	{
		pack();

		Dimension dim = getMinimumSize();

		setLocation(Utility.getPosForCenter(dim));
		setResizable(false);
		reg_exp.selectAll();    // set the whole string as selected
		setVisible(true);

		if (ok)
		{
			txt = reg_exp.getText();
		}
	}

	// Pop up this dialog with the textfield set to txt
	public RegexpDialog(JFrame parent, String txt)
	{
		super(parent, "Enter regular expression", true);    // modal

		setup(txt);
		setJMenuBar(new RegexpMenuBar());
		goAhead(txt);
	}

	public RegexpDialog(JFrame parent, Automaton a, String txt)
	{
		super(parent, "Enter regular expression for " + a.getName(), true);    // modal

		setup(txt);
		setJMenuBar(new RegexpMenuBar(a));
		goAhead(txt);
	}

	public boolean isOk()
	{
		return ok;
	}

	public String getText()
	{
		return reg_exp.getText();
	}
}
