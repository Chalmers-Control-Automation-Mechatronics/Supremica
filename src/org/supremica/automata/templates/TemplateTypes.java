
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
package org.supremica.automata.templates;

import java.util.*;

public class TemplateTypes
{
	private static List collection = new LinkedList();
	public static final TemplateTypes Undefined = new TemplateTypes("Undefined");
	public static final TemplateTypes DESBookExample = new TemplateTypes("DES-Book Examples");
	public static final TemplateTypes DESBookExercise = new TemplateTypes("DES-Book Exercises");
	public static final TemplateTypes DESCourseAssignment = new TemplateTypes("DES-Course Assignments");
	public static final TemplateTypes DESCourseAssignmentSolutions = new TemplateTypes("DES-Course Assignment Solutions");
	public static final TemplateTypes OtherExample = new TemplateTypes("Other Examples");
	public static final TemplateTypes AIPExample = new TemplateTypes("AIP Examples");
	public static final TemplateTypes CentralLockExample = new TemplateTypes("Central Lock Examples");
	public static final TemplateTypes OperatorSupervisorExample = new TemplateTypes("Hybrid Human-Computer Supervisors");
	public static final TemplateTypes CommunicationSystemExamples = new TemplateTypes("Communication System Examples");
	public static final TemplateTypes ManufacturingSystemExamples = new TemplateTypes("Manufacturing System Examples");
	public static final TemplateTypes Games = new TemplateTypes("Games and Puzzles");
	public static final TemplateTypes ModuleExamples = new TemplateTypes("Module Examples");

	// public static final TemplateTypes StandardComponent = new TemplateTypes("Standard Components");
	private String description;

	private TemplateTypes(String description)
	{
		this.description = description;

		collection.add(this);
	}

	public static Iterator iterator()
	{
		return collection.iterator();
	}

	public String getDescription()
	{
		return description;
	}
}
