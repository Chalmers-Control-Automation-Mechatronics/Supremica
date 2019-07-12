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

package net.sourceforge.waters.analysis.options;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import net.sourceforge.waters.analysis.compositional.CompositionalAutomataSynthesizer;
import net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.marshaller.MarshallingTools;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


public class PanelTest
{

  static int currentAlgHome = 0; //current active algorithm panel
  final static ProductDESProxyFactory factory =
    ProductDESElementFactory.getInstance();
  final static ProductDESProxy des = MarshallingTools.loadAndCompileModule("examples/includeInJarFile/ModuleExamples/big_factory/bfactory.wmod");

  public static void main(final String[] args)
  {
    final GridBagConstraints constraints = new GridBagConstraints();
    final JFrame frame = new JFrame("Test");
    constraints.anchor = GridBagConstraints.WEST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(2, 4, 2, 4);
    final JPanel mMainPanel = new JPanel();
    final JPanel mButtonPanel = new JPanel();
    final JPanel mMonolithicHomePanel = new JPanel();
    final JPanel mCompositionalHomePanel = new JPanel();
    final JPanel cards = new JPanel(new CardLayout());
    final CardLayout cl = (CardLayout) (cards.getLayout());
    final HashMap<Integer,String> algPosName = new HashMap<Integer,String>();
    final HashMap<Integer,List<ParameterPanel>> parameterPanelList =
      new HashMap<Integer,List<ParameterPanel>>();
    final String alg1Name = "ALG1";
    final String alg2Name = "ALG2";


    final GridBagLayout mainlayout = new GridBagLayout();

    final JRadioButton mAlgOneRadio = new JRadioButton("Monolithic", true);
    final JRadioButton mAlgTwoRadio = new JRadioButton("Compositional");

    final ButtonGroup superviserType = new ButtonGroup();
    superviserType.add(mAlgOneRadio);
    superviserType.add(mAlgTwoRadio);

    constrainComponent(mAlgOneRadio, constraints, mainlayout, 0, 1, 3);
    mMainPanel.add(mAlgOneRadio);

    constrainComponent(mAlgTwoRadio, constraints, mainlayout, 2, 1, 3);
    mMainPanel.add(mAlgTwoRadio);

    /*
     * final JComboBox<ModelAnalyzerFactoryLoader> superviserCombobox = new
     * JComboBox<>();
     *
     * for (final ModelAnalyzerFactoryLoader dir :
     * ModelAnalyzerFactoryLoader.values()) { try {
     * if(dir.getModelAnalyzerFactory().createSupervisorSynthesizer(
     * ProductDESElementFactory.getInstance()) != null);
     * superviserCombobox.addItem(dir);
     *
     * } catch (ClassNotFoundException | AnalysisConfigurationException |
     * UnsatisfiedLinkError exception) { // exception.printStackTrace(); } }
     *
     * constrainComponent(superviserCombobox, constraints, mainlayout, 0, 2,
     * 3); mMainPanel.add(superviserCombobox);
     */

    final JButton updateButton = new JButton("Update Parameters");
    final JButton printButton = new JButton("Print Algorithm Panel");

    mButtonPanel.add(updateButton);
    mButtonPanel.add(printButton);



    // final List<Parameter> alg1 = fakeMonolithic();
    final List<Parameter> alg1 =     new MonolithicSynthesizer(factory).getParameters();

    final List<ParameterPanel> alg1Panels = new ArrayList<ParameterPanel>();

    for (int i = 0; i < alg1.size(); i++) {
      alg1Panels.add(new ParameterPanel(alg1.get(i),des));
    }

    //final List<Parameter> alg2 = fakeCompositionAutomataSynthesizer();
    final List<Parameter> alg2 = new CompositionalAutomataSynthesizer(factory).getParameters();

    final List<ParameterPanel> alg2Panels = new ArrayList<ParameterPanel>();

    for (int i = 0; i < alg2.size(); i++) {
      alg2Panels.add(new ParameterPanel(alg2.get(i),des));
    }

    final ActionListener updateParameters = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        for (final ParameterPanel panel : parameterPanelList
          .get(currentAlgHome)) {
          panel.commitParameter();
        }
      }
    };

    final ActionListener printPanel = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        //cl.next(cards);
        for (final ParameterPanel panel : parameterPanelList
          .get(currentAlgHome)) {

          final Parameter p = panel.getParameter();
          if (p.getClass().equals(IntParameter.class))
            System.out.print("Int: " + ((IntParameter) p).getValue() + " "); //default parameter doesn't have getValue
          if (p.getClass().equals(BoolParameter.class))
            System.out
              .print("Boolean: " + ((BoolParameter) p).getValue() + " ");
          if (p.getClass().equals(StringParameter.class))
            System.out
              .print("String: " + ((StringParameter) p).getValue() + " ");
          if (p.getClass().equals(EnumParameter.class))
            System.out
              .print("Enum: " + ((EnumParameter<?>) p).getValue() + " ");
        }
        System.out.println();
      }
    };

    final ActionListener changePanel = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        for (final ParameterPanel panel : parameterPanelList
          .get(currentAlgHome))
          panel.commitParameter();

        if (mAlgOneRadio.isSelected()) {
          copyValue(parameterPanelList.get(currentAlgHome),
                    parameterPanelList.get(0));
          currentAlgHome = 0;
        } else if (mAlgTwoRadio.isSelected()) {
          copyValue(parameterPanelList.get(currentAlgHome),
                    parameterPanelList.get(1));
          currentAlgHome = 1;
        }

        //  System.out.println("Current Alg: " + currentAlgHome + " it's name: " + algPosName.get(currentAlgHome));
        cl.show(cards, algPosName.get(currentAlgHome)); //if algs stored in drop down use index
      }
    };

    mAlgOneRadio.addActionListener(changePanel);
    mAlgTwoRadio.addActionListener(changePanel);

    updateButton.addActionListener(updateParameters);
    printButton.addActionListener(printPanel);

    // Finally, build the full dialog ...
    final GridBagLayout layout = new GridBagLayout();
    //frame.setLayout(layout);
    constraints.gridx = 0;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(0, 0, 0, 0);
    layout.setConstraints(mMainPanel, constraints);
    frame.add(mMainPanel, BorderLayout.PAGE_START);

    //All panels occur vertically, one column many rows
    mMonolithicHomePanel
      .setLayout(new BoxLayout(mMonolithicHomePanel, BoxLayout.Y_AXIS));

    for (final ParameterPanel panel : alg1Panels) {
      mMonolithicHomePanel.add(panel);
    }

    mCompositionalHomePanel
      .setLayout(new BoxLayout(mCompositionalHomePanel, BoxLayout.Y_AXIS));

    for (final ParameterPanel panel : alg2Panels) {
      mCompositionalHomePanel.add(panel);
    }

    //Using cardlayout
    layout.setConstraints(cards, constraints);
    cards.setPreferredSize(new Dimension(cards.getWidth(), 300));

    final JScrollPane mono = new JScrollPane(mMonolithicHomePanel);
    cards.add(mono, alg1Name);
    final JScrollPane comp = new JScrollPane(mCompositionalHomePanel);
    cards.add(comp, alg2Name);

    //hashmap for retrieving active card
    algPosName.put(0, alg1Name);
    parameterPanelList.put(0, alg1Panels);

    algPosName.put(1, alg2Name);
    parameterPanelList.put(1, alg2Panels);

    frame.add(cards, BorderLayout.CENTER);
    layout.setConstraints(mButtonPanel, constraints);
    frame.add(mButtonPanel, BorderLayout.PAGE_END);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);

  }

  public static void constrainComponent(final Component comp,
                                        final GridBagConstraints constraints,
                                        final GridBagLayout mainlayout,
                                        final int x, final int y,
                                        final int wx)
  {
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.weightx = wx;
    mainlayout.setConstraints(comp, constraints);
  }

  public static void copyValue(final List<ParameterPanel> oldPanel,
                               final List<ParameterPanel> newPanel)
  {

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
    for (final Integer i : oldPanelIDs) {

      final ParameterPanel o = findID(oldPanel, i);
      final ParameterPanel n = findID(newPanel, i);

      o.commitParameter();
      n.copyFromPanel(o);
    }
  }

  public static ParameterPanel findID(final List<ParameterPanel> Panels,
                                      final int id)
  {
    for (final ParameterPanel p : Panels) {
      if (p.getParameter().getID() == id)
        return p;
    }

    return null;
  }
}
