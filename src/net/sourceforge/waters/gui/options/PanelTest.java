//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.options;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;



public class PanelTest {

	static int currentAlgHome = 0;		//current active algorithm panel

	public static void main(final String[] args) {

		final GridBagConstraints constraints = new GridBagConstraints();
		final JFrame frame = new JFrame("Test");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;
		constraints.insets = new Insets(2, 4, 2, 4);
		final JPanel mMainPanel = new JPanel();
		final JPanel mButtonPanel = new JPanel();
		final JPanel mAlgOneHomePanel = new JPanel();
		final JPanel mAlgTwoHomePanel = new JPanel();
		final JPanel mAlgThreeHomePanel = new JPanel();
		final JPanel cards = new JPanel(new CardLayout());
		final CardLayout cl = (CardLayout)(cards.getLayout());
		final HashMap<Integer, String> algPosName = new HashMap<Integer, String>();
		final HashMap<Integer, List<ParameterPanel>> parameterPanelList = new HashMap<Integer, List<ParameterPanel>>();
		final String alg1Name = "ALG1";
		final String alg2Name = "ALG2";
		final String alg3Name = "ALG3";

		final GridBagLayout mainlayout = new GridBagLayout();

		final JLabel mNamePrefixLabel = new JLabel("Name Prefix:");
		final JTextField mNamePrefix = new JTextField();

		final JRadioButton mAlgOneRadio = new JRadioButton("AlgOne", true);
		final JRadioButton mAlgTwoRadio = new JRadioButton("AlgTwo");
		final JRadioButton mAlgThreeRadio = new JRadioButton("AlgThree");

	    final ButtonGroup superviserType = new ButtonGroup();
	    superviserType.add(mAlgOneRadio);
	    superviserType.add(mAlgTwoRadio);
	    superviserType.add(mAlgThreeRadio);

		// mNameLabel
		constrainComponent(mNamePrefixLabel, constraints, mainlayout, 0, 0, 0);
	    mMainPanel.add(mNamePrefixLabel);
	    // mNameInput
	    mNamePrefix.setColumns(20);
	    constraints.gridwidth = 2;
	    constrainComponent(mNamePrefix, constraints, mainlayout, 2, 0, 3);
	    mMainPanel.add(mNamePrefix);

	    constrainComponent(mAlgOneRadio, constraints, mainlayout, 0, 1, 3);
	    mMainPanel.add(mAlgOneRadio);

	    constrainComponent(mAlgTwoRadio, constraints, mainlayout, 2, 1, 3);
	    mMainPanel.add(mAlgTwoRadio);

	    constrainComponent(mAlgThreeRadio, constraints, mainlayout, 4, 1, 3);
	    mMainPanel.add(mAlgThreeRadio);

	    final JButton updateButton = new JButton("Update Parameters");
	    final JButton printButton = new JButton("Print Algorithm Panel");

	    mButtonPanel.add(updateButton);
	    mButtonPanel.add(printButton);

	    final List<Parameter> alg1 = new ArrayList<Parameter>();

	    final IntParameter max1 = new IntParameter(0, "Max", "Max value", 0, 100);
	    final BoolParameter enable1 = new BoolParameter(1, "Enable", "Turn on or off", true);
	    final BoolParameter enable2 = new BoolParameter(8, "Enable", "Turn on or off", true);
	    alg1.add(max1);
	    alg1.add(enable1);
	    alg1.add(enable2);

	    final List<ParameterPanel> alg1Panels = new ArrayList<ParameterPanel>();

	    for(int i = 0; i < alg1.size(); i++) {
	    	alg1Panels.add(new ParameterPanel(alg1.get(i), i));
	    }

	    final List<Parameter> alg2 = new ArrayList<Parameter>();
	    final IntParameter max2 = new IntParameter(0, "Max", "Max value", 0, 100);
	    final IntParameter min1 = new IntParameter(2, "Min", "Min value", 0, 100);
	    final BoolParameter enable3 = new BoolParameter(3, "Controllable", "Check if automata is controllable", true);
	    final BoolParameter enable4 = new BoolParameter(4, "Non-blocking", "Check if automata is non-blocking", true);
	    alg2.add(max2);
	    alg2.add(min1);
	    alg2.add(enable3);
	    alg2.add(enable4);
	    final List<ParameterPanel> alg2Panels = new ArrayList<ParameterPanel>();

	    for(int i = 0; i < alg2.size(); i++) {
	    	alg2Panels.add(new ParameterPanel(alg2.get(i), i));
	    }

	    final List<Parameter> alg3 = new ArrayList<Parameter>();
	    final IntParameter max10 = new IntParameter(0, "Max", "Max value", 0, 100);
	    final IntParameter min5 = new IntParameter(2, "Min", "Min value", 0, 100);
	    final BoolParameter enable8 = new BoolParameter(3, "Controllable", "Check if automata is controllable", true);
	    final BoolParameter enable7 = new BoolParameter(4, "Non-blocking", "Check if automata is non-blocking", true);
	    final BoolParameter enable00 = new BoolParameter(8, "Blocking", "Blocking", true);
	    alg3.add(max10);
	    alg3.add(min5);
	    alg3.add(enable8);
	    alg3.add(enable7);
	    alg3.add(enable00);
	    final List<ParameterPanel> alg3Panels = new ArrayList<ParameterPanel>();

	    for(int i = 0; i < alg3.size(); i++) {
	    	alg3Panels.add(new ParameterPanel(alg3.get(i), i));
	    }


	    final ActionListener updateParameters = new ActionListener() {
	        @Override
	        public void actionPerformed(final ActionEvent event)
	        {
	        	for(final ParameterPanel panel : parameterPanelList.get(currentAlgHome)) {
	        		/*
	        		Parameter tmp = panel.getParameter();
	        		if(tmp.getClass().equals(IntParameter.class))
	        			System.out.print("Before: " + ((IntParameter) tmp).getValue() + " ");
	        		else if(tmp.getClass().equals(BoolParameter.class))
	        			System.out.print("Before: " + ((BoolParameter) tmp).getValue() + " ");
	        		*/
	        		panel.commitParameter();
	        		/*
	        		if(tmp.getClass().equals(IntParameter.class))
	        			System.out.print("After: " + ((IntParameter) tmp).getValue() + " ");
	        		else if(tmp.getClass().equals(BoolParameter.class))
	        			System.out.print("After: " + ((BoolParameter) tmp).getValue() + " ");
	        		*/
	        	//	System.out.println();
	    	    }
	        }
	      };

	    final ActionListener printPanel = new ActionListener() {
		        @Override
		        public void actionPerformed(final ActionEvent event)
		        {
		    	    //cl.next(cards);
		    	    for(final ParameterPanel panel : parameterPanelList.get(currentAlgHome)) {

		    	        final Parameter p = panel.getParameter();
		    	    	if(p.getClass().equals(IntParameter.class))
		    	    		 System.out.print(((IntParameter)p).getValue() + " ");		//default parameter doesnt have getValue
		    	    	if(p.getClass().equals(BoolParameter.class))
		    	    		 System.out.print(((BoolParameter)p).getValue() + " ");
		    	    }
		    	    System.out.println();
		        }
		 };

		 final ActionListener changePanel = new ActionListener() {
		        @Override
		        public void actionPerformed(final ActionEvent event)
		        {
		        	for(final ParameterPanel panel : parameterPanelList.get(currentAlgHome))
		    	    	panel.commitParameter();

		    	    if(mAlgOneRadio.isSelected()) {
		    	    	copyValue(parameterPanelList.get(currentAlgHome), parameterPanelList.get(0));
		    	    	currentAlgHome = 0;
		    	    }
		    	    else if(mAlgTwoRadio.isSelected()) {
		    	    	copyValue(parameterPanelList.get(currentAlgHome), parameterPanelList.get(1));
		    	    	currentAlgHome = 1;
		    	    }
		    	    else if(mAlgThreeRadio.isSelected()) {
		    	    	copyValue(parameterPanelList.get(currentAlgHome), parameterPanelList.get(2));
		    	    	currentAlgHome = 2;
		    	    }

		    	    System.out.println("Current Alg: " + currentAlgHome + " it's name: " + algPosName.get(currentAlgHome));
		    	    cl.show(cards, algPosName.get(currentAlgHome));		    //if algs stored in drop down use index
		        }
		 };

		mAlgOneRadio.addActionListener(changePanel);
		mAlgTwoRadio.addActionListener(changePanel);
		mAlgThreeRadio.addActionListener(changePanel);

	    updateButton.addActionListener(updateParameters);
	    printButton.addActionListener(printPanel);

		// Finally, build the full dialog ...
	    final GridBagLayout layout = new GridBagLayout();
	    frame.setLayout(layout);
	    constraints.gridx = 0;
	    constraints.gridy = GridBagConstraints.RELATIVE;
	    constraints.gridwidth = GridBagConstraints.REMAINDER;
	    constraints.weightx = 1.0;
	    constraints.weighty = 1.0;
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.insets = new Insets(0, 0, 0, 0);
	    layout.setConstraints(mMainPanel, constraints);
	    frame.add(mMainPanel);

	    //All panels occur vertically, one column many rows
	    mAlgOneHomePanel.setLayout(new BoxLayout(mAlgOneHomePanel, BoxLayout.Y_AXIS));

	    for (final ParameterPanel panel : alg1Panels) {
	    	mAlgOneHomePanel.add(panel);
	    }

	    mAlgTwoHomePanel.setLayout(new BoxLayout(mAlgTwoHomePanel, BoxLayout.Y_AXIS));
	    for (final ParameterPanel panel : alg2Panels) {
		   mAlgTwoHomePanel.add(panel);
		}

	    mAlgThreeHomePanel.setLayout(new BoxLayout(mAlgThreeHomePanel, BoxLayout.Y_AXIS));
	    for (final ParameterPanel panel : alg3Panels) {
		   mAlgThreeHomePanel.add(panel);
		}
	 	//Using cardlayout
	    layout.setConstraints(cards, constraints);
	    cards.add(mAlgOneHomePanel, alg1Name);
	    cards.add(mAlgTwoHomePanel, alg2Name);
	    cards.add(mAlgThreeHomePanel, alg3Name);

	    //hashmap for retrieving active card
	    algPosName.put(0, alg1Name);
	    parameterPanelList.put(0, alg1Panels);

	    algPosName.put(1, alg2Name);
	    parameterPanelList.put(1, alg2Panels);

	    algPosName.put(2, alg3Name);
	    parameterPanelList.put(2, alg3Panels);

	    frame.add(cards);
	    layout.setConstraints(mButtonPanel, constraints);
	    frame.add(mButtonPanel);
	    frame.pack();
	    frame.setVisible(true);
	}

