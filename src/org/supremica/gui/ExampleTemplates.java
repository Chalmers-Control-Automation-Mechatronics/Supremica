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

import java.util.*;
import org.supremica.automata.templates.*;

/**
 * For convenient menu-access to a large set of examples.
 */

public class ExampleTemplates implements Iterable<TemplateGroup>
{
  private static ExampleTemplates templates = null;
  private final TemplateGroup DESBookExamples = new TemplateGroup(TemplateTypes.DESBookExample);
  private final TemplateGroup DESBookExercises = new TemplateGroup(TemplateTypes.DESBookExercise);
  private final TemplateGroup DESCourseAssignments = new TemplateGroup(TemplateTypes.DESCourseAssignment);
  private final TemplateGroup DESCourseAssignmentSolutions = new TemplateGroup(TemplateTypes.DESCourseAssignmentSolutions);
  private final TemplateGroup AIPExamples = new TemplateGroup(TemplateTypes.AIPExample);
  private final TemplateGroup CentralLockExamples = new TemplateGroup(TemplateTypes.CentralLockExample);
  private final TemplateGroup OperatorSupervisorExamples = new TemplateGroup(TemplateTypes.OperatorSupervisorExample);
  private final TemplateGroup CommunicationSystemExamples = new TemplateGroup(TemplateTypes.CommunicationSystemExamples);
  private final TemplateGroup ManufacturingSystemExamples = new TemplateGroup(TemplateTypes.ManufacturingSystemExamples);
  private final TemplateGroup Games = new TemplateGroup(TemplateTypes.Games);
  private final TemplateGroup ModuleExamples  = new TemplateGroup(TemplateTypes.ModuleExamples);
  private final TemplateGroup SchedulingExamples = new TemplateGroup(TemplateTypes.SchedulingExamples);
  private final TemplateGroup OtherExamples = new TemplateGroup(TemplateTypes.OtherExample);
  private final TemplateGroup TUMunichExamples = new TemplateGroup(TemplateTypes.TUMunichExamples);

  //private final TemplateGroup StandardComponents = new TemplateGroup(TemplateTypes.StandardComponent);
  private static final String basePrefix = "/includeInJarFile";
  private final List<TemplateGroup> mAllGroups = new LinkedList<TemplateGroup>();

  private ExampleTemplates()
  {
    initialize();
  }

  private void initialize()
  {
    initializeDESBookExamples();
    initializeDESBookExercises();
    initializeDESCourseAssignments();
    //initializeDESCourseAssignmentSolutions();
    initializeCentralLockExamples();
    initializeAIPExamples();
    initializeManufacturingSystemExamples();
    initializeOperatorSupervisorExamples();
    initializeCommunicationSystemExamples();
    initializeGames();
    initializeModuleExamples();
    initializeSchedulingExamples();
    initializeTUMunichExamples();
	initializeOtherExamples();
    //initializeStandardComponents();
  }

  private void initializeDESBookExamples()
  {
    final TemplateGroup thisGroup = DESBookExamples;
    final String prefix = basePrefix + "/CCSBookExamples/";
    thisGroup.addItem("Chapter 2 - Automaton", prefix + "Ch2_Automaton.xml");
    thisGroup.addItem("Chapter 2 - Synchronization", prefix + "Ch2_Synchronization.xml");
    thisGroup.addItem("Chapter 2 - Sub-automata and Refinement", prefix + "Ch2_Sub-automata_and_Refinement.xml");
    thisGroup.addItem("Chapter 2 - Why must \u03a3(A)=\u03a3(B)?", prefix + "Ch2_Why_must_sigmaA_sigmaB.xml");
    thisGroup.addItem("Chapter 2 - Marked Automaton", prefix + "Ch2_Marked_Automaton.xml");
    thisGroup.addItem("Chapter 2 - Forbidden States", prefix + "Ch2_Forbidden_States.xml");
    thisGroup.addItem("Chapter 2 - Reachability Tree and Graph", prefix + "Ch2_Reachability_Tree_and_Graph.xml");
    thisGroup.addItem("Chapter 3 - Specification", prefix + "Ch3_Specification.xml");
    thisGroup.addItem("Chapter 3 - Partial and Total Specifications", prefix + "Ch3_Partial_and_Total_Specifications.xml");
    thisGroup.addItem("Chapter 3 - Static and Dynamic Specifications", prefix + "Ch3_Static_and_Dynamic_Specifications.xml");
    thisGroup.addItem("Chapter 4 - Static and Dynamic Controller", prefix + "Ch4_Static_and_Dynamic_Controller.xml");
    thisGroup.addItem("Chapter 4 - Controllability", prefix + "Ch4_Controllability.xml");
    thisGroup.addItem("Chapter 4 - Uncontrollable States", prefix + "Ch4_Uncontrollable_States.xml");
    thisGroup.addItem("Chapter 4 - Trimming", prefix + "Ch4_Trimming.xml");
    thisGroup.addItem("Chapter 4 - Extending", prefix + "Ch4_Extending.xml");
    thisGroup.addItem("Chapter 4 - Supervisor Algorithm at Work", prefix + "Ch4_Supervisor_Algorithm_at_Work.xml");
    thisGroup.addItem("Chapter 4 - Non-Conflicting and Conflicting Supervisors", prefix + "Ch4_Non-Conflicting_and_Conflicting_Supervisors.xml");
    addGroup(thisGroup);
  }

  private void initializeDESBookExercises()
  {
    final TemplateGroup thisGroup = DESBookExercises;
    final String prefix = basePrefix + "/CCSBookExercises/";
    thisGroup.addItem("2.1 - PIN Code Reader", prefix + "Ex2_1.xml");
    thisGroup.addItem("2.2 - Kanban", prefix + "Ex2_2.xml");
    thisGroup.addItem("2.6 - Man, Wolf, Goat, Cabbage", prefix + "Ex2_6.xml");
    // OBS! THE EXERCISES BELOW HAVE CHANGED NUMBER... AND THE BOOK HAS CHANGED NAME!
    // WE NEED TO SWITCH TO SVN!
    thisGroup.addItem("4.2 - Man, Wolf, Goat, Cabbage Revisited", prefix + "Ex4_1.xml");
    thisGroup.addItem("4.3 - AGV System", prefix + "Ex4_2.xml");
    thisGroup.addItem("4.5 - Cat & Mouse", prefix + "Ex4_4.xml");
    thisGroup.addItem("4.6 - Robot & Machine: All events controllable", prefix + "Ex4_5_all_con.xml");
    thisGroup.addItem("4.6 - Robot & Machine: Put is uncontrollable", prefix + "Ex4_5_b_uncon.xml");
    thisGroup.addItem("4.7 - Two Machines & a Buffer", prefix + "Ex4_6.xml");
    addGroup(thisGroup);
  }

  private void initializeDESCourseAssignments()
  {
    final TemplateGroup thisGroup = DESCourseAssignments;
    final String prefix = basePrefix + "/CCSCourseAssignments/";
    thisGroup.addItem("FMS Line", prefix + "FMSLine.xml");
    thisGroup.addItem("Production System", prefix + "ProdSysStudent.xml");
    thisGroup.addItem("Communication Channel", prefix + "CommunicationChannel.xml");
    addGroup(thisGroup);
  }

  @SuppressWarnings("unused")
  private void initializeDESCourseAssignmentSolutions()
  {
    final TemplateGroup thisGroup = DESCourseAssignmentSolutions;
    final String prefix = basePrefix + "/CCSCourseAssignments/solutions/";
    thisGroup.addItem("FMS Without Feedback", prefix + "FMSLine.xml");
    thisGroup.addItem("FMS With Feedback", prefix + "FMSLoop.xml");
    thisGroup.addItem("Communication Channel Specification", prefix + "comm_spec.xml");
    addGroup(thisGroup);
  }

  private void initializeOtherExamples()
  {
    final TemplateGroup thisGroup = OtherExamples;
    final String prefix = basePrefix + "/OtherExamples/";
    thisGroup.addItem("Automatic Car Park Gate", prefix + "AutomaticCarParkGate.xml");
    thisGroup.addItem("Ball Process", prefix + "ballProcess.xml");
    thisGroup.addItem("Ball Process - Gatekeeper", prefix + "ballProcessGatekeeper.xml");
    thisGroup.addItem("Car Window Control System", prefix + "big_bmw.xml");
    thisGroup.addItem("Car fh", prefix + "/ModuleExamples/car_fh/car_fh.wmod");
    thisGroup.addItem("Coffee Machine", prefix + "CoffeeMachine.wmod");
    thisGroup.addItem("Exponential determinisation", prefix + "ExponentialDeterminisation.xml");
    thisGroup.addItem("Mars Pathfinder", prefix + "MarsPathfinder.wmod");
    thisGroup.addItem("Observation Equivalence Execise", prefix + "ObservationEquivalenceExercise.xml");
    //thisGroup.addItem("Passenger Land-Transportation System", prefix + "PLanTS.xml");
    thisGroup.addItem("Passenger Land-Transportation System", basePrefix + "/ModuleExamples/other/PLanTS.wmod");
    thisGroup.addItem("Professors, Pen and Paper", prefix + "ProfessorsPenPaper.xml");
    thisGroup.addItem("Telecommunications Network (incomplete)", prefix + "telecommunicationsNetwork.xml");
    addGroup(thisGroup);
  }

  private void initializeTUMunichExamples()
  {
    final TemplateGroup thisGroup = TUMunichExamples;
    final String prefix = basePrefix + "/TUMunichSES/";
    thisGroup.addItem("Portal2_Test", prefix + "Portal2_Test.wmod");
    thisGroup.addItem("Portal2_V20_init", prefix + "Portal2_V20_init.wmod");
    addGroup(thisGroup);
  }

  private void initializeAIPExamples()
  {
    final TemplateGroup thisGroup = AIPExamples;
    final String prefix = basePrefix + "/OtherExamples/aip/";
    thisGroup.addItem("Assembly station 1", prefix + "System1_system1.xml");
    //thisGroup.addItem("AIP System 1 - Top AS 1", prefix + "System1_Top_AS1.xml");
    thisGroup.addItem("Assembly station 2", prefix + "System2_system2.xml");
    //thisGroup.addItem("AIP System 2 - Top AS 2", prefix + "System2_Top_AS2.xml");
    thisGroup.addItem("Assembly station 3", prefix + "System3_system3.xml");
    //thisGroup.addItem("AIP System 3 - Top AS 3", prefix + "System3_Top_AS3.xml");
    thisGroup.addItem("Transport unit 1", prefix + "System4_system4.xml");
    //thisGroup.addItem("AIP System 4 - Top TU 1", prefix + "System4_Top_TU1.xml");
    thisGroup.addItem("Transport unit 2", prefix + "System5_system5.xml");
    //thisGroup.addItem("AIP System 5 - Top TU 2", prefix + "System5_Top_TU2.xml");
    thisGroup.addItem("Transport unit 3", prefix + "System6_system6.xml");
    thisGroup.addItem("Transport unit 4", prefix + "System7_system7.xml");
    //thisGroup.addItem("AIP System 7 - Top TU 4", prefix + "System7_Top_TU4.xml");
    thisGroup.addItem("\"Complete\" system (all the above)", prefix + "All.xml");
    thisGroup.addItem("Largest coherent part", prefix + "LargestCoherentPart.xml");
    addGroup(thisGroup);
  }

  private void initializeCentralLockExamples()
  {
    final TemplateGroup thisGroup = CentralLockExamples;
    // THREE DOORS
    thisGroup.addItem("Central Locking - 3 Doors",
                      basePrefix +
                      "/ModuleExamples/central_locking/verriegel3.wmod");
    //thisGroup.addItem("Central Locking - 3 Doors - Uncontrollable", prefix + "verriegel3_uncontrollable.xml");
    //thisGroup.addItem("Central Locking - 3 Doors - Synchronized Plants", prefix + "verriegel3_joint.xml");
    //thisGroup.addItem("Central Locking - 3 Doors - Synchronized Plants Uncontrollable", prefix + "verriegel3_joint_uncontrollable.xml");
    // FOUR DOORS
    thisGroup.addItem("Central Locking - 4 Doors",
                      basePrefix +
                      "/ModuleExamples/central_locking/verriegel4.wmod");
    // Subsystems
    thisGroup.addItem("Central Locking - ftuer",
                      basePrefix +
                      "/ModuleExamples/central_locking/ftuer.wmod");
    thisGroup.addItem("Central Locking - koordwsp",
                      basePrefix +
                      "/ModuleExamples/central_locking/koordwsp.wmod");
    addGroup(thisGroup);
  }

