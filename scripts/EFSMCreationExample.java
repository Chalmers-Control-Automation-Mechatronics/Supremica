//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

// package net.sourceforge.waters.samples.plain;
// Compile:
// %PORTABLE%\jdk-12.0.2\bin\javac -cp dist\Supremica.jar EFSMCreationExample.java
// Run:
// %PORTABLE%\jdk-12.0.2\bin\java -cp Z:\Lua;dist\Supremica.jar EFSMCreationExample
package Lupremica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.MarshallingTools;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.properties.Config;

import org.xml.sax.SAXException;


/**
 * <P>An example to demonstrate the use of the WATERS API to create a
 * module with an EFSM component.</P>
 *
 * <P>This code creates a module ({@link ModuleProxy}) with two events and
 * the <CODE>:accepting</CODE> proposition, an integer and an enumeration
 * variable, and an EFSM plant with a guard/action block on one edge and
 * a conditional on another edge.</P>
 *
 * <P>When finished creating the module, it is saved as
 * <CODE>EFSMCreationExample.wmod</CODE>. Then the module is compiled to a
 * product DES ({@link ProductDESProxy}) and the result is saved as
 * <CODE>EFSMCreationExample.wdes</CODE>. Both output files are in the
 * current working directory, which is the root of the Supremica
 * repository when launched from Eclipse.</P>
 *
 * @author Robi Malik
 */
/**
 * Running this as a script within Supremica works,
 * It saves a wmod and a wdes, plus opens a module in Supremica
 */
public class EFSMCreationExample
{
	static ModuleSubject createEFSMmodule()
	{
		// We need a module factory and an operator table
		final ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
		final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();

		System.out.println("First create three events ...");
		final List<EventDeclProxy> events = new ArrayList<>(3);
		// Controllable event c
		final IdentifierProxy eventNameC =
		  factory.createSimpleIdentifierProxy("c");
		final EventDeclProxy eventC =
		  factory.createEventDeclProxy(eventNameC, EventKind.CONTROLLABLE);
		events.add(eventC);
		// Uncontrollable event u
		final IdentifierProxy eventNameU =
		  factory.createSimpleIdentifierProxy("u");
		final EventDeclProxy eventU =
		  factory.createEventDeclProxy(eventNameU, EventKind.UNCONTROLLABLE);
		events.add(eventU);
		//Proposition :accepting (used for marking)
		final IdentifierProxy eventNameAccepting =
		  factory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
		final EventDeclProxy eventAccepting =
		  factory.createEventDeclProxy(eventNameAccepting, EventKind.PROPOSITION);
		events.add(eventAccepting);

		System.out.println("Next, two variables and an EFSM ...");
		final List<ComponentProxy> components = new ArrayList<>(3);

		// Integer variable x with range 0..10, initially 0
		final IdentifierProxy varNameX = factory.createSimpleIdentifierProxy("x");
		final IntConstantProxy varMinX = factory.createIntConstantProxy(0);
		final IntConstantProxy varMaxX = factory.createIntConstantProxy(10);
		final SimpleExpressionProxy varRangeX =
		  factory.createBinaryExpressionProxy(optable.getRangeOperator(),
											  varMinX, varMaxX);
		final IdentifierProxy varRefX = factory.createSimpleIdentifierProxy("x");
		final IntConstantProxy varInitValueX = factory.createIntConstantProxy(0);
		final SimpleExpressionProxy varInitPredX =
		  factory.createBinaryExpressionProxy(optable.getEqualsOperator(),
											  varRefX, varInitValueX);
		final VariableComponentProxy varX =
		  factory.createVariableComponentProxy(varNameX, varRangeX, varInitPredX);
		components.add(varX);

		// Enumeration variable y with range [a, b, c], initially a
		final IdentifierProxy varNameY = factory.createSimpleIdentifierProxy("y");
		final List<SimpleIdentifierProxy> enumMembers = new ArrayList<>(3);
		final SimpleIdentifierProxy enumMemberA =
		  factory.createSimpleIdentifierProxy("a");
		enumMembers.add(enumMemberA);
		final SimpleIdentifierProxy enumMemberB =
		  factory.createSimpleIdentifierProxy("b");
		enumMembers.add(enumMemberB);
		final SimpleIdentifierProxy enumMemberC =
		  factory.createSimpleIdentifierProxy("c");
		enumMembers.add(enumMemberC);
		final SimpleExpressionProxy varRangeY =
		  factory.createEnumSetExpressionProxy(enumMembers);
		final IdentifierProxy varRefY = factory.createSimpleIdentifierProxy("y");
		final SimpleIdentifierProxy varInitValueY =
		  factory.createSimpleIdentifierProxy("a");
		final SimpleExpressionProxy varInitPredY =
		  factory.createBinaryExpressionProxy(optable.getEqualsOperator(),
											  varRefY, varInitValueY);
		final VariableComponentProxy varY =
		  factory.createVariableComponentProxy(varNameY, varRangeY, varInitPredY);
		components.add(varY);

		// Next, create two states for our EFSM ...
		final List<NodeProxy> states = new ArrayList<>(2);
		// State s0, initial and not marked
		final NodeProxy state0 =
		  factory.createSimpleNodeProxy("s0", null, null, true, null, null, null);
		states.add(state0);
		// State s1, not initial and marked
		final IdentifierProxy nodeLabelAccepting =
		  factory.createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
		final List<IdentifierProxy> nodeLabelList =
		  Collections.singletonList(nodeLabelAccepting);
		final PlainEventListProxy nodeLabelExpression =
		  factory.createPlainEventListProxy(nodeLabelList);
		final NodeProxy state1 =
		  factory.createSimpleNodeProxy("s1", nodeLabelExpression, null, false,
										null, null, null);
		states.add(state1);

		// Next, create two edges for our EFSM ...
		final List<EdgeProxy> edges = new ArrayList<>(2);
		// edge1 ...
		// ... with guard x > 2 && y != a
		final IdentifierProxy guardRefX = factory.createSimpleIdentifierProxy("x");
		final IntConstantProxy guardValue2 = factory.createIntConstantProxy(2);
		final SimpleExpressionProxy guardPart1 =
		  factory.createBinaryExpressionProxy(optable.getGreaterThanOperator(),
											  guardRefX, guardValue2);
		final IdentifierProxy guardRefY = factory.createSimpleIdentifierProxy("y");
		final IdentifierProxy guardValueA = factory.createSimpleIdentifierProxy("a");
		final SimpleExpressionProxy guardPart2 =
		  factory.createBinaryExpressionProxy(optable.getNotEqualsOperator(),
											  guardRefY, guardValueA);
		final SimpleExpressionProxy guard1 =
		  factory.createBinaryExpressionProxy(optable.getAndOperator(),
											  guardPart1, guardPart2);
		final List<SimpleExpressionProxy> guards1 =
		  Collections.singletonList(guard1);
		// ... with actions x += 1 and y = c
		final List<BinaryExpressionProxy> actions1 = new ArrayList<>(2);
		final IdentifierProxy acionRefX = factory.createSimpleIdentifierProxy("x");
		final IntConstantProxy actionValue1 = factory.createIntConstantProxy(1);
		final BinaryExpressionProxy actionX =
		  factory.createBinaryExpressionProxy(optable.getIncrementOperator(),
											  acionRefX, actionValue1);
		actions1.add(actionX);
		final IdentifierProxy actionRefY = factory.createSimpleIdentifierProxy("y");
		final IdentifierProxy actionValueC = factory.createSimpleIdentifierProxy("c");
		final BinaryExpressionProxy actionY =
		  factory.createBinaryExpressionProxy(optable.getAssignmentOperator(),
											  actionRefY, actionValueC);
		actions1.add(actionY);
		final GuardActionBlockProxy gaBlock =
		  factory.createGuardActionBlockProxy(guards1, actions1, null);
		// ... with two events c and u
		final List<Proxy> labels1 = new ArrayList<>(2);
		final IdentifierProxy edge1LabelC = factory.createSimpleIdentifierProxy("c");
		labels1.add(edge1LabelC);
		final IdentifierProxy edge1LabelU = factory.createSimpleIdentifierProxy("u");
		labels1.add(edge1LabelU);
		final LabelBlockProxy labelBlock1 =
		  factory.createLabelBlockProxy(labels1, null);
		// ... from state s0 to state s1
		final EdgeProxy edge1 =
		  factory.createEdgeProxy(state0, state1, labelBlock1, gaBlock,
								  null, null, null);
		edges.add(edge1);

		// edge2 ... (alternative, use conditional instead of guard/action block)
		// ... with guard x > 2
		final IdentifierProxy condRefX = factory.createSimpleIdentifierProxy("x");
		final IntConstantProxy condValue2 = factory.createIntConstantProxy(2);
		final SimpleExpressionProxy condPart1 =
		  factory.createBinaryExpressionProxy(optable.getGreaterThanOperator(),
											  condRefX, condValue2);
		// ... with action y = c
		final IdentifierProxy condRefY = factory.createSimpleIdentifierProxy("y");
		final IdentifierProxy condValueC = factory.createSimpleIdentifierProxy("c");
		final BinaryExpressionProxy condPart2 =
		  factory.createBinaryExpressionProxy(optable.getAssignmentOperator(),
											  condRefY, condValueC);
		final SimpleExpressionProxy guard2 =
		  factory.createBinaryExpressionProxy(optable.getAndOperator(),
											  condPart1, condPart2);
		// ... with event c
		final IdentifierProxy edge2LabelC = factory.createSimpleIdentifierProxy("c");
		final List<Proxy> condLabels2 = Collections.singletonList(edge2LabelC);
		final ConditionalProxy cond =
		  factory.createConditionalProxy(condLabels2, guard2);
		final List<Proxy> labels2 = Collections.singletonList(cond);
		final LabelBlockProxy labelBlock2 =
		  factory.createLabelBlockProxy(labels2, null);
		// ... from state s1 to state s0
	   final EdgeProxy edge2 =
		  factory.createEdgeProxy(state1, state0, labelBlock2, null,
								  null, null, null);
		edges.add(edge2);

		// Now we can create the EFSM
		final GraphProxy graph =
		  factory.createGraphProxy(true, null, states, edges);
		final IdentifierProxy efsmName =
		  factory.createSimpleIdentifierProxy("efsm");
		final SimpleComponentProxy efsm =
		  factory.createSimpleComponentProxy(efsmName, ComponentKind.PLANT, graph);
		components.add(efsm);

		// Combine events, variables, and EFSM to make module
		final String moduleName =
		  ProxyTools.getShortClassName(EFSMCreationExample.class);
		//final ModuleProxy module =
		final ModuleSubject module =
		  factory.createModuleProxy(moduleName,
									"Automatically created demo module.", null,
									null, events, null, components);

		System.out.println("Successfully created module: " + module.getName());
		return module;
	}

