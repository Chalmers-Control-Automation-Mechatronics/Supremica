package org.supremica.automata.BDD.EFA;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import net.sf.javabdd.BDD;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
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
 * @author Sajed Miremadi, Zhennan Fei
 */

public class BDDExtendedSynthesizer {

    private static Logger logger = LogManager.getLogger(BDDExtendedSynthesizer.class);

    public BDDExtendedAutomata bddAutomata;
    ExtendedAutomata theAutomata;
    private BDD statesAfterSynthesis;
    private ActionTimer synthesisTimer;
    private ActionTimer guardTimer;
    private HashMap<String,BDDExtendedGuardGenerator> event2guard;
    ModuleSubjectFactory factory = null;
    ExpressionParser parser = null;
    long nbrOfStates = -1;
    private Set<ExtendedAutomaton> autTobeDeclaredAsVars;
    private final EditorSynthesizerOptions options;

    public BDDExtendedSynthesizer(final ExtendedAutomata theAutomata, final  EditorSynthesizerOptions options)
    {
        this.theAutomata = theAutomata;
        bddAutomata = new BDDExtendedAutomata(theAutomata, options);
        factory = ModuleSubjectFactory.getInstance();
        parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
        this.options = options;
        if (options.getCreateAutVars())
          autTobeDeclaredAsVars = new HashSet<ExtendedAutomaton>();
    }

    public void synthesize(final EditorSynthesizerOptions options)
    {
        synthesisTimer = new ActionTimer();

        if(options.getSynthesisType().equals(SynthesisType.CONTROLLABLE))
        {
            synthesisTimer.start();
            statesAfterSynthesis =  bddAutomata.getControllableStates(options.getReachability());
            nbrOfStates = bddAutomata.nbrOfControllableStates;
            synthesisTimer.stop();
        }
        else if(options.getSynthesisType().equals(SynthesisType.NONBLOCKING))
        {
            synthesisTimer.start();
            statesAfterSynthesis =  bddAutomata.getNonblockingStates();
            nbrOfStates = bddAutomata.nbrOfNonblockingStates;
            synthesisTimer.stop();
        }
        else if(options.getSynthesisType().equals(SynthesisType.NONBLOCKING_CONTROLLABLE))
        {
            synthesisTimer.start();
            statesAfterSynthesis =  bddAutomata.getNonblockingControllableStates(options.getReachability());
            nbrOfStates = bddAutomata.nbrOfNonblockingControllableStates;
            synthesisTimer.stop();
        }

        else if(options.getSynthesisType().equals(SynthesisType.UNSAFETY))
        {
            synthesisTimer.start();
            statesAfterSynthesis =  bddAutomata.getUnsafeStates();
            nbrOfStates = bddAutomata.nbrOfBoundaryUnsafeStates;
            synthesisTimer.stop();
        }

    }

    public String getFeasibleValues(final String variable)
    {
        return bddAutomata.intListToFormula(
                            variable,
                            bddAutomata.BDD2valuations(
                                bddAutomata.getMarkedStates().and(
                                statesAfterSynthesis),variable));
    }

    public long nbrOfStates()
    {
        return nbrOfStates;
    }

