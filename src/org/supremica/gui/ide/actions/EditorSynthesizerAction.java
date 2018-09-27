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

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.javabdd.BDD;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.BDD.EFA.BDDExtendedSynthesizer;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.Guard.BDDExtendedGuardGenerator;
import org.supremica.gui.EditorSynthesizerDialog;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;
import org.supremica.properties.Config;

public class EditorSynthesizerAction extends IDEAction
{
  private static final long serialVersionUID = 1L;
  private final Logger logger = LogManager.getLogger(IDE.class);

  //#########################################################################
  //# Constructor
  public EditorSynthesizerAction(final List<IDEAction> actionList)
  {
    super(actionList);
    setEditorActiveRequired(true);

    final String actName =
      "Symbolic (BDD) Synthesis/Optimization on TEFAs...";
    final String description = "Synthesize a modular supervisor by adding "
                               + "guards to the original automata";
    final ImageIcon icon =
      new ImageIcon(IDE.class.getResource("/icons/synthesize16.gif"));

    putValue(Action.NAME, actName);
    putValue(Action.SHORT_DESCRIPTION, description);
    putValue(Action.SMALL_ICON, icon);
  }

  //#########################################################################
  //# Overridden methods
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    doAction();
  }

  @Override
  public void doAction()
  {
    final ModuleSubject module =
      ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();

    final int nbrOfComponents = module.getComponentList().size();
    if (nbrOfComponents == 0) {
      JOptionPane.showMessageDialog(ide.getFrame(), "Module is empty.");
      return;
    }

    /***
     * Check that we do not have any for-each blocks that create
     * IndexedIdentifierSubject events (see issue #56)
     * Issue #105. WIP.
     ***/
    for (final Proxy sub : module.getComponentList()) {
      if (sub instanceof ForeachProxy) {
        final String msg = "Module contains for-each blocks\n" +
                           "that need to be instantiated first.\n" +
                           "Choose Edit -> Instantiate.";
        JOptionPane.showMessageDialog(ide.getFrame(),
                                      msg,
                                      "Unable to handle...",
                                      JOptionPane.WARNING_MESSAGE);
        return;
      }
    }

    // get the stored or default options
    final EditorSynthesizerOptions options = new EditorSynthesizerOptions();

    // collect controllable event names from the module
    final Vector<String> eventNames = new Vector<String>();
    for (final EventDeclSubject sigmaS : module.getEventDeclListModifiable())
    {
      if (sigmaS.getKind() == EventKind.CONTROLLABLE)
      {
        eventNames.add(sigmaS.getName());
      }
    }

    final Vector<String> variableNamesForBox = new Vector<String>();
    variableNamesForBox.add("No variable selected");
    for (final Proxy sub : module.getComponentList()) {
      if (sub instanceof VariableComponentProxy) {
        variableNamesForBox.add(((VariableComponentProxy) sub).getName());
      }
    }

    final Vector<String> eventNamesForBox = new Vector<String>(eventNames);
    eventNamesForBox.add(0, "Generate guards for ALL controllable events");

    final EditorSynthesizerDialog synthesizerDialog =
      new EditorSynthesizerDialog(ide.getFrame(), nbrOfComponents, options,
                                  eventNamesForBox, variableNamesForBox);
    synthesizerDialog.show();

    if (!options.getDialogOK()) {
      return;
    }

    final ExtendedAutomata exAutomata = options.getOptimization()
      ? new ExtendedAutomata(module, (int) options.getGlobalClockDomain())
      : new ExtendedAutomata(module);

    final BDDExtendedSynthesizer bddSynthesizer =
      new BDDExtendedSynthesizer(exAutomata, options);

    if (logger.isDebugEnabled()) {
      final int nbrBDDVars =
        bddSynthesizer.getBDDAutomata().getNumberOfUsedBDDVariables();
      logger.info("Number of used BDD variables: " + nbrBDDVars);
    }

    // do the work
    bddSynthesizer.synthesize(options);

    logger.info("Synthesis completed on "
                + new Date().toString() + ".");

    logger.info("Synthesis completed after "
                + bddSynthesizer.getSynthesisTimer().toString() + ".");
    if (options.getOptimization()) {
      final BDD result = bddSynthesizer.getResult();
      final long optimalTime =
        bddSynthesizer.getBDDAutomata().getOptimalTime(result);
      logger.info("The minimum time to 'safely' reach a marked state from " +
                  " the initial state: " + optimalTime + ".");
    }

    if (!options.getOptVaribale().isEmpty()) {
      logger.info("The minimum value of variable " + options.getOptVaribale()
                  + " among the reachable marked states is: "
                  + bddSynthesizer.getBDDAutomata().getMinValueOfVar() + ".");
    }

    logger.info("The " + options.getSynthesisType().toString()
                + " supervisor consists of "
                + bddSynthesizer.nbrOfStates() + " states.");

    if (options.getPeakBDD())
      logger.info("The maximal number of BDD nodes used is "
                  + bddSynthesizer.peakBDDNodes());

    final List<VariableComponentProxy> pars =
      bddSynthesizer.getAutomata().getParameters();
    if (!pars.isEmpty())
      logger.info("The feasible values for the parameters are:");
    for (final VariableComponentProxy variable : pars) {
      logger.info(bddSynthesizer.getFeasibleValues(variable.getName()));
    }

    // handle the guards
    if (!(options.getSaveInFile() || options.getSaveIDDInFile()
      || options.getPrintGuard() || options.getAddGuards())) {
      // no guard related option is set, quite silently
      return;
    }

    if (bddSynthesizer.nbrOfStates() == 0) {
      logger.info("No guard can be derived from empty supervisor.");
      return;
    }

    // generate guards...
    bddSynthesizer.generateGuard(eventNames, options);
    logger.info("The guards were generated in "
      + bddSynthesizer.getGuardTimer().toString() + ".");
    // print guards or save them in a excel file.
    if (options.getSaveInFile() || options.getPrintGuard()) {
      final Map<EdgeProxy, ExtendedAutomaton> edge2ExAutomatonMap =
        bddSynthesizer.getBDDAutomata().getEdge2ExAutomatonMap();
      final Map<String,BDDExtendedGuardGenerator> event2GuardGen =
        bddSynthesizer.getEvent2GuardGen();
      final Map<String, List<Entry<EdgeProxy,ExtendedAutomaton>>>
      eventGeoInfo = new HashMap<>();
      // Make things deterministic...
      for(final String event: eventNames) {
        if (!event2GuardGen.containsKey(event))
           continue;
        final BDDExtendedGuardGenerator guardGen = event2GuardGen.get(event);
        final Map<EdgeProxy, String> evensEdge2GuardMap =
          guardGen.getEdge2GuardMap();
        final ArrayList<Entry<EdgeProxy, ExtendedAutomaton>> entries =
           new ArrayList<>();
        for(final Entry<EdgeProxy, ExtendedAutomaton> e:
            edge2ExAutomatonMap.entrySet()) {
          final String edgesGuard = evensEdge2GuardMap.get(e.getKey());
          if (evensEdge2GuardMap.containsKey(e.getKey()) &&
             edgesGuard != BDDExtendedGuardGenerator.TRUE) {
            entries.add(e);
          }
        }
        Collections.sort(entries, // sort based on automaton and edges names
                         new Comparator<Entry<EdgeProxy,ExtendedAutomaton>>() {
          @Override
          public int compare(final Entry<EdgeProxy,ExtendedAutomaton> o1,
                             final Entry<EdgeProxy,ExtendedAutomaton> o2)
          {
            final String aut1 = o1.getValue().getName();
            final String aut2 = o2.getValue().getName();
            if (aut1.equals(aut2)) {
              final String sourceName1 = o1.getKey().getSource().getName();
              final String sourceName2 = o2.getKey().getSource().getName();
              return sourceName1.compareTo(sourceName2);
            }
            else {
              return aut1.compareTo(aut2);
            }
          }
        });
        eventGeoInfo.put(event, entries);
      }
      // Format strings...
      final List<List<String>> guardInfoList = new ArrayList<>();
      for(final String event: eventGeoInfo.keySet()) {
        final List<Entry<EdgeProxy, ExtendedAutomaton>> edge2AutMap =
          eventGeoInfo.get(event);
        final Map<EdgeProxy, String> edge2guardMap =
          event2GuardGen.get(event).getEdge2GuardMap();
        for(final Entry<EdgeProxy, ExtendedAutomaton> e: edge2AutMap) {
          final ArrayList<String> entry = new ArrayList<>(4);
          final String edge =
            String.format("<%s, %s, %s>",
                           e.getKey().getSource().getName(),
                           event,
                           e.getKey().getTarget().getName());
          entry.add(edge);
          final String aut = e.getValue().getName();
          entry.add(aut);
          String guard = edge2guardMap.get(e.getKey());
          final int nbrOfTerms =
            event2GuardGen.get(event).getGuard2NbrOfTerms().get(guard);
          if (guard.equals(BDDExtendedGuardGenerator.FALSE)) {
            guard = "FALSE";
          }
          entry.add(guard);
          entry.add("" + nbrOfTerms);
          guardInfoList.add(entry);
        }
      }
      // Save guards in a Excel file...
      if (options.getSaveInFile()) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        final int returnVal = chooser.showOpenDialog(ide.getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          final String path = chooser.getSelectedFile().getAbsolutePath();
          Config.FILE_SAVE_PATH.set(path);
          String name = module.getName();
          if (name.isEmpty())
            name = "event_edge_guard_list";
          final File file = new File(path + "/" + name + ".xls");
          try {
            final FileWriter fstream = new FileWriter(file);
            final BufferedWriter out = new BufferedWriter(fstream);
            out.write("Edge <source, event, target>" + "\t"
                      + "Automaton" + "\t"
                      + "Guard expression" + "\t"
                      + "Guard size");
            out.newLine();
            out.newLine();
            for(final List<String> e: guardInfoList) {
              out.write(String.join("\t", e));
              out.newLine();
            }
            out.close();
          }
          catch (final Exception e) {
            logger.error("Could not save the event-guard pairs in the file: "
                         + e.getMessage());
          }
        }
      }
      // print guards in the console...
      if (options.getPrintGuard()) {
        for (final List<String> e:guardInfoList) {
          if (e.get(2).equals("FALSE")) {
            logger.info("Edge " + e.get(0) + " in automaton \""
                        + e.get(1) + "\" is FORBIDDEN.");
          }
          else {
            logger.info("Guard " + e.get(2) + " with the term size " + e.get(3)
                        + " is generated for edge " + e.get(0)
                        + " of automaton \"" + e.get(1) + "\".");
          }
        }
      }
    }

    // Add guards and variables generated from automaton to the model...
    if (options.getAddGuards()) {
      bddSynthesizer.addGuardsToAutomata();
    }

    // Cleanup...
    bddSynthesizer.done();

    // Call TUM PLC Code generator
    // TODO: * Improve the integration: Get some outputs (at least Pass/Fail) from the external toolbox and pass them to Supremica/Waters logger.info()
    //       * Handling of network (NAS) repositories (e.g. //nas.ads. ...) -> Pass the relative paths
    //       * Only Windows if considered here
    //         -> Add a check for the OS
    //         -> Handle the file separators properly: "\\", "/", File.separator ...
    //       * Probably move this code (which is getting larger) to another file and functions
    //
    if (options.getGenPLCCodeTUMBox()) {
        logger.info("Generating PLC Code using TUM external toolbox...");
        tryLabel:
        try {
            logger.info("\tChecking the current model file");
            final String modulename = module.getName(); // Module's name without extension

            // Retrieve the path to access the Wmod file to be passed to the converter
            String modulePath;
            URI moduleUri = module.getLocation(); // already returns null if the module (included in the JAR) is not a Wmod file
            if (moduleUri == null) {
                modulePath = null;
            } else {
                modulePath = moduleUri.getPath(); // returns null if in the JAR, returns the path otherwise
            }

            // modulePath is null if the module is included in the JAR -> Save the current module into a selected folder
            if (modulePath == null) { // modulePath is null if the module is included in the JAR
                // Save as ...
                logger.warn("The current model first needs to be saved as an external WMOD file. Please select the destination folder.");
                final DocumentContainerManager manager = ide.getActiveDocumentContainer().getIDE().getDocumentContainerManager();
                manager.saveActiveContainerAs();

                // Retrieve the new module's path (and name)
                moduleUri = module.getLocation();
                modulePath = moduleUri.getPath(); // retrieve the new module's path (and name)
            }
            logger.debug("moduleUri   : " + moduleUri);
            logger.debug("modulePath  : " + modulePath);

            // Select the Output path to be passed to the converter
            String destFoldPath = modulePath;
            if (!options.getSavPLCCodeTUMBox()) { // adding the Output option
                logger.warn("Selection of the PLC code destination. Please select the output folder.");
                final String dialogTitle;
                if (options.getTypePLCCodeTUM()=="standalone") {
                    dialogTitle = "Select the destination folder";
                } else if (options.getTypePLCCodeTUM()=="TwinCAT") {
                    dialogTitle = "Select the TwinCAT project folder";
                } else {
                    dialogTitle = "Title undefined"; // Should never be called
                }

                final JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setCurrentDirectory(new File("/" + modulePath));
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setDialogTitle(dialogTitle);
                final int returnVal = chooser.showOpenDialog(ide.getFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    destFoldPath = chooser.getSelectedFile().getAbsolutePath();
                    destFoldPath = destFoldPath.replace("\\", "/");
                } else {
                    logger.warn("\tSelection canceled! Saving in the module's folder:" + modulePath);
                    destFoldPath = modulePath;
                }
            }
            logger.debug("destFoldPath: " + destFoldPath);

            // Check and clean up the paths
            if (modulePath.startsWith("//")) {
                logger.error("The external PLC code converter does not support network repositories!");
                break tryLabel;
            } else if (modulePath.startsWith("/")) {
                modulePath = modulePath.substring(1); // remove the first /
            }
            if (destFoldPath.startsWith("//")) {
                logger.error("The external PLC code converter does not support network repositories!");
                break tryLabel;
            } else if (destFoldPath.startsWith("/")) {
                destFoldPath = destFoldPath.substring(1); // remove the first /
            }
            // remove the filename : NOTA: that is actually not needed since we don't pass the destFoldPath if we save in the same folder
            // if (destFoldPath.contains(".wmod")) {
                // final int i = destFoldPath.lastIndexOf("/");
                // if (i > -1) {
                    // destFoldPath = destFoldPath.substring(0, i);
                // }
            // }

            // Building the command to pass to the external converter
            // TODO: Now that the number of command is growing it would make sense to create a dedicated function
            // If would be simpler to handle specific cases, such as -h which should directly return
            logger.info("\tBuilding the command");
            ArrayList<String> command = new ArrayList<>(Arrays.asList("cmd", "/k", "start", "STCodeConverter.exe"));
            if (options.getTypePLCCodeTUM()=="standalone") { // Standalone (default) option, with filepath
                List<String> addargs = Arrays.asList("-f", modulePath);
                command.addAll(addargs);
            } else if (options.getTypePLCCodeTUM()=="TwinCAT") { // TwinCAT option, with filepath
                List<String> addargs = Arrays.asList("-tc", "-f", modulePath);
                command.addAll(addargs);
            } else { // Help option
                command.add("-h");
            }
            if (options.getPLCCodeTUMefaBox()) { // adding the EFA option
                List<String> addargs = Arrays.asList("-e");
                command.addAll(addargs);
            }
            if (!options.getSavPLCCodeTUMBox()) { // adding the Output option
                List<String> addargs = Arrays.asList("-o", destFoldPath);
                command.addAll(addargs);
            }
            logger.debug("Command built: " + command);

            // Building the external process and starting it
            logger.info("\tStarting the external process");
            final ProcessBuilder pb = new ProcessBuilder(command);
            final Process p = pb.start();
            logger.warn("External process started! Please check the external console for output.");
        }
        catch (final Exception ex) {
            logger.error("Exception while generating PLC Code using TUM external toolbox");
            ex.printStackTrace();
        }
        finally{
            // TODO: finally of Generating PLC Code using TUM external toolbox
            logger.debug("TODO: finally of Generating PLC Code using TUM external toolbox");
        }
        // TODO: Get some outputs from the external toolbox?
    }
  }
}