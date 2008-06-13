/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */

/*
 * Copyright (C) 2008 Markus Skoldstam
 * Copyright (C) 2005 Goran Cengic
*/
/**
 * @author Markus Sk&ouml;ldstam (skoldsta@chalmers.se)
 * @author Goran Cengic (cengic@chalmers.se)
 * @author Tord Alenljung (torda@chalmers.se)
 */

package org.supremica.automata;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.io.StringReader;

import net.sourceforge.fuber.model.interpreters.Finder;
import net.sourceforge.fuber.model.interpreters.efa.Lexer;
import net.sourceforge.fuber.model.interpreters.efa.Parser;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Goal;
import net.sourceforge.fuber.model.interpreters.abstractsyntax.Identifier;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.TypeMismatchException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.IntRange;
import groovy.lang.Script;
import groovy.util.GroovyCollections;

import static org.supremica.automata.VariableHelper.getLowerBound;
import static org.supremica.automata.VariableHelper.getUpperBound;

import org.supremica.log.*;

/**
 * This class is a "quick fix" in order to handle more complex actions than just x = 2 etc
 * Example: Let all vars be of type 0..1. Assume transition with guard 'x & !y'
 * and action 'z = x & s'. By expanding this transition we get one transition with
 * [guard]/action [x==1 & y==0 & s==1]/z=1 and one with [x==1 & y==0 & s==0]/z=0
 * The parser of Fuber is used to find the identifiers, and the guards and actions
 * are evaluated using GroovyShell. In future, Supremicas/Waters own
 * parser/interpreter should be used instead of Fuber + Groovy 
 */
public class ExtendedAutomataExpander
{

	private static ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
	private static ExpressionParser parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
    private static Logger log = LoggerFactory.createLogger(ExtendedAutomataExpander.class);

    static class GuardAndActionPair {
    	final String guard;
    	final String actions;
		public GuardAndActionPair(String guard, String actions) {
			super();
			this.guard = guard;
			this.actions = actions;
		}
		public String toString() { return "[" + guard + "]/" + actions; }
    }
	public static void expandTransitions(final ModuleSubject module)
	{           
		log.info("ExtendedAutomataExpander.expandTransitions(): Expanding transitions.");
		
		// get all component variables
		// and put them in a map
		log.debug("ExtendedAutomataExpander.expandTransitions(): Making module variables map.");
		final Map<String,VariableComponentProxy> moduleVariables =
            new HashMap<String,VariableComponentProxy>();
        for (final Proxy proxy : module.getComponentList()) {
            if (proxy instanceof VariableComponentProxy) {
                final VariableComponentProxy curVar =
                    (VariableComponentProxy) proxy;
				final String curVarName = curVar.getName();
				if (moduleVariables.keySet().contains(curVarName))
				{
					log.warn("ExtendedAutomataExpander.expandTransitions(): Warning!: The module contains duplicate variable definitions.");
					log.warn("Variable name: " + curVarName);
					throw new IllegalArgumentException("Module contains duplicate variables with name " + curVarName);
				}
                else
                {
                    moduleVariables.put(curVar.getName(), curVar);    
                }
            }
        }
		
		// expand transitions for all components in the module
        for (final Proxy proxy : module.getComponentList()) {
            if (!(proxy instanceof SimpleComponentProxy)) {
                continue;
            }
            final SimpleComponentProxy curComponent =
                (SimpleComponentProxy) proxy;
			String curComponentName = curComponent.getName();
			log.debug("ExtendedAutomataExpander.expandTransitions(): Component");
			log.debug(curComponentName);

			final ListSubject<EdgeSubject> edges =
                ((GraphSubject) curComponent.getGraph()).getEdgesModifiable();
			final List<EdgeSubject> removeEdges =
                new LinkedList<EdgeSubject>();
			final List<EdgeSubject> addEdges = new LinkedList<EdgeSubject>();

			for (EdgeSubject curEdge : edges)
			{
				NodeProxy source = curEdge.getSource();
				NodeProxy target = curEdge.getTarget();

				LabelBlockProxy curLabel = curEdge.getLabelBlock();

				GuardActionBlockProxy curBlock = curEdge.getGuardActionBlock();

				// do expansion only if guard and actions block is present
				if (curBlock != null)
				{
					Set<GuardAndActionPair> expandedGuardsAndActions = createExpandedGuardAndActions(moduleVariables, curBlock);
					for (GuardAndActionPair guardAndActions : expandedGuardsAndActions) {
						addEdges.add(makeTransition((NodeProxy) source, (NodeProxy) target, 
									(LabelBlockProxy) curLabel.clone(), guardAndActions.guard, guardAndActions.actions));
						log.debug(guardAndActions);
					}
					// mark current edge for removal
					removeEdges.add(curEdge);
				}
			}
  			// remove old edges
            edges.removeAll(removeEdges);
 			// add new edges
            edges.addAll(addEdges);
		}

		//writeModuleToFile(module ,"blah.wmod");

		log.info("ExtendedAutomataExpander.expandTransitions(): Done expanding transitions.");
	}
	
