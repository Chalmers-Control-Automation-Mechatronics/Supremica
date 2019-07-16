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

package net.sourceforge.waters.gui.analyzer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterJScrollPane;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.marshaller.MarshallingTools;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


public class PanelTest_V2
{
  static String bfactory = "big_factory/bfactory.wmod";
  static String car_fh = "car_fh/car_fh.wmod";

  final static ProductDESProxy des = MarshallingTools.loadAndCompileModule("examples/includeInJarFile/ModuleExamples/" + car_fh);

  static ParameterJScrollPane mScrollParametersPanel;
  static HashMap<Integer,Parameter> AllParams = new HashMap<Integer,Parameter>();
  static TestDESContext DESContext = new TestDESContext();

  public static void main(final String[] args)
  {
    final JFrame frame = new JFrame("Test");
    final JPanel mSuperviserPanel = new JPanel();
    final JComboBox<ModelAnalyzerFactoryLoader> superviserCombobox =
      new JComboBox<>();
    final JLabel superviserComboboxLabel = new JLabel("Algorithm");

    final JPanel mButtons = new JPanel(new GridLayout(0,2));

    //Buttons for testing synthesizer generation, storing/retrieval with database
    final JButton commit = new JButton("Store in Database");
    final JButton setValue = new JButton("Print Database");
    final JButton createSynth = new JButton("Create Synthesizer");

    final ActionListener setValuePrint = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        printMap();
      }
    };

    final ActionListener store = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        storeInDatabase();
      }
    };

    final ActionListener synthesizer = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        createSynthesizer((ModelAnalyzerFactoryLoader) superviserCombobox.getSelectedItem());
      }
    };

    setValue.addActionListener(setValuePrint);
    commit.addActionListener(store);
    createSynth.addActionListener(synthesizer);

    mButtons.add(commit);
    mButtons.add(setValue);
    mButtons.add(createSynth);

    for (final ModelAnalyzerFactoryLoader dir : ModelAnalyzerFactoryLoader.values()) {
      try {

        final SynchronousProductBuilder s = dir.getModelAnalyzerFactory()
          .createSynchronousProductBuilder(ProductDESElementFactory.getInstance());


        if (s != null)
        {
          superviserCombobox.addItem(dir);

          //System.out.println(s);          // what classes are being called

          //database of parameters
          for(final Parameter p : s.getParameters()) {
            AllParams.put(p.getID(),p);
          }

        }
      } catch (NoClassDefFoundError | ClassNotFoundException | UnsatisfiedLinkError
        | AnalysisConfigurationException exception) {

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

        try {

          final List<Parameter> newParams = tmp.getModelAnalyzerFactory()
            .createSynchronousProductBuilder(ProductDESElementFactory.getInstance()).getParameters();
          storeInDatabase();
          copyFromDatabase(newParams);
          mScrollParametersPanel.replaceView(newParams, DESContext);
        } catch (AnalysisConfigurationException  | ClassNotFoundException exception) {

          exception.printStackTrace();
        }

        //re-packing causes the frame to shrink/increase to preferred size
         frame.pack();
      }
    };

    superviserCombobox.addActionListener(syntheisSuperviserChanged);

    // superviserCombobox should have at least one item
    final ModelAnalyzerFactoryLoader first = (ModelAnalyzerFactoryLoader) superviserCombobox.getSelectedItem();

    try {
      mScrollParametersPanel = new ParameterJScrollPane(first.getModelAnalyzerFactory()
                                                        .createSynchronousProductBuilder(ProductDESElementFactory.getInstance()).getParameters(), DESContext);
    } catch (AnalysisConfigurationException  | ClassNotFoundException exception) {
      exception.printStackTrace();
    }
    //Finally, build the full dialog ...
    frame.add(mSuperviserPanel, BorderLayout.PAGE_START);
    frame.add(mScrollParametersPanel, BorderLayout.CENTER);
    frame.add(mButtons, BorderLayout.PAGE_END);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  public static void printMap() {
    for (final Entry<Integer,Parameter> entry : AllParams.entrySet()) {
      entry.getValue().printValue();
    }
  }

  //Values stored in GUI Components are stored in corresponding parameter then added to the database
  public static void storeInDatabase() {

    mScrollParametersPanel.commit();       //All ParameterPanels save their stored value in their corresponding parameter
    final List<Parameter> activeParameters =  mScrollParametersPanel.getParameters();

    for(final Parameter p: activeParameters) {  //overwrite stored parameters with new version
      AllParams.put(p.getID(), p);
    }
  }

  // updates the passed parameters to have same stored value as
  // corresponding one in database
  public static void copyFromDatabase(final List<Parameter> newParams) {

    for(final Parameter current: newParams)
      current.updateFromParameter(AllParams.get(current.getID()));

  }

   public static void createSynthesizer(final ModelAnalyzerFactoryLoader synth ) {

    SynchronousProductBuilder sythesizer;
    try {
      sythesizer = synth.getModelAnalyzerFactory()
                            .createSynchronousProductBuilder(ProductDESElementFactory.getInstance());

      final List<Parameter> parameters = sythesizer.getParameters();
      storeInDatabase();
      copyFromDatabase(parameters);

      //System.out.println(sythesizer);

      for(final Parameter current: parameters)
        current.commitValue();

    } catch (AnalysisConfigurationException | ClassNotFoundException exception) {
      exception.printStackTrace();
    }
  }
}
