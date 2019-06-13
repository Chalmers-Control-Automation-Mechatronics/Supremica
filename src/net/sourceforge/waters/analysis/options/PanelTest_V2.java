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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.marshaller.MarshallingTools;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


public class PanelTest_V2
{

  final static ProductDESProxyFactory factory =   ProductDESElementFactory.getInstance();
  final static ProductDESProxy des = MarshallingTools.loadAndCompileModule("examples/includeInJarFile/ModuleExamples/big_factory/bfactory.wmod");
  static ParameterJScrollPane mScrollParametersPanel;
  static HashMap<ModelAnalyzerFactoryLoader,List<Parameter>> algorithmsParameters =
    new HashMap<ModelAnalyzerFactoryLoader,List<Parameter>>();

  public static void main(final String[] args)
    throws AnalysisConfigurationException
  {
    final JFrame frame = new JFrame("Test");
    final JPanel mSuperviserPanel = new JPanel();

    final JComboBox<ModelAnalyzerFactoryLoader> superviserCombobox =
      new JComboBox<>();
    final JLabel superviserComboboxLabel =
      new JLabel("Available Synthesis Supervisers");

    for (final ModelAnalyzerFactoryLoader dir : ModelAnalyzerFactoryLoader
      .values()) {
      try {
        if (dir.getModelAnalyzerFactory()
          .createSupervisorSynthesizer(ProductDESElementFactory
            .getInstance()) != null)
          ;
        {
          superviserCombobox.addItem(dir);
          algorithmsParameters
            .put(dir,
                 dir.getModelAnalyzerFactory()
                   .createSupervisorSynthesizer(ProductDESElementFactory
                     .getInstance())
                   .getParameters());
          //System.out.println(dir.toString());
        }
      } catch (ClassNotFoundException | UnsatisfiedLinkError
        | AnalysisConfigurationException exception) {
        // exception.printStackTrace(); }
      }
    }

    mSuperviserPanel.add(superviserComboboxLabel, BorderLayout.WEST);
    mSuperviserPanel.add(superviserCombobox, BorderLayout.EAST);

    final ActionListener syntheisSuperviserChanged = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {

        final ModelAnalyzerFactoryLoader tmp =
          (ModelAnalyzerFactoryLoader) superviserCombobox.getSelectedItem();

        mScrollParametersPanel.commit();
        mScrollParametersPanel.replaceView(algorithmsParameters.get(tmp), des);

        //re-packing causes the frame to shrink/increase to preferred size
         frame.pack();
      }
    };

    superviserCombobox.addActionListener(syntheisSuperviserChanged);

    // superviserCombobox should have at least one item
    final ModelAnalyzerFactoryLoader first = (ModelAnalyzerFactoryLoader) superviserCombobox.getSelectedItem();

    algorithmsParameters.get(first).add(new FileParameter(100000, "Open File", "Open a file "));
    algorithmsParameters.get(first).add(new EventParameter(1001000, "Events", "All events"));

    mScrollParametersPanel = new ParameterJScrollPane(algorithmsParameters.get(first),des);
    //Finally, build the full dialog ...
    frame.add(mSuperviserPanel, BorderLayout.PAGE_START);
    frame.add(mScrollParametersPanel, BorderLayout.CENTER);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
}