	private static Set<GuardAndActionPair> createExpandedGuardAndActions(final Map<String,VariableComponentProxy> moduleVariables, GuardActionBlockProxy guardActionBlock) {
		final Set<GuardAndActionPair> guardsAndActionsFromExpansion = new LinkedHashSet<GuardAndActionPair>();
		if (guardActionBlock.getGuards().size() > 0) {
			final String guardText = constructGuardText(guardActionBlock);
			log.debug(guardText);
			final Goal guardSyntaxTree = parse(guardText);
			final Set<String> guardIdentifiers = findExpressionIdentifiers(guardSyntaxTree);
			Script guardScript = new GroovyShell().parse(groovyfy(guardText, true));
			if (guardIdentifiers.size() > 0) {
				final List<List<Integer>> combinationsOfAllGuardVariableValues = getCombinationsOfAllVariableValues(moduleVariables, guardIdentifiers);
				log.debug("ExtendedAutomataExpander.expandTransitions(): Counting guard identifiers");
				for (final List<Integer> values : combinationsOfAllGuardVariableValues) {
					
					// evaluate the guard with interpreter
					final Binding guardBinding = createBinding(guardIdentifiers, values);
					guardScript.setBinding(guardBinding);
							
					if ((Boolean) guardScript.run()) {
						guardsAndActionsFromExpansion.addAll(createExpandedGuardAndActionsGivenGuardBinding(moduleVariables, guardIdentifiers, guardBinding, guardActionBlock));
					}
				}
			} else if ((Boolean) guardScript.run()) { // if no identifiers in guard and guard is true
				guardsAndActionsFromExpansion.addAll(createExpandedGuardAndActionsGivenGuardBinding(moduleVariables, new LinkedHashSet<String>(), new Binding(), guardActionBlock));
			}
		} else { // if no guard
			guardsAndActionsFromExpansion.addAll(createExpandedGuardAndActionsGivenGuardBinding(moduleVariables, new LinkedHashSet<String>(), new Binding(), guardActionBlock));
		}		
		return guardsAndActionsFromExpansion;
	}

