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
 * Haradsgatan 26A
 * 431 42 Molndal
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

public class ExampleTemplates
{
	private static ExampleTemplates templates = null;
	private final TemplateGroup CCSBookExamples = new TemplateGroup(TemplateTypes.CCSBookExample);
	private final TemplateGroup CCSBookExercises = new TemplateGroup(TemplateTypes.CCSBookExercise);
	private final TemplateGroup CCSCourseAssignments = new TemplateGroup(TemplateTypes.CCSCourseAssignment);
	private final TemplateGroup CCSCourseAssignmentSolutions = new TemplateGroup(TemplateTypes.CCSCourseAssignmentSolutions);
	private final TemplateGroup AIPExamples = new TemplateGroup(TemplateTypes.AIPExample);
	private final TemplateGroup CentralLockExamples = new TemplateGroup(TemplateTypes.CentralLockExample);
	private final TemplateGroup OperatorSupervisorExamples = new TemplateGroup(TemplateTypes.OperatorSupervisorExample);
	private final TemplateGroup OtherExamples = new TemplateGroup(TemplateTypes.OtherExample);
	//private final TemplateGroup StandardComponents = new TemplateGroup(TemplateTypes.StandardComponent);
	private static final String extraPrefix = "/includeInJarFile";
	private List allGroups = new LinkedList();

	private ExampleTemplates()
	{
		initialize();
	}

	private void initialize()
	{
		initializeCCSBookExamples();
		initializeCCSBookExercises();
		initializeCCSCourseAssignments();
		//initializeCCSCourseAssignmentSolutions();
		initializeCentralLockExamples();
		initializeAIPExamples();
		initializeOperatorSupervisorExamples();
		initializeOtherExamples();
		//initializeStandardComponents();
	}