    public int peakBDDNodes() {
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

    public void generateGuard(Vector<String> eventNames, final EditorSynthesizerOptions options)
    {
        if(!options.getEvent().equals(""))
        {
            eventNames = new Vector<String>();
            eventNames.add(options.getEvent());
        }

        BDDExtendedGuardGenerator bddgg = null;

        event2guard = new HashMap<String,BDDExtendedGuardGenerator>();

        guardTimer = new ActionTimer();

        final Iterator<String> it = eventNames.iterator();
        guardTimer.start();
        while(it.hasNext())
        {
            final String sigmaName = it.next();
            bddgg = new BDDExtendedGuardGenerator(bddAutomata, sigmaName, statesAfterSynthesis, options);
            event2guard.put(sigmaName, bddgg);
        }
        guardTimer.stop();

    }

    int[] decodeToIntArray(final Chromosome chromosome)
    {
        final TIntArrayList intArray = new TIntArrayList(bddAutomata.getEventDomain().vars());
        for(final String gene:chromosome.getGenes())
        {
            final Integer geneIndex = bddAutomata.getIndexMap().getVariableIndexByName(gene);

            if(bddAutomata.getSourceLocationDomain(gene) != null)
            {
                intArray.add(bddAutomata.getTempLocationDomain(gene).vars());
                intArray.add(bddAutomata.getSourceLocationDomain(gene).vars());
                intArray.add(bddAutomata.getDestLocationDomain(gene).vars());
            }
            else if(bddAutomata.getSourceVariableDomain(geneIndex) != null)
            {
                intArray.add(bddAutomata.getTempVariableDomain(geneIndex).vars());
                intArray.add(bddAutomata.getSourceVariableDomain(geneIndex).vars());
                intArray.add(bddAutomata.getDestVariableDomain(geneIndex).vars());
            }
            else
                throw new IllegalArgumentException("The gene does not exist!");

        }

        final TIntArrayList remainingVars = new TIntArrayList(bddAutomata.getManager().getFactory().getVarOrder());
        remainingVars.remove(0, intArray.size());
        intArray.add(remainingVars.toArray());
        return intArray.toArray();
    }

    public void addGuardsToAutomata(final ModuleSubject module)
    {
      String guard = "";
      BDDExtendedGuardGenerator currBDDGG = null;

      for(final AbstractSubject simSubj: module.getComponentListModifiable())
      {
        if(simSubj instanceof SimpleComponentSubject)
        {
          final List<EdgeSubject> edgesToBeAdded = new ArrayList<EdgeSubject>();
          for(final EdgeSubject ep:((SimpleComponentSubject)simSubj).getGraph().getEdgesModifiable())
          {
            SimpleExpressionSubject ses = null;
            SimpleExpressionSubject ses1 = null;
            SimpleExpressionSubject ses2 = null;

            // Some edges could have multiple events. If a guard is generated for one event, we need to
            // create an extra edge labeled by the event, copy the existing guards and actions (if any) and
            // put the generated guards there.

            final ListSubject<AbstractSubject> eventList = ep.getLabelBlock().getEventIdentifierListModifiable();
            final List<AbstractSubject> eventsToBeRemovedFromEdge = new ArrayList<AbstractSubject>();

            for (final AbstractSubject event: eventList)
            {
              final String currEvent = event.toString();
              currBDDGG = event2guard.get(currEvent);

              if (currBDDGG != null && !currBDDGG.guardIsTrue())
              {
                if (options.getCreateAutVars())
                  autTobeDeclaredAsVars.addAll(currBDDGG.getAutGuardVars());

                EdgeSubject manipulatedEdge = null;
                if (eventList.size() - eventsToBeRemovedFromEdge.size() == 1)
                {
                  manipulatedEdge = ep; // we manipulate ep itself
                }
                else // create a new edge to manipulate
                {
                  eventsToBeRemovedFromEdge.add(event);

                  final LabelBlockSubject lbs = new LabelBlockSubject();
                  lbs.getEventIdentifierListModifiable().add(event.clone());

                  GuardActionBlockSubject clonedGuardActionBlock = null;
                  if (ep.getGuardActionBlock() != null)
                    clonedGuardActionBlock = ep.getGuardActionBlock().clone();

                  manipulatedEdge = new EdgeSubject(ep.getSource(),
                                                    ep.getTarget(),
                                                    lbs,
                                                    clonedGuardActionBlock,
                                                    null,
                                                    null,
                                                    null);

                  edgesToBeAdded.add(manipulatedEdge);
                }

                // next, we insert the generated guards to the manipulated edge
                if (manipulatedEdge.getGuardActionBlock()==null)
                {
                  manipulatedEdge.setGuardActionBlock(new GuardActionBlockSubject());
                }
                String currGuard = "";
                guard = currBDDGG.getGuard();
                currGuard="";
                if(!manipulatedEdge.getGuardActionBlock().getGuardsModifiable().isEmpty())
                {
                    ses1 = manipulatedEdge.getGuardActionBlock().getGuardsModifiable().iterator().next().clone();
                    currGuard = "("+ses1.toString()+")"+" & ";
                }
                try {
                  ses = (SimpleExpressionSubject)(parser.parse(currGuard+guard,Operator.TYPE_BOOLEAN));
                  //The following line concerns the new guards that will be attached to the automata with a DIFFERENT COLOR!
                  ses2 = (SimpleExpressionSubject)(parser.parse(guard,Operator.TYPE_BOOLEAN));
                } catch (final ParseException pe) {
                  System.err.println(pe);
                  logger.error("Some of the guards could not be parsed and attached to the automata: "
                    + "It is likely that there exist some 'strange' characters in some variables or values!");
                  break;
                }
                if(!manipulatedEdge.getGuardActionBlock().getGuardsModifiable().isEmpty())
                {
                  manipulatedEdge.getGuardActionBlock().getGuardsModifiable().remove(0);
                  manipulatedEdge.getGuardActionBlock().getGuardsModifiable().add(ses);
                  //For color purposes
                  manipulatedEdge.getGuardActionBlock().getGuardsModifiable().add(ses1);
                  manipulatedEdge.getGuardActionBlock().getGuardsModifiable().add(ses2);
                }
                else
                {
                  manipulatedEdge.getGuardActionBlock().getGuardsModifiable().add(ses);
                  //For color purposes
                  manipulatedEdge.getGuardActionBlock().getGuardsModifiable().add(ses2);
                }
              }
            }
            eventList.removeAll(eventsToBeRemovedFromEdge);
          }
          ((SimpleComponentSubject)simSubj).getGraph().getEdgesModifiable().addAll(edgesToBeAdded);
        }
      }
      if (options.getCreateAutVars())
        createAutVarsAndUpdates();
    }

    private void createAutVarsAndUpdates()
    {
      for (final ExtendedAutomaton aut: this.autTobeDeclaredAsVars)
      {
        final Set<String> markedValues = new HashSet<>();
        for (final NodeProxy node: aut.getMarkedLocations())
           markedValues.add(node.getName());

        final String autVarName = aut.getName() + ExtendedAutomata.getlocVarSuffix();
        // Add automaton variables to extended automata
        aut.getExAutomata().addEnumerationVariable(autVarName,
                                                   aut.getNameToLocationMap().keySet(),
                                                   aut.getInitialLocation().getName(),
                                                   markedValues);

        for (final EdgeSubject edge:aut.getComponent().getGraph().getEdgesModifiable())
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
          edge.getGuardActionBlock().getActionsModifiable().add((BinaryExpressionSubject) assignmentAsAction);
        }
      }
    }

    public HashMap<String,BDDExtendedGuardGenerator> getEventGuardMap()
    {
        return event2guard;
    }

    public void done()
    {
        if (bddAutomata != null)
        {
            bddAutomata.done();
            bddAutomata = null;
        }
    }

}