	public static void constrainComponent(final Component comp, final GridBagConstraints constraints, final GridBagLayout mainlayout, final int x, final int y, final int wx) {
		constraints.gridx = x;
	    constraints.gridy = y;
	    constraints.weightx = wx;
	    mainlayout.setConstraints(comp, constraints);
	}

	public static void copyValue(final List<ParameterPanel> oldPanel, final List<ParameterPanel> newPanel) {

		final List<Integer> oldPanelIDs = new ArrayList<Integer>();
		final List<Integer> newPanelIDs = new ArrayList<Integer>();

		 for (final ParameterPanel panel : oldPanel) {
			 	oldPanelIDs.add(panel.getParameter().getID());
		    }

		 for (final ParameterPanel panel : newPanel) {
			 newPanelIDs.add(panel.getParameter().getID());
		    }

		 //intersection
		 oldPanelIDs.retainAll(newPanelIDs);

		 //copy value from current panel to new one
		 for(final Integer i : oldPanelIDs) {

			 final ParameterPanel o = findID(oldPanel, i);
			 final ParameterPanel n = findID(newPanel, i);

			 o.commitParameter();
			 n.copyFromPanel(o);

			 /*
			  * This is what we want:
			 Parameter oldParam = o.getParameter();
			 Parameter newParam = n.getParameter();
			 oldParam.updateFromGUI(o);
			 newParam.copyFrom(oldParam);
			 newParam.displayInGUI(n);
			 */

		 }
	}

	public static ParameterPanel findID(final List<ParameterPanel> Panels, final int id) {

		 for (final ParameterPanel p : Panels) {
			 if(p.getParameter().getID() == id)
				 return p;
		    }

		return null;
	}
}
