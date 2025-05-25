package org.supremica.automata.BDD.EFA;

import gnu.trove.list.array.TIntArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.sf.javabdd.BDD;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.Guard.BDDExtendedGuardGenerator;
import org.supremica.automata.algorithms.Guard.GeneticMinimizer.Chromosome;
import org.supremica.util.ActionTimer;

/**
 * @author Zhennan Fei
 * @author Sajed Miremadi
 */

public class BDDExtendedSynthesizer
{
  //#########################################################################
  //# Data members
  private static Logger logger =
    LogManager.getLogger(BDDExtendedSynthesizer.class);

  BDDExtendedAutomata bddAutomata;
  private final ExtendedAutomata theAutomata;
  private final ModuleSubjectFactory factory;
  private final ExpressionParser parser;
  private final Set<ExtendedAutomaton> autTobeDeclaredAsVars;

  private ActionTimer synthesisTimer;
  private BDD statesAfterSynthesis;

  private ActionTimer guardTimer;
  private HashMap<String,BDDExtendedGuardGenerator> event2GuardGen;
  private long nbrOfStates = -1;

  //#########################################################################
  //# Constructor
  public BDDExtendedSynthesizer(final ExtendedAutomata theAutomata,
                                final EditorSynthesizerOptions options)
  {
    this.theAutomata = theAutomata;
    // create an instance of BDDExtendedAutomata
    this.bddAutomata = new BDDExtendedAutomata(theAutomata, options);
    this.factory = ModuleSubjectFactory.getInstance();
    this.parser =
      new ExpressionParser(factory, CompilerOperatorTable.getInstance());
    this.autTobeDeclaredAsVars = new HashSet<ExtendedAutomaton>();
  }

  public void synthesize(final EditorSynthesizerOptions options)
  {
    synthesisTimer = new ActionTimer();

    if (options.getSynthesisType().equals(SynthesisType.CONTROLLABLE)) {
      synthesisTimer.start();
      statesAfterSynthesis =
        bddAutomata.getControllableStates(options.getReachability());
      nbrOfStates = bddAutomata.nbrOfControllableStates;
      synthesisTimer.stop();
    } else if (options.getSynthesisType().equals(SynthesisType.NONBLOCKING)) {
      synthesisTimer.start();
      statesAfterSynthesis = bddAutomata.getNonblockingStates();
      nbrOfStates = bddAutomata.nbrOfNonblockingStates;
      synthesisTimer.stop();
    } else if (options.getSynthesisType()
      .equals(SynthesisType.NONBLOCKING_CONTROLLABLE)) {
      synthesisTimer.start();
      final boolean reachable = options.getReachability();
      statesAfterSynthesis =
        bddAutomata.getNonblockingControllableStates(reachable);
      nbrOfStates = bddAutomata.nbrOfNonblockingControllableStates;
      synthesisTimer.stop();
    }
    // Applicable to RAS models (choose include RAS support in configuration)
    else if (options.getSynthesisType().equals(SynthesisType.UNSAFETY)) {
      synthesisTimer.start();
      statesAfterSynthesis = bddAutomata.getUnsafeStates();
      nbrOfStates = bddAutomata.nbrOfBoundaryUnsafeStates;
      synthesisTimer.stop();
    }
  }

  //#########################################################################
  //# Simple access
  public ExtendedAutomata getAutomata() {
    return theAutomata;
  }

  public BDDExtendedAutomata getBDDAutomata() {
    return bddAutomata;
  }

  public long nbrOfStates()
  {
    return nbrOfStates;
  }

  public int peakBDDNodes()
  {
    return bddAutomata.getMaxNbrNodes();
  }

  public BDD getResult()
  {
    return statesAfterSynthesis;
  }

  public ActionTimer getSynthesisTimer()
  {
    return synthesisTimer;
  }

  public ActionTimer getGuardTimer()
  {
    return guardTimer;
  }

  public HashMap<String,BDDExtendedGuardGenerator> getEvent2GuardGen()
  {
    return event2GuardGen;
  }

