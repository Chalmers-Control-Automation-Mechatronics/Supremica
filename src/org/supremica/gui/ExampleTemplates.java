
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.gui;

import java.util.*;
import org.supremica.automata.templates.*;

/**
 * For comvenient menu-access to a large set of examples.
 */
public class ExampleTemplates
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
    
    //private final TemplateGroup StandardComponents = new TemplateGroup(TemplateTypes.StandardComponent);
    private static final String basePrefix = "/includeInJarFile";
    private List allGroups = new LinkedList();
    
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
	initializeOtherExamples();
        //initializeStandardComponents();
    }
    
    private void initializeDESBookExamples()
    {
        TemplateGroup thisGroup = DESBookExamples;
        
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/CCSBookExamples/";
        
        thisGroup.addItem(new TemplateItem("Chapter 2 - Automaton", prefix + "Ch2_Automaton.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 2 - Synchronization", prefix + "Ch2_Synchronization.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 2 - Sub-automata and Refinement", prefix + "Ch2_Sub-automata_and_Refinement.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 2 - Why must \u03a3(A)=\u03a3(B)?", prefix + "Ch2_Why_must_sigmaA_sigmaB.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 2 - Marked Automaton", prefix + "Ch2_Marked_Automaton.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 2 - Forbidden States", prefix + "Ch2_Forbidden_States.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 2 - Reachability Tree and Graph", prefix + "Ch2_Reachability_Tree_and_Graph.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 3 - Specification", prefix + "Ch3_Specification.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 3 - Partial and Total Specifications", prefix + "Ch3_Partial_and_Total_Specifications.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 3 - Static and Dynamic Specifications", prefix + "Ch3_Static_and_Dynamic_Specification.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 4 - Static and Dynamic Controller", prefix + "Ch4_Static_and_Dynamic_Specification.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 4 - Controllability", prefix + "Ch4_Controllability.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 4 - Uncontrollable States", prefix + "Ch4_Uncontrollable_States.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 4 - Trimming", prefix + "Ch4_Trimming.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 4 - Extending", prefix + "Ch4_Extending.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 4 - Supervisor Algorithm at Work", prefix + "Ch4_Supervisor_Algorithm_at_Work.xml"));
        thisGroup.addItem(new TemplateItem("Chapter 4 - Non-Conflicting and Conflicting Supervisors", prefix + "Ch4_Non-Conflicting_and_Conflicting_Supervisors.xml"));
    }
    
    private void initializeDESBookExercises()
    {
        TemplateGroup thisGroup = DESBookExercises;
        
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/CCSBookExercises/";
        
        thisGroup.addItem(new TemplateItem("2.1 - PIN Code Reader", prefix + "Ex2_1.xml"));
        thisGroup.addItem(new TemplateItem("2.2 - Kanban", prefix + "Ex2_2.xml"));
        thisGroup.addItem(new TemplateItem("2.6 - Man, Wolf, Goat, Cabbage", prefix + "Ex2_6.xml"));
        // OBS! THE EXERCISES BELOW HAVE CHANGED NUMBER... AND THE BOOK HAS CHANGED NAME!
        // WE NEED TO SWITCH TO SVN!
        thisGroup.addItem(new TemplateItem("4.2 - Man, Wolf, Goat, Cabbage Revisited", prefix + "Ex4_1.xml"));
        thisGroup.addItem(new TemplateItem("4.3 - AGV System", prefix + "Ex4_2.xml"));
        thisGroup.addItem(new TemplateItem("4.5 - Cat & Mouse", prefix + "Ex4_4.xml"));
        thisGroup.addItem(new TemplateItem("4.6 - Robot & Machine: All events controllable", prefix + "Ex4_5_all_con.xml"));
        thisGroup.addItem(new TemplateItem("4.6 - Robot & Machine: Put is uncontrollable", prefix + "Ex4_5_b_uncon.xml"));
        thisGroup.addItem(new TemplateItem("4.7 - Two Machines & a Buffer", prefix + "Ex4_6.xml"));
    }
    
    private void initializeDESCourseAssignments()
    {
        TemplateGroup thisGroup = DESCourseAssignments;
        
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/CCSCourseAssignments/";
        
        thisGroup.addItem(new TemplateItem("FMS Line", prefix + "FMSLine.xml"));
        thisGroup.addItem(new TemplateItem("Production System", prefix + "ProdSysStudent.xml"));
        thisGroup.addItem(new TemplateItem("Communication Channel", prefix + "CommunicationChannel.xml"));
    }
    
    private void initializeDESCourseAssignmentSolutions()
    {
        TemplateGroup thisGroup = DESCourseAssignmentSolutions;
        
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/CCSCourseAssignments/solutions/";
        
        thisGroup.addItem(new TemplateItem("FMS Without Feedback", prefix + "FMSLine.xml"));
        thisGroup.addItem(new TemplateItem("FMS With Feedback", prefix + "FMSLoop.xml"));
        thisGroup.addItem(new TemplateItem("Communication Channel Specification", prefix + "comm_spec.xml"));
    }
    
    private void initializeOtherExamples()
    {
        TemplateGroup thisGroup = OtherExamples;
        
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/OtherExamples/";
        
        thisGroup.addItem(new TemplateItem("Automatic Car Park Gate", prefix + "AutomaticCarParkGate.xml"));
        thisGroup.addItem(new TemplateItem("Ball Process", prefix + "ballProcess.xml"));
        thisGroup.addItem(new TemplateItem("Ball Process - Gatekeeper", prefix + "ballProcessGatekeeper.xml"));
        thisGroup.addItem(new TemplateItem("Car Window Control System", prefix + "big_bmw.xml"));
        thisGroup.addItem(new TemplateItem("Coffee Machine", prefix + "CoffeeMachine.wmod"));
        thisGroup.addItem(new TemplateItem("Observation Equivalence Execise", prefix + "ObservationEquivalenceExercise.xml"));
        //thisGroup.addItem(new TemplateItem("Passenger Land-Transportation System", prefix + "PLanTS.xml"));
        thisGroup.addItem(new TemplateItem("Passenger Land-Transportation System", basePrefix + "/ModuleExamples/other/PLanTS.wmod"));
        thisGroup.addItem(new TemplateItem("Professors, Pen and Paper", prefix + "ProfessorsPenPaper.xml"));
        thisGroup.addItem(new TemplateItem("Telecommunications Network (incomplete)", prefix + "telecommunicationsNetwork.xml"));
    }
    
    private void initializeAIPExamples()
    {
        TemplateGroup thisGroup = AIPExamples;
        
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/OtherExamples/aip/";
        
        thisGroup.addItem(new TemplateItem("Assembly station 1", prefix + "System1_system1.xml"));
        //thisGroup.addItem(new TemplateItem("AIP System 1 - Top AS 1", prefix + "System1_Top_AS1.xml"));
        thisGroup.addItem(new TemplateItem("Assembly station 2", prefix + "System2_system2.xml"));
        //thisGroup.addItem(new TemplateItem("AIP System 2 - Top AS 2", prefix + "System2_Top_AS2.xml"));
        thisGroup.addItem(new TemplateItem("Assembly station 3", prefix + "System3_system3.xml"));
        //thisGroup.addItem(new TemplateItem("AIP System 3 - Top AS 3", prefix + "System3_Top_AS3.xml"));
        thisGroup.addItem(new TemplateItem("Transport unit 1", prefix + "System4_system4.xml"));
        //thisGroup.addItem(new TemplateItem("AIP System 4 - Top TU 1", prefix + "System4_Top_TU1.xml"));
        thisGroup.addItem(new TemplateItem("Transport unit 2", prefix + "System5_system5.xml"));
        //thisGroup.addItem(new TemplateItem("AIP System 5 - Top TU 2", prefix + "System5_Top_TU2.xml"));
        thisGroup.addItem(new TemplateItem("Transport unit 3", prefix + "System6_system6.xml"));
        thisGroup.addItem(new TemplateItem("Transport unit 4", prefix + "System7_system7.xml"));
        //thisGroup.addItem(new TemplateItem("AIP System 7 - Top TU 4", prefix + "System7_Top_TU4.xml"));
        thisGroup.addItem(new TemplateItem("\"Complete\" system (all the above)", prefix + "All.xml"));
        thisGroup.addItem(new TemplateItem("Largest coherent part", prefix + "LargestCoherentPart.xml"));
    }
    
    private void initializeCentralLockExamples()
    {
        TemplateGroup thisGroup = CentralLockExamples;
        
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/OtherExamples/centralLock/";
        
        // THREE DOORS
        //thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors", prefix + "verriegel3.xml"));
        thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors", basePrefix + "/ModuleExamples/central_locking/verriegel3.wmod"));
        //thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors - Uncontrollable", prefix + "verriegel3_uncontrollable.xml"));
        thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors - Language Inclusion", prefix + "verriegel3_language_inclusion.xml"));
        thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors - Language Exclusion", prefix + "verriegel3_language_exclusion.xml"));
        //thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors - Synchronized Plants", prefix + "verriegel3_joint.xml"));
        //thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors - Synchronized Plants Uncontrollable", prefix + "verriegel3_joint_uncontrollable.xml"));
        
        // FOUR DOORS
        //thisGroup.addItem(new TemplateItem("Central Lock - 4 Doors", prefix + "verriegel4.xml"));
        thisGroup.addItem(new TemplateItem("Central Lock - 4 Doors", basePrefix + "/ModuleExamples/central_locking/verriegel4.wmod"));
        thisGroup.addItem(new TemplateItem("Central Lock - 4 Doors - Language Inclusion", prefix + "verriegel4_language_inclusion.xml"));
        thisGroup.addItem(new TemplateItem("Central Lock - 4 Doors - Language Exclusion", prefix + "verriegel4_language_exclusion.xml"));
    }
    
    private void initializeOperatorSupervisorExamples()
    {
        TemplateGroup thisGroup = OperatorSupervisorExamples;
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/OperatorSupervisor/";
        
        thisGroup.addItem(new TemplateItem("Warehouse", prefix + "warehouse.xml"));
        thisGroup.addItem(new TemplateItem("Warehouse k=2", prefix + "warehouse_k2.xml"));
        thisGroup.addItem(new TemplateItem("Warehouse k=5", prefix + "warehouse_k5.xml"));
        thisGroup.addItem(new TemplateItem("Warehouse k=7", prefix + "warehouse_k7.xml"));
        thisGroup.addItem(new TemplateItem("Warehouse k=10", prefix + "warehouse_k10.xml"));
        thisGroup.addItem(new TemplateItem("Warehouse k=13", prefix + "warehouse_k13.xml"));
    }
    
    private void initializeCommunicationSystemExamples()
    {
        TemplateGroup thisGroup = CommunicationSystemExamples;
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/CommunicationSystemExamples/";
        
        thisGroup.addItem(new TemplateItem("Parrow's Protocol", prefix + "ParrowsProtocol.xml"));
        thisGroup.addItem(new TemplateItem("Alternating Bit Protocol", prefix + "AlternatingBitProtocol1.xml"));
        thisGroup.addItem(new TemplateItem("Alternating Bit Protocol (variant)", prefix + "AlternatingBitProtocol2.xml"));
        thisGroup.addItem(new TemplateItem("CSMA/CD Protocol", prefix + "CSMA_CD.xml"));
    }
    
    private void initializeManufacturingSystemExamples()
    {
        TemplateGroup thisGroup = ManufacturingSystemExamples;
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/ManufacturingExamples/";
        
        thisGroup.addItem(new TemplateItem("Automated Guided Vehicles", prefix + "agv.xml"));
        //thisGroup.addItem(new TemplateItem("Automated Guided Vehicle, immediate events", prefix + "agvImmediate.xml"));
        thisGroup.addItem(new TemplateItem("Circular Table", prefix + "circularTable.xml"));
        thisGroup.addItem(new TemplateItem("Circular Table (variant)", prefix + "rotationTable.xml"));
        thisGroup.addItem(new TemplateItem("Dosing Unit", prefix + "dosingUnit.xml"));
        thisGroup.addItem(new TemplateItem("Dosing Tank - EFA", prefix + "dosingtankEFA.wmod"));
        thisGroup.addItem(new TemplateItem("Flexible Manufacturing System", prefix + "FlexibleManufacturingSystem.xml"));
        thisGroup.addItem(new TemplateItem("Flexible Manufacturing System (variant, incomplete)", prefix + "FlexibleManufacturingSystemVariant.xml"));
        thisGroup.addItem(new TemplateItem("Flexible Production Cell", prefix + "ftechnik.wmod"));
        thisGroup.addItem(new TemplateItem("Flexible Production Cell (variant)", prefix + "FlexibleProductionCell.xml"));
        thisGroup.addItem(new TemplateItem("Intertwined Product Cycles", prefix + "IntertwinedProductCycles.xml"));
        thisGroup.addItem(new TemplateItem("Machine Buffer Machine", prefix + "machineBufferMachine.xml"));
        thisGroup.addItem(new TemplateItem("Parallel Manufacturing Example", prefix + "parallelManufacturingExample.xml"));
        thisGroup.addItem(new TemplateItem("Production Cell for Mounting Frames", prefix + "fzelle.wmod"));
        thisGroup.addItem(new TemplateItem("Robot Assembly Cell", prefix + "robotAssemblyCell.xml"));
        thisGroup.addItem(new TemplateItem("Simple Manufacturing Example", prefix + "simpleManufacturingExample.xml"));
        thisGroup.addItem(new TemplateItem("Tank Process", prefix + "tankProcess.xml"));
        thisGroup.addItem(new TemplateItem("Transfer Line", prefix + "transfer.xml"));
        thisGroup.addItem(new TemplateItem("Train Testbed", prefix + "tbed_ctct.xml"));
        thisGroup.addItem(new TemplateItem("Train Testbed (variant)", prefix + "tbed_valid.xml"));
        thisGroup.addItem(new TemplateItem("Welding Robots", prefix + "weldingRobots.xml"));
        //thisGroup.addItem(new TemplateItem("Volvo cell", prefix + "volvo.xml"));
        thisGroup.addItem(new TemplateItem("Volvo cell", prefix + "volvo_no_sup.xml"));
    }
    
    private void initializeModuleExamples()
    {
        TemplateGroup thisGroup = ModuleExamples;
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/ModuleExamples/";
        
        thisGroup.addItem(new TemplateItem("Big Factory", prefix + "big_factory/bfactory.wmod"));
        thisGroup.addItem(new TemplateItem("Car fh", prefix + "car_fh/car_fh.wmod"));
        thisGroup.addItem(new TemplateItem("Central Locking - Dreitueren", prefix + "central_locking/dreitueren.wmod"));
        thisGroup.addItem(new TemplateItem("Central Locking - ftuer", prefix + "central_locking/ftuer.wmod"));
        thisGroup.addItem(new TemplateItem("Central Locking - koordwsp", prefix + "central_locking/koordwsp.wmod"));
        //thisGroup.addItem(new TemplateItem("Central Locking - verriegel3", prefix + "central_locking/verriegel3.wmod");
        //thisGroup.addItem(new TemplateItem("Central Locking - verriegel4", prefix + "central_locking/verriegel4.wmod");
        //thisGroup.addItem(new TemplateItem("Fischertechnik", prefix + "fischertechnik/fischertechnik.wmod"));
        //thisGroup.addItem(new TemplateItem("Stick Picking Game", prefix + "other/stick_picking_game.wmod");
    }
    
    private void initializeGames()
    {
        TemplateGroup thisGroup = Games;
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/Games/";
        
        //thisGroup.addItem(new TemplateItem("Man, Wolf, Goat, Cabbage", basePrefix + "/CCSBookExercises/" + "Ex2_6.xml"));
        thisGroup.addItem(new TemplateItem("Man, Wolf, Goat, Cabbage", prefix + "ManWolfGoatCabbage.wmod"));
        thisGroup.addItem(new TemplateItem("Stick Picking Game", basePrefix + "/ModuleExamples/other/stick_picking_game.wmod"));
        thisGroup.addItem(new TemplateItem("Tic Tac Toe", basePrefix + "/ModuleExamples/other/tictactoe.wmod"));
        //thisGroup.addItem(new TemplateItem("Wine Merchant's Problem", prefix + "WineMerchant.xml"));
        thisGroup.addItem(new TemplateItem("Wine Merchant's Problem", basePrefix + "/ModuleExamples/other/winemerchant.wmod"));
    }

private void initializeSchedulingExamples()
    {
        TemplateGroup thisGroup = SchedulingExamples;
        
        allGroups.add(thisGroup);
        
        String prefix = basePrefix + "/Scheduling/";
        
        thisGroup.addItem(new TemplateItem("2 robots, 1 zone (from HBS)", prefix + "hbs_example.xml"));
        thisGroup.addItem(new TemplateItem("2 robots with alternatives, 1 zone (from PV35)", prefix + "pv35_example.xml"));
    }
    
    /*
      private void initializeStandardComponents()
      {
      TemplateGroup thisGroup = StandardComponents;
     
      allGroups.add(thisGroup);
      }
     */
    
    public synchronized static ExampleTemplates getInstance()
    {
        if (templates == null)
        {
            templates = new ExampleTemplates();
        }
        
        return templates;
    }
    
    public Iterator iterator()
    {
        return allGroups.iterator();
    }
}
