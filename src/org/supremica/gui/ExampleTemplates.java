
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
	private final TemplateGroup OtherExamples = new TemplateGroup(TemplateTypes.OtherExample);
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
		initializeOtherExamples();
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

		thisGroup.addItem(new TemplateItem("4.5 - Robot & Machine", prefix + "Ex4_5.xml"));
	}

	private void initializeCCSCourseAssignments()
	{

		TemplateGroup thisGroup = CCSCourseAssignments;

		allGroups.add(thisGroup);

		String prefix = extraPrefix + "/CCSCourseAssignments/";

		thisGroup.addItem(new TemplateItem("Assignment 1 - Communication channel", prefix + "CommunicationChannel.xml"));
	}

	private void initializeOtherExamples()
	{

		TemplateGroup thisGroup = OtherExamples;

		allGroups.add(thisGroup);

		String prefix = extraPrefix + "/OtherExamples/";

		thisGroup.addItem(new TemplateItem("Central locking system - 3 doors", prefix + "centralLocking3Doors.xml"));
		thisGroup.addItem(new TemplateItem("Flexible manufacturing system", prefix + "flexibleManufacturingSystem.xml"));
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