  //#########################################################################
  //# Methods handling guard generation
  public void generateGuard(final Vector<String> eventNames,
                            final EditorSynthesizerOptions options)
  {
    // make a copy of eventNames first
    final List<String> cpyEventNames = new LinkedList<String>(eventNames);

    // Users choose a single event for which guard should be generated
    if (!options.getEvent().equals("")) {
      cpyEventNames.clear();
      cpyEventNames.add(options.getEvent());
    }

    // BDDExtendedGuardGenerator bddgg = null;

    event2GuardGen = new HashMap<String, BDDExtendedGuardGenerator>();

    guardTimer = new ActionTimer();

    final Iterator<String> it = cpyEventNames.iterator();
    guardTimer.start();
    while (it.hasNext())
    {
      final String sigmaName = it.next();
      final HashMap<EdgeProxy, BDD> edgeToBDDMap =
      					bddAutomata.getEventName2EdgeBDDMap().get(sigmaName);

      final BDDExtendedGuardGenerator bddgg = new BDDExtendedGuardGenerator(bddAutomata,
                                            sigmaName,
                                            edgeToBDDMap,
                                            statesAfterSynthesis,
                                            options);
      if(!bddgg.getEdge2GuardMap().isEmpty())
        event2GuardGen.put(sigmaName, bddgg);
    }
    guardTimer.stop();
  }

  /* Add generated guards into the model. */
  public void addGuardsToAutomata()
  {
    for (final Map.Entry<String,BDDExtendedGuardGenerator> entry: event2GuardGen.entrySet())
    {
      final String eventName = entry.getKey();
      final BDDExtendedGuardGenerator currBDDGG = entry.getValue();
      final HashMap<EdgeProxy,String> edgesGuards =
        currBDDGG.getEdge2GuardMap();
      for (final EdgeProxy edge: edgesGuards.keySet()) {
        final EdgeSubject edgeSub = (EdgeSubject) edge;
        final String generatedGuard = edgesGuards.get(edgeSub);
        EdgeSubject manipulatedEdge = null;
        // get the event list from the edge subject
        final ListSubject<AbstractSubject> eventList =
          edgeSub.getLabelBlock().getEventIdentifierListModifiable();
        if (eventList.size() == 1) {
          manipulatedEdge = edgeSub;
        } else {
          // remove this event (as abstract subject) from the event list
          AbstractSubject thisEvent = null;
          for (final AbstractSubject ab: eventList) {
            if (ab.toString().equals(eventName)) {
              thisEvent = ab;
              break;
            }
          }
          eventList.remove(thisEvent);
          // clone this event and guard/action (if any)
          final LabelBlockSubject lbs = new LabelBlockSubject();
          lbs.getEventIdentifierListModifiable().add(thisEvent.clone());

          GuardActionBlockSubject clonedGuardActionBlock = null;
          if (edgeSub.getGuardActionBlock() != null)
            clonedGuardActionBlock = edgeSub.getGuardActionBlock().clone();
          // create manipulated edge
          manipulatedEdge = new EdgeSubject(edgeSub.getSource(),
                                            edgeSub.getTarget(),
                                            lbs,
                                            clonedGuardActionBlock,
                                            null, null, null);
          // add the manipulated edge to the graph which the edge belongs to.
          final GraphSubject graph =
            bddAutomata.getEdge2ExAutomatonMap().get(edgeSub).getGraph();
          graph.getEdgesModifiable().add(manipulatedEdge);
        }
        // add guard to the manipulated edge
        if (manipulatedEdge.getGuardActionBlock() == null) {
          final GuardActionBlockSubject block =
            new GuardActionBlockSubject();
          manipulatedEdge.setGuardActionBlock(block);
        }
        final ListSubject<SimpleExpressionSubject> guardBlock =
          manipulatedEdge.getGuardActionBlock().getGuardsModifiable();
        // create simple expression subject from existing guards and guard
        SimpleExpressionProxy sep = null;
        try {
          if (guardBlock.isEmpty())
            sep = parser.parse(generatedGuard, Operator.TYPE_BOOLEAN);
          else {
            final String guard = guardBlock.remove(0).toString();
            sep = parser.parse(String.format("(%s) & (%s)",
                                              guard, generatedGuard));
          }
        } catch (final ParseException pe) {
          logger.error(pe);
          logger.error("Some of the guards could not be parsed and attached "
                       + "to the automata. It is likely that there exist "
                       + "some 'strange' characters in some variables or"
                       + "values!");
          break;
        }
        guardBlock.add((SimpleExpressionSubject) sep);
      }
      autTobeDeclaredAsVars.addAll(currBDDGG.getAutGuardVars());
    }
    createAutVarsAndUpdates();
  }