	@SuppressWarnings("unchecked")
	private static Set<GuardAndActionPair> createExpandedGuardAndActionsGivenGuardBinding(final Map<String,VariableComponentProxy> moduleVariables, final Set<String> guardIdentifiers, final Binding guardBinding, final GuardActionBlockProxy guardActionBlock) {
		final Set<GuardAndActionPair> guardsAndActionsFromExpansion = new LinkedHashSet<GuardAndActionPair>();
		if (guardActionBlock.getActions().size() > 0) {
			final String actionsText = constructActionText(guardActionBlock);
			log.debug(actionsText);
			final Goal actionsSyntaxTree = parse(actionsText);
			
			// get actions identifiers
			final Set<String> actionsAssignmentIdents = findAssignmentIdentifiers(actionsSyntaxTree);
			final Set<String> actionsExpressionIdents = findExpressionIdentifiers(actionsSyntaxTree);
			final Set<String> actionsExpressionIdentsNotInGuard = new LinkedHashSet<String>(actionsExpressionIdents);
			actionsExpressionIdentsNotInGuard.removeAll(guardIdentifiers);
						
			Map<String,Script> actionScripts = new LinkedHashMap<String,Script>();
			List<BinaryExpressionProxy> actions = guardActionBlock.getActions();
			GroovyShell shell = new GroovyShell();
			int i = 0;
			for (String assignedIdentifier : actionsAssignmentIdents) {
				actionScripts.put(assignedIdentifier, shell.parse(groovyfy(actions.get(i++).toString(), false)));
			}
			
			if (actionsExpressionIdentsNotInGuard.size() > 0) {
				// add symbols for action identifiers
			// currently only integer variables
				final List<List<Integer>> combinationsOfAllActionExpressionVariableValues = getCombinationsOfAllVariableValues(
				        moduleVariables, actionsExpressionIdentsNotInGuard);

				// count up all identifiers and evaluate the actions
				log.debug("ExtendedAutomataExpander.expandTransitions(): Counting actions identifiers");

				for (final List<Integer> values : combinationsOfAllActionExpressionVariableValues) {
					final Binding guardActionBinding = createBinding(actionsExpressionIdentsNotInGuard, values);
					for (String identity : ((Map<String, ? extends Object>) guardBinding.getVariables()).keySet()) {
						guardActionBinding.setVariable(identity, guardBinding.getVariable(identity));
					}
					final Map<String, Integer> actionResult = evaluateActions(
							actionScripts, guardActionBinding);
					if (variableValuesWithinBounds(moduleVariables, actionResult)) {
						String actionString = createActionsString(actionResult);
						String guardString = constructGuardFromBinding(guardActionBinding);											
						guardsAndActionsFromExpansion.add(new GuardAndActionPair(guardString, actionString));
					}
				}
			} else { // No nonguard identifiers on right hand side (action expression)
				final Map<String, Integer> actionResult = evaluateActions(
						actionScripts, guardBinding);
				if (variableValuesWithinBounds(moduleVariables, actionResult)) {
					String actionString = createActionsString(actionResult);
					String guardString = constructGuardFromBinding(guardBinding);											
					guardsAndActionsFromExpansion.add(new GuardAndActionPair(guardString, actionString));
				}
			}
		} else { // No actions
			guardsAndActionsFromExpansion.add(new GuardAndActionPair(constructGuardFromBinding(guardBinding), ""));
		}
		return guardsAndActionsFromExpansion;	
	}

	
	private static String groovyfy(final String text, boolean isGuard) {
		// the &,^ and |-operators in groovy do only work with (int,int) or (bool,bool)
		// not (int,bool) or (bool,int). Thus 'a & !b', where a and b are (binary?) ints,
		// will fail. We therefore add support for (int,bool) and (bool,int) 
		// by adding methods to the Integer and Boolean classes at runtime (meta programming).
		StringBuffer sb = new StringBuffer()
		      .append("Boolean.metaClass.and = { Integer x -> delegate.and(x!=0) };")
		      .append("Boolean.metaClass.or = { Integer x -> delegate.or(x!=0) };")
		      .append("Boolean.metaClass.xor = { Integer x -> delegate.xor(x!=0) };")
		      .append("Integer.metaClass.and = { Boolean x -> x.and(delegate) };")
		      .append("Integer.metaClass.or = { Boolean x -> x.or(delegate) };")
		      .append("Integer.metaClass.xor = { Boolean x -> x.xor(delegate) };");
		//Make sure the guard returns boolean (nonzero int becomes true)
		if (isGuard) sb.append("(Boolean) (").append(text).append(")");
		else sb.append(text);
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Integer> evaluateActions(
			Map<String, Script> actionScripts,
			final Binding guardActionBindingOriginal) {
		final Map<String,Integer> actionResult = new LinkedHashMap<String, Integer>();
		for(Map.Entry<String,Script> identifierAssignmentScriptPair : actionScripts.entrySet()) {
			Binding guardActionBindingToBeModified = new Binding(new LinkedHashMap(guardActionBindingOriginal.getVariables()));
			identifierAssignmentScriptPair.getValue().setBinding(guardActionBindingToBeModified);
			identifierAssignmentScriptPair.getValue().run();
			Object value = guardActionBindingToBeModified.getVariable(identifierAssignmentScriptPair.getKey());
			if (value instanceof Boolean) actionResult.put(identifierAssignmentScriptPair.getKey(), ((Boolean) value) ? 1 : 0);
			else if (value instanceof Integer) actionResult.put(identifierAssignmentScriptPair.getKey(), (Integer) value);
			else throw new IllegalArgumentException("Result of assignment expression to" + identifierAssignmentScriptPair.getKey() + " was neither Boolean nor Integer");
		}
		return actionResult;
	}

	private static boolean variableValuesWithinBounds(final Map<String, VariableComponentProxy> moduleVariables, final Map<String, Integer> actionsResult) {
		for (String identifier : actionsResult.keySet()) {
			final int value = actionsResult.get(identifier);
			if (value < getLowerBound(moduleVariables.get(identifier)) || value > getUpperBound(moduleVariables.get(identifier))) {
				return false;
			}
		}
		return true;
	}
	private static String createActionsString(final Map<String, Integer> actionResult) {
		// make new actions
		String actions = "";
		for (String curIdent : actionResult.keySet()) {
			actions = actions + curIdent + " = " + actionResult.get(curIdent) + ";";
		}
		return actions;
	}
	
	@SuppressWarnings("unchecked")
	private static String constructGuardFromBinding(final Binding binding) {
		final List<String> comparisons = new ArrayList<String>();
		for (final String identifier : ((Map<String,? extends Object>) binding.getVariables()).keySet()) {
			comparisons.add(identifier + " == " + binding.getVariable(identifier));
		}
		return join(comparisons, " & ");
	}

	private static Binding createBinding(final Set<String> identifiers,
			final List<Integer> values) {
		int i = 0;
		Binding binding = new Binding();
		for (final String identifier : identifiers) {
			binding.setVariable(identifier, values.get(i++));
		}
		return binding;
	}

	@SuppressWarnings("unchecked")
	private static List<List<Integer>> getCombinationsOfAllVariableValues(
			final Map<String, VariableComponentProxy> moduleVariables,
			Set<String> guardIdentifiers) {
		final Collection<List<Integer>> domainOfDefinitionOfAllVariables = new ArrayList<List<Integer>>();
		for (String curIdent : guardIdentifiers) {
			final VariableComponentProxy variable = moduleVariables.get(curIdent);
			domainOfDefinitionOfAllVariables.add(new IntRange(getLowerBound(variable), getUpperBound(variable)));
		}
		return GroovyCollections.combinations(domainOfDefinitionOfAllVariables);
	}

	@SuppressWarnings("unchecked")
	private static Set<String> findExpressionIdentifiers(Goal syntaxTree) {
		Set<String> identifiers = new LinkedHashSet<String>();
		if (syntaxTree != null)
		{
			Finder finder = new Finder(syntaxTree);
			Set<Identifier> expressionIdents = finder.getExpressionIdentifiers();
			for (Identifier curIdent : expressionIdents) identifiers.add(curIdent.a);						
		}
		return identifiers;
	}

	@SuppressWarnings("unchecked")
	private static Set<String> findAssignmentIdentifiers(Goal syntaxTree) {
		Set<String> identifiers = new LinkedHashSet<String>();
		if (syntaxTree != null)
		{
			Finder finder = new Finder(syntaxTree);
			Set<Identifier> expressionIdents = finder.getAssignmentIdentifiers();
			for (Identifier curIdent : expressionIdents) identifiers.add(curIdent.a);						
		}
		return identifiers;
	}

	private static String constructActionText(GuardActionBlockProxy curBlock) {
		String actionsText = "";
		for (BinaryExpressionProxy curAction : curBlock.getActions()) actionsText = actionsText + curAction.toString() + "; ";
		return actionsText;
	}
	
	private static String constructGuardText(GuardActionBlockProxy curBlock) {
		 return "(" + join(curBlock.getGuards(), ") & (") + ")";
	}

	private static Goal parse(String text) {
		Goal syntaxTree = null;
		final Lexer lexer = new Lexer(new StringReader(text));
		final Parser parser = new Parser(lexer);
		try	{
			syntaxTree = (Goal) parser.parse().value; 
		} catch(Exception e) {
			log.error("ExtendedAutomataExpander.expandTransitions(): Couldn't parse " + text);
			throw new RuntimeException(e);
		}
		if (syntaxTree == null) throw new IllegalArgumentException("Failed parsing '" + text + "'. Returned syntax tree was null.");
		return syntaxTree;
	}
        
    public static void dontExpandTransitions(final ModuleSubject module)
    {
        log.info("ExtendedAutomataExpander.dontExpandTransitions(): Not expanding transitions.");

        // get all component variables
        // and put them in a map
        log.debug("ExtendedAutomataExpander.expandTransitions(): Making module variables map.");
        final Map<String,VariableComponentProxy> moduleVariables = new HashMap<String,VariableComponentProxy>();
        for (final Proxy proxy : module.getComponentList()) 
        {
            if (proxy instanceof VariableComponentProxy) 
            {
                final VariableComponentProxy curVar = (VariableComponentProxy) proxy;
                final String curVarName = curVar.getName();
                if (moduleVariables.keySet().contains(curVarName))
                {
                    log.warn("ExtendedAutomataExpander.expandTransitions(): Warning!: The module contains duplicate variable definitions.");
                    log.warn("Variable name: " + curVarName);
                }
                else
                {
                    moduleVariables.put(curVar.getName(), curVar);    
                }
            }
        }

        // expand transitions for all components in the module
        for (final Proxy proxy : module.getComponentList()) 
        {
            if (!(proxy instanceof SimpleComponentProxy)) 
            {
                continue;
            }
            final SimpleComponentProxy curComponent = (SimpleComponentProxy) proxy;
            String curComponentName = curComponent.getName();
            log.debug("ExtendedAutomataExpander.expandTransitions(): Component");
            log.debug(curComponentName);

            final ListSubject<EdgeSubject> edges = ((GraphSubject) curComponent.getGraph()).getEdgesModifiable();

            for (Iterator<EdgeSubject> edgeIter = edges.iterator(); edgeIter.hasNext();)
            {
                EdgeSubject curEdge = edgeIter.next();
                
                GuardActionBlockProxy curBlock = curEdge.getGuardActionBlock();

                // do expansion only if guard and actions block is present
                if (curBlock != null)
                {
                    log.debug("ExtendedAutomataExpander.expandTransitions(): Expanding edge ");

                    // get guards
                    List<SimpleExpressionProxy> curGuards = curBlock.getGuards();
                    String guardText = "(";
                    if (curGuards.size() > 0 )
                    {
                        log.debug("ExtendedAutomataExpander.expandTransitions(): Guard");
                        for (Iterator<SimpleExpressionProxy> iter = curGuards.iterator(); iter.hasNext();)
                        {
                            //guardText = guardText + ((SimpleExpressionProxy) iter.next()).getPlainText();
                            guardText = guardText + iter.next().toString();
                            if (iter.hasNext())
                            {
                                guardText = guardText + ") & (";
                            }
                            else
                            {
                                guardText = guardText + ")";
                            }
                        }
                        log.debug(guardText);
                    }

                    // get actions
                    List <BinaryExpressionProxy> curActions = curBlock.getActions();
                    String actionsText = "";
                    String origActionsText = "";
                    if (curActions.size() > 0 )
                    {
                        log.debug("ExtendedAutomataExpander.expandTransitions(): Actions");
                        for (Iterator<BinaryExpressionProxy> iter = curActions.iterator(); iter.hasNext();)
                        {
                            BinaryExpressionProxy curAction = iter.next();
                            actionsText = actionsText + curAction.toString() + "; ";
                            if (iter.hasNext())
                            {
                                origActionsText = origActionsText + curAction.toString() + "; ";
                            }
                            else
                            {
                                origActionsText = origActionsText + curAction.toString();
                            }
                        }
                        log.debug(actionsText);
                    }
                }
            }
        }
    }

	private static EdgeSubject makeTransition(NodeProxy from, NodeProxy to, LabelBlockProxy labelBlock, String guardIn, String actionIn)
	{
		// make GuardActionSubject
		// Get guard ...
		SimpleExpressionSubject guard = null;
		try
		{
			String guardText = guardIn;
			if (guardText != null && !guardText.trim().equals(""))
			{
				guard = (SimpleExpressionSubject) parser.parse(guardText, Operator.TYPE_BOOLEAN);
			}
		}
		catch (ParseException exception)
		{
			log.error("ExtendedAutomataExpander.makeTransition(): Syntax error in guard!");
			log.error("\t guard: " + guardIn);
			log.error("\t action: " + actionIn);
			throw new RuntimeException(exception);
		}
		// Get actions ...
		List<BinaryExpressionSubject> actions = null;
		String actionText = actionIn;
		if (actionText != null && !actionText.trim().equals(""))
		{
			String[] texts = actionIn.split(";");
			actions = new ArrayList<BinaryExpressionSubject>(texts.length);
			for (String text : texts)
			{
				if (text.length() > 0)
				{
					try
					{
						SimpleExpressionSubject action = (SimpleExpressionSubject) parser.parse(text);
						if (!(action instanceof BinaryExpressionSubject))
						{
							throw new TypeMismatchException(action, "ACTION");
						}
						BinaryExpressionSubject binaction = (BinaryExpressionSubject) action;
						actions.add(binaction);
					}
					catch (ParseException exception)
					{
						log.error("ExtendedAutomataExpander.makeTransition(): Syntax error in action!");
						log.error("\t guard: " + guardIn);
						log.error("\t action: " + actionIn);
						throw new RuntimeException(exception);
					}
					catch (TypeMismatchException exception)
					{
						log.error("ExtendedAutomataExpander.makeTransition(): Type mismatch error in action!");
						log.error("\t guard: " + guardIn);
						log.error("\t action: " + actionIn);
						throw new RuntimeException(exception);
					}
				}
			}
		}
			
		// Store parsed results ...
		GuardActionBlockSubject guardActionBlock = factory.createGuardActionBlockProxy();
		List<SimpleExpressionSubject> blockGuards = guardActionBlock.getGuardsModifiable();
		blockGuards.clear();
		if (guard != null)
		{
			blockGuards.add(guard);
		}
		List<BinaryExpressionSubject> blockActions = guardActionBlock.getActionsModifiable();
		blockActions.clear();
		if (actions != null)
		{
			blockActions.addAll(actions);
		}
			
		EdgeSubject newEdge = factory.createEdgeProxy(from, to, labelBlock, guardActionBlock, null, null, null);

		return newEdge;
	}

	protected static <T> String join(Collection<T> collection, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator<T> iter = collection.iterator();
        if (iter.hasNext()) {
            buffer.append(iter.next());
            while (iter.hasNext()) {
                buffer.append(delimiter);
                buffer.append(iter.next());
            }
        }
        return buffer.toString();
    }
}
                