	static void saveModuleAsWMOD(final ModuleSubject module)
	{
		final String savePath = Config.FILE_SAVE_PATH.getValue().toString();
		final String wmod = savePath + module.getName() + ".wmod";
		System.out.println("Save module to " + wmod);
		MarshallingTools.saveModule(module, wmod);
	}

	// Compile the module to a ProductDESProxy ...
	static void compileAndSaveWDES(final ModuleSubject module)
	{
		try
		{
	  	  final ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
		  final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
		  // Create a compiler
		  final DocumentManager manager = new DocumentManager();
		  final ProxyUnmarshaller<ModuleProxy> unmarshaller = new SAXModuleMarshaller(factory, optable);
		  manager.registerUnmarshaller(unmarshaller);
		  final ProductDESProxyFactory desFactory = ProductDESElementFactory.getInstance();
		  final ModuleCompiler compiler = new ModuleCompiler(manager, desFactory, module);
		  // Configure the compiler
		  // ... optimisation removes selfloops and redundant components
		  compiler.setOptimizationEnabled(true);
		  // ... normalisation is needed for this module with advanced features
		  compiler.setNormalizationEnabled(true);
		  // ... automaton variables are not used here, this could be turned off
		  compiler.setAutomatonVariablesEnabled(true);
		  // ... only report the first error even if there are several
		  compiler.setMultiExceptionsEnabled(false);
		  // Now compile the module
		  final ProductDESProxy des = compiler.compile();

		  // Save output to EFSMCreationExample.wdes
		  final String savePath = Config.FILE_SAVE_PATH.getValue().toString();
		  MarshallingTools.saveProductDES(des, savePath + module.getName() + ".wdes");
		  System.out.println("Successfully compiled and saved module.");
		}
		catch (SAXException | ParserConfigurationException exception)
		{
		  System.err.println("FATAL: Failed to configure unmarshaller.");
		}
		catch (final EvalException exception)
		{
		  System.err.println("Error compiling module: " + exception.getMessage());
		}
	}

  	public EFSMCreationExample(org.supremica.gui.ide.IDE ide) // called by RunScript
  	{
		System.out.println("EFSMCreationExample.constructing!");
  		final ModuleSubject module = EFSMCreationExample.createEFSMmodule();
  		// EFSMCreationExample.saveModuleAsWMOD(module);
		// EFSMCreationExample.compileAndSaveWDES(module);
  		final DocumentContainerManager manager = ide.getDocumentContainerManager();
  		final ModuleContainer container = new ModuleContainer(ide, module);
		manager.addContainer(container);
	}
}