	private void createAutVarsAndUpdates()
	{
		if(!net.sourceforge.waters.model.compiler.CompilerOptions.AUTOMATON_VARIABLES_COMPILER.getValue())
		{
			logger.warn("Automaton Variable Compiler is not engaged, falling back on \"fake\" automaton variables");
			useFakeAutomatonVariables();
		}
	}
	/**
	 * Using real automaton variables means that there is no need to:
	 * * generate new variables, just use the automaton name as is;
	 * * generate assignments, this is handled behind-the-scenes;
	 **/
	/**
	 * Use pre-v2.5 "fake" automaton variables
	 **/
	private void useFakeAutomatonVariables()
	{
		for (final ExtendedAutomaton aut : this.autTobeDeclaredAsVars)
		{
		  final Set<String> markedValues = new HashSet<>();
		  for (final NodeProxy node : aut.getMarkedLocations())
			markedValues.add(node.getName());

		  final String autVarName =
			aut.getName() + ExtendedAutomata.getLocVarSuffix();
		  // Add automaton variables to extended automata
		  final ExtendedAutomata exAutomata = aut.getExAutomata();
		  exAutomata.addEnumerationVariable(autVarName,
											aut.getNameToLocationMap().keySet(),
											aut.getInitialLocation().getName(),
											markedValues);
		  // add an action in the form of "A_curr = location"
		  final GraphSubject graph = aut.getComponent().getGraph();
		  for (final EdgeSubject edge : graph.getEdgesModifiable())
		  {
			final String sourceState = edge.getSource().getName();
			final String targetState = edge.getTarget().getName();
			// No need to add assignment to self-loop
			if (sourceState.equals(targetState))
			  continue;
			if (edge.getGuardActionBlock() == null)
			  edge.setGuardActionBlock(new GuardActionBlockSubject());
			final String assignment = autVarName + " = " + targetState;
			SimpleExpressionProxy assignmentAsAction = null;
			try {
			  assignmentAsAction = parser.parse(assignment);
			} catch (final ParseException exception) {
			  exception.printStackTrace();
			}
			edge.getGuardActionBlock().getActionsModifiable()
			  .add((BinaryExpressionSubject) assignmentAsAction);
		  }
		}
  }

  //#########################################################################
  //# Cleanup
  public void done()
  {
    if (bddAutomata != null) {
      bddAutomata.done();
      bddAutomata = null;
    }
  }

  //#########################################################################
  //# Others
  // this method is never used.
  public int[] decodeToIntArray(final Chromosome chromosome)
  {
    final TIntArrayList intArray =
      new TIntArrayList(bddAutomata.getEventDomain().vars());
    for (final String gene : chromosome.getGenes()) {
      final Integer geneIndex =
        bddAutomata.getIndexMap().getVariableIndexByName(gene);

      if (bddAutomata.getSourceLocationDomain(gene) != null) {
        intArray.add(bddAutomata.getTempLocationDomain(gene).vars());
        intArray.add(bddAutomata.getSourceLocationDomain(gene).vars());
        intArray.add(bddAutomata.getDestLocationDomain(gene).vars());
      } else if (bddAutomata.getSourceVariableDomain(geneIndex) != null) {
        intArray.add(bddAutomata.getTempVariableDomain(geneIndex).vars());
        intArray.add(bddAutomata.getSourceVariableDomain(geneIndex).vars());
        intArray.add(bddAutomata.getDestVariableDomain(geneIndex).vars());
      } else
        throw new IllegalArgumentException("The gene does not exist!");

    }

    final TIntArrayList remainingVars =
      new TIntArrayList(bddAutomata.getManager().getFactory().getVarOrder());
    remainingVars.remove(0, intArray.size());
    intArray.add(remainingVars.toArray());
    return intArray.toArray();
  }

  public String getFeasibleValues(final String variable)
  {
    final BDD mSafeStates =
      bddAutomata.getMarkedStates().and(statesAfterSynthesis);
    final String formula =
      bddAutomata.intListToFormula(variable,
                                   bddAutomata.BDD2valuations(mSafeStates,
                                                              variable));
    return formula;
  }
}