  private void initializeOperatorSupervisorExamples()
  {
    final TemplateGroup thisGroup = OperatorSupervisorExamples;
    final String prefix = basePrefix + "/OperatorSupervisor/";
    thisGroup.addItem("Warehouse", prefix + "warehouse.xml");
    thisGroup.addItem("Warehouse k=2", prefix + "warehouse_k2.xml");
    thisGroup.addItem("Warehouse k=5", prefix + "warehouse_k5.xml");
    thisGroup.addItem("Warehouse k=7", prefix + "warehouse_k7.xml");
    thisGroup.addItem("Warehouse k=10", prefix + "warehouse_k10.xml");
    thisGroup.addItem("Warehouse k=13", prefix + "warehouse_k13.xml");
    addGroup(thisGroup);
  }

  private void initializeCommunicationSystemExamples()
  {
    final TemplateGroup thisGroup = CommunicationSystemExamples;
    final String prefix = basePrefix + "/CommunicationSystemExamples/";
    thisGroup.addItem("Parrow's Protocol", prefix + "ParrowsProtocol.xml");
    thisGroup.addItem("Alternating Bit Protocol", prefix + "AlternatingBitProtocol1.xml");
    thisGroup.addItem("Alternating Bit Protocol (variant)", prefix + "AlternatingBitProtocol2.xml");
    thisGroup.addItem("CSMA/CD Protocol", prefix + "CSMA_CD.xml");
    addGroup(thisGroup);
  }

  private void initializeManufacturingSystemExamples()
  {
    final TemplateGroup thisGroup = ManufacturingSystemExamples;
    final String prefix = basePrefix + "/ManufacturingExamples/";
    thisGroup.addItem("Automated Guided Vehicles", prefix + "agv.xml");
    //thisGroup.addItem("Automated Guided Vehicle, immediate events", prefix + "agvImmediate.xml");
    thisGroup.addItem("Circular Table", prefix + "circularTable.xml");
    thisGroup.addItem("Circular Table (variant)", prefix + "rotationTable.xml");
    thisGroup.addItem("Dosing Unit", prefix + "dosingUnit.xml");
    thisGroup.addItem("Dosing Tank - EFA", prefix + "dosingtankEFA.wmod");
    thisGroup.addItem("Flexible Manufacturing System", prefix + "FlexibleManufacturingSystem.xml");
    thisGroup.addItem("Flexible Manufacturing System (variant, incomplete)", prefix + "FlexibleManufacturingSystemVariant.xml");
    thisGroup.addItem("Flexible Production Cell", prefix + "ftechnik.wmod");
    thisGroup.addItem("Flexible Production Cell (variant)", prefix + "FlexibleProductionCell.xml");
    thisGroup.addItem("Intertwined Product Cycles", prefix + "IntertwinedProductCycles.xml");
    thisGroup.addItem("Machine Buffer Machine", prefix + "machineBufferMachine.xml");
    thisGroup.addItem("Parallel Manufacturing Example", prefix + "parallelManufacturingExample.xml");
    thisGroup.addItem("Production Cell for Mounting Frames (incomplete)", prefix + "fzelle.wmod");
    thisGroup.addItem("Robot Assembly Cell", prefix + "robotAssemblyCell.xml");
    thisGroup.addItem("Simple Manufacturing Example", prefix + "simpleManufacturingExample.xml");
    thisGroup.addItem("Tank Process", prefix + "tankProcess.xml");
    thisGroup.addItem("Transfer Line", prefix + "transfer.xml");
    thisGroup.addItem("Train Testbed", prefix + "tbed_ctct.xml");
    thisGroup.addItem("Train Testbed (variant)", prefix + "tbed_valid.xml");
    thisGroup.addItem("Welding Robots", prefix + "weldingRobots.xml");
    //thisGroup.addItem("Volvo cell", prefix + "volvo.xml");
    thisGroup.addItem("Volvo cell", prefix + "volvo_no_sup.xml");
    addGroup(thisGroup);
  }