	private void initializeCCSBookExamples()
	{
		TemplateGroup thisGroup = CCSBookExamples;
		allGroups.add(thisGroup);

		String prefix = extraPrefix + "/CCSBookExamples/";

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

	private void initializeCCSBookExercises()
	{
		TemplateGroup thisGroup = CCSBookExercises;
		allGroups.add(thisGroup);

		String prefix = extraPrefix + "/CCSBookExercises/";

		thisGroup.addItem(new TemplateItem("2.1 - PIN Code Reader", prefix + "Ex2_1.xml"));
		thisGroup.addItem(new TemplateItem("2.6 - Man, Wolf, Goat, Cabbage", prefix + "Ex2_6.xml"));
		thisGroup.addItem(new TemplateItem("4.1 - Man, Wolf, Goat, Cabbage Revisited", prefix + "Ex4_1.xml"));
		thisGroup.addItem(new TemplateItem("4.2 - AGV System", prefix + "Ex4_2.xml"));
		thisGroup.addItem(new TemplateItem("4.4 - Cat & Mouse", prefix + "Ex4_4.xml"));
		thisGroup.addItem(new TemplateItem("4.5 - Robot & Machine: All events controllable", prefix + "Ex4_5_all_con.xml"));
		thisGroup.addItem(new TemplateItem("4.5 - Robot & Machine: Put is uncontrollable", prefix + "Ex4_5_b_uncon.xml"));
	}

	private void initializeCCSCourseAssignments()
	{
		TemplateGroup thisGroup = CCSCourseAssignments;
		allGroups.add(thisGroup);

		String prefix = extraPrefix + "/CCSCourseAssignments/";

		thisGroup.addItem(new TemplateItem("FMS line", prefix + "FMSLine.xml"));
		thisGroup.addItem(new TemplateItem("Production system", prefix + "ProdSysStudent.xml"));
		thisGroup.addItem(new TemplateItem("Communication channel", prefix + "CommunicationChannel.xml"));
	}

	private void initializeCCSCourseAssignmentSolutions()
	{
		TemplateGroup thisGroup = CCSCourseAssignmentSolutions;
		allGroups.add(thisGroup);

		String prefix = extraPrefix + "/CCSCourseAssignments/solutions/";

		thisGroup.addItem(new TemplateItem("FMS without feedback", prefix + "FMSLine.xml"));
		thisGroup.addItem(new TemplateItem("FMS with feedback", prefix + "FMSLoop.xml"));
		thisGroup.addItem(new TemplateItem("Communication channel Specification", prefix + "comm_spec.xml"));
	}

	private void initializeOtherExamples()
	{
		TemplateGroup thisGroup = OtherExamples;
		allGroups.add(thisGroup);

		String prefix = extraPrefix + "/OtherExamples/";

		//thisGroup.addItem(new TemplateItem("Central locking system - 3 doors", prefix + "centralLocking3Doors.xml"));
		thisGroup.addItem(new TemplateItem("Flexible manufacturing system", prefix + "flexibleManufacturingSystem.xml"));
		thisGroup.addItem(new TemplateItem("Robot assembly cell", prefix + "robotAssemblyCell.xml"));
		thisGroup.addItem(new TemplateItem("Flexible manufacuring cell", prefix + "flexibleManufacturingCell.xml"));
		//thisGroup.addItem(new TemplateItem("Cat and mouse", prefix + "catmouse.xml")); // A variant of this one is included in the CCSBookExercises above
		thisGroup.addItem(new TemplateItem("Automated Guided Vehicles", prefix + "agv.xml"));
		//thisGroup.addItem(new TemplateItem("Automated Guided Vehicle, immediate events", prefix + "agvImmediate.xml"));
		thisGroup.addItem(new TemplateItem("Circular Table", prefix + "circularTable.xml"));
		thisGroup.addItem(new TemplateItem("Ball Process", prefix + "ballProcess.xml"));
		thisGroup.addItem(new TemplateItem("Ball Process - Gatekeeper", prefix + "ballProcessGatekeeper.xml"));
		thisGroup.addItem(new TemplateItem("Rotation Table", prefix + "rotationTable.xml"));
		thisGroup.addItem(new TemplateItem("Welding Robots", prefix + "weldingRobots.xml"));
		thisGroup.addItem(new TemplateItem("Simple Manufacturing Example", prefix + "simpleManufacturingExample.xml"));
		thisGroup.addItem(new TemplateItem("Parallel Manufacturing Example", prefix + "parallelManufacturingExample.xml"));
		thisGroup.addItem(new TemplateItem("Dosing Unit", prefix + "dosingUnit.xml"));
		thisGroup.addItem(new TemplateItem("Telecommunications Network", prefix + "telecommunicationsNetwork.xml"));
	}

	private void initializeAIPExamples()
	{
		TemplateGroup thisGroup = AIPExamples;
		allGroups.add(thisGroup);

		String prefix = extraPrefix + "/OtherExamples/";

		thisGroup.addItem(new TemplateItem("AIP System 1 - System 1", prefix + "aip/System1_system1.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 1 - Top AS 1", prefix + "aip/System1_Top_AS1.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 2 - System 2", prefix + "aip/System2_system2.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 2 - Top AS 2", prefix + "aip/System2_Top_AS2.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 3 - System 3", prefix + "aip/System3_system3.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 3 - Top AS 3", prefix + "aip/System3_Top_AS3.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 4 - System 4", prefix + "aip/System4_system4.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 4 - Top TU 1", prefix + "aip/System4_Top_TU1.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 5 - System 5", prefix + "aip/System5_system5.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 5 - Top TU 2", prefix + "aip/System5_Top_TU2.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 6 - System 6", prefix + "aip/System6_system6.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 7 - System 7", prefix + "aip/System7_system7.xml"));
		thisGroup.addItem(new TemplateItem("AIP System 7 - Top TU 4", prefix + "aip/System7_Top_TU4.xml"));
	}

	private void initializeCentralLockExamples()
	{
		TemplateGroup thisGroup = CentralLockExamples;
		allGroups.add(thisGroup);

		String prefix = extraPrefix + "/OtherExamples/";

		thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors", prefix + "centralLock/verriegel3.xml"));
		thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors - Uncontrollable", prefix + "centralLock/verriegel3_uncontrollable.xml"));
		thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors - Language Inclusion", prefix + "centralLock/verriegel3_language_inclusion.xml"));
		thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors - Language Exclusion", prefix + "centralLock/verriegel3_language_exclusion.xml"));
		thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors - Synchronized Plants", prefix + "centralLock/verriegel3_joint.xml"));
		thisGroup.addItem(new TemplateItem("Central Lock - 3 Doors - Synchronized Plants Uncontrollable", prefix + "centralLock/verriegel3_joint_uncontrollable.xml"));
		thisGroup.addItem(new TemplateItem("Central Lock - 4 Doors", prefix + "centralLock/verriegel4.xml"));
		thisGroup.addItem(new TemplateItem("Central Lock - 4 Doors - Language Inclusion", prefix + "centralLock/verriegel4_language_inclusion.xml"));
		thisGroup.addItem(new TemplateItem("Central Lock - 4 Doors - Language Exclusion", prefix + "centralLock/verriegel4_language_exclusion.xml"));
	}

	private void initializeOperatorSupervisorExamples()
	{
		TemplateGroup thisGroup = OperatorSupervisorExamples;
		allGroups.add(thisGroup);

		String prefix = extraPrefix + "/OperatorSupervisor/";

		thisGroup.addItem(new TemplateItem("Warehouse", prefix + "warehouse.xml"));
		thisGroup.addItem(new TemplateItem("Warehouse k=2", prefix + "warehouse_k2.xml"));
		thisGroup.addItem(new TemplateItem("Warehouse k=5", prefix + "warehouse_k5.xml"));
		thisGroup.addItem(new TemplateItem("Warehouse k=7", prefix + "warehouse_k7.xml"));
		thisGroup.addItem(new TemplateItem("Warehouse k=10", prefix + "warehouse_k10.xml"));
		thisGroup.addItem(new TemplateItem("Warehouse k=13", prefix + "warehouse_k13.xml"));
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
