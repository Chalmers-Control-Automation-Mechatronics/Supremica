
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
	private final TemplateGroup OtherExamples = new TemplateGroup(TemplateTypes.OtherExample);
	private final TemplateGroup AIPExamples = new TemplateGroup(TemplateTypes.AIPExample);
	private final TemplateGroup OperatorSupervisorExamples = new TemplateGroup(TemplateTypes.OperatorSupervisorExample);
	private final TemplateGroup StandardComponents = new TemplateGroup(TemplateTypes.StandardComponent);
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
		initializeCCSCourseAssignmentSolutions();
		initializeOtherExamples();
		initializeAIPExamples();
		initializeOperatorSupervisorExamples();
		initializeStandardComponents();
	}

	private void initializeCCSBookExamples()
	{
		TemplateGroup thisGroup = CCSBookExamples;

		allGroups.add(thisGroup);
	}

	private void initializeCCSBookExercises()
	{
		TemplateGroup thisGroup = CCSBookExercises;

		allGroups.add(thisGroup);

		String prefix = extraPrefix + "/CCSBookExercises/";

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

		thisGroup.addItem(new TemplateItem("Central locking system - 3 doors", prefix + "centralLocking3Doors.xml"));
		thisGroup.addItem(new TemplateItem("Flexible manufacturing system", prefix + "flexibleManufacturingSystem.xml"));
		thisGroup.addItem(new TemplateItem("Robot assembly cell", prefix + "robotAssemblyCell.xml"));
		thisGroup.addItem(new TemplateItem("Flexible manufacuring cell", prefix + "flexibleManufacturingCell.xml"));
		thisGroup.addItem(new TemplateItem("Cat and mouse", prefix + "catmouse.xml"));
		thisGroup.addItem(new TemplateItem("Automated Guided Vehicle", prefix + "agv.xml"));
		thisGroup.addItem(new TemplateItem("Circular Table", prefix + "circularTable.xml"));
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


	private void initializeStandardComponents()
	{
		TemplateGroup thisGroup = StandardComponents;

		allGroups.add(thisGroup);
	}

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