  private void initializeModuleExamples()
  {
    final TemplateGroup thisGroup = ModuleExamples;
    final String prefix = basePrefix + "/ModuleExamples/";
    thisGroup.addItem("Big Factory", prefix + "big_factory/bfactory.wmod");
    thisGroup.addItem("Central Locking - Dreitueren", prefix + "central_locking/dreitueren.wmod");
    //thisGroup.addItem("Central Locking - verriegel3", prefix + "central_locking/verriegel3.wmod");
    //thisGroup.addItem("Central Locking - verriegel4", prefix + "central_locking/verriegel4.wmod");
    //thisGroup.addItem("Fischertechnik", prefix + "fischertechnik/fischertechnik.wmod");
    //thisGroup.addItem("Stick Picking Game", prefix + "other/stick_picking_game.wmod");
    addGroup(thisGroup);
  }

  private void initializeGames()
  {
    final TemplateGroup thisGroup = Games;
    final String prefix = basePrefix + "/Games/";
    //thisGroup.addItem("Man, Wolf, Goat, Cabbage", basePrefix + "/CCSBookExercises/" + "Ex2_6.xml");
    thisGroup.addItem("Man, Wolf, Goat, Cabbage", prefix + "ManWolfGoatCabbage.wmod");
    thisGroup.addItem("Stick Picking Game", basePrefix + "/ModuleExamples/other/stick_picking_game.wmod");
    thisGroup.addItem("Tic Tac Toe", basePrefix + "/ModuleExamples/other/tictactoe.wmod");
    //thisGroup.addItem("Wine Merchant's Problem", prefix + "WineMerchant.xml");
    thisGroup.addItem("Wine Merchant's Problem", basePrefix + "/ModuleExamples/other/winemerchant.wmod");
    addGroup(thisGroup);
  }

  private void initializeSchedulingExamples()
  {
    final TemplateGroup thisGroup = SchedulingExamples;
    final String prefix = basePrefix + "/Scheduling/";
    thisGroup.addItem("2 robots (from HBS)", prefix + "hbs_example.xml");
    thisGroup.addItem("2 robots with alternatives (from PV35)", prefix + "pv35_example.xml");
    thisGroup.addItem("3 robots", prefix + "3_2.xml");
    thisGroup.addItem("Velocity balancing example", prefix + "velocity_balancing_example.xml");
    thisGroup.addItem("2 robots, uncontrollable alternatives", prefix + "uc_alternatives.xml");
    thisGroup.addItem("Fisher & Thompson, 6x6 (6 robots, 6 zones)", prefix + "ft06.xml");
    thisGroup.addItem("Fisher & Thompson, 10x10 (10 robots, 10 zones)", prefix + "ft10.xml");
    thisGroup.addItem("Fisher & Thompson, 20x5 (20 robots, 5 zones)", prefix + "ft20.xml");
    thisGroup.addItem("Modified Fisher & Thompson, 6x6 (6 robots, 6 zones, no buffers)", prefix + "ft06_no_buffers.xml");
    thisGroup.addItem("Modified Fisher & Thompson, 10x10 (10 robots, 10 zones, no buffers)", prefix + "ft10_no_buffers.xml");
    thisGroup.addItem("Modified Fisher & Thompson, 20x5 (20 robots, 5 zones, no buffers)", prefix + "ft20_no_buffers.xml");
    addGroup(thisGroup);
  }

  private void addGroup(final TemplateGroup group)
  {
    if (!group.isEmpty()) {
      mAllGroups.add(group);
    }
  }

  public synchronized static ExampleTemplates getInstance()
  {
    if (templates == null)
    {
      templates = new ExampleTemplates();
    }

    return templates;
  }

  public boolean isEmpty()
  {
    return mAllGroups.isEmpty();
  }

  public Iterator<TemplateGroup> iterator()
  {
    return mAllGroups.iterator();
  }
}
