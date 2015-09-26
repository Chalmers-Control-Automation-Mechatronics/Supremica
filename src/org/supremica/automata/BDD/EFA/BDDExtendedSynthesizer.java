package org.supremica.automata.BDD.EFA;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.Guard.BDDExtendedGuardGenerator;
import org.supremica.automata.algorithms.Guard.GeneticMinimizer.Chromosome;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.ActionTimer;


/**
 *
 * @author sajed
 */
public class BDDExtendedSynthesizer {

    private static Logger logger = LoggerFactory.createLogger(BDDExtendedSynthesizer.class);

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
 /*
        Map<String,Integer> var2val = new HashMap<String,Integer>();
        for(VariableComponentProxy var:theAutomata.getVars())
        {
            int i = Integer.parseInt(((BinaryExpressionProxy)(var.getInitialStatePredicate())).getRight().toString());
            var2val.put(var.getName(), i);
        }
*/
/*
        System.err.println("Computing reachablity graph");
        ExtendedAutomaton efa = theAutomata.iterator().next();
        EFAMonlithicReachability efaMR = new EFAMonlithicReachability(efa.getComponent(), theAutomata.getVars(),efa.getAlphabet());
        theAutomata.addAutomaton(new ExtendedAutomaton(theAutomata, efaMR.createEFA()));
        System.err.println("Reachablity graph computed");
*/

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
        else if(options.getSynthesisType().equals(SynthesisType.NONBLOCKINGCONTROLLABLE))
        {
            synthesisTimer.start();
            statesAfterSynthesis =  bddAutomata.getNonblockingControllableStates(options.getReachability());
            nbrOfStates = bddAutomata.nbrOfNonblockingControllableStates;
            synthesisTimer.stop();
        }

        else if(options.getSynthesisType().equals(SynthesisType.UNSAFETY))
        {
            synthesisTimer.start();

//            BDDDomain v11Domain = bddAutomata.getSourceVariableDomain(bddAutomata.theIndexMap.getVariableIndexByName("v11"));
//            BDDDomain v12Domain = bddAutomata.getSourceVariableDomain(bddAutomata.theIndexMap.getVariableIndexByName("v12"));
//            BDDDomain v21Domain = bddAutomata.getSourceVariableDomain(bddAutomata.theIndexMap.getVariableIndexByName("v21"));
//            BDDDomain v22Domain = bddAutomata.getSourceVariableDomain(bddAutomata.theIndexMap.getVariableIndexByName("v22"));
//            BDDDomain VR1Domain = bddAutomata.getSourceVariableDomain(bddAutomata.theIndexMap.getVariableIndexByName("r1"));
//            BDDDomain VR2Domain = bddAutomata.getSourceVariableDomain(bddAutomata.theIndexMap.getVariableIndexByName("r2"));
//            BDDDomain VR3Domain = bddAutomata.getSourceVariableDomain(bddAutomata.theIndexMap.getVariableIndexByName("r3"));
//
//            BDD state1 = bddAutomata.getManager().createBDD(0,v11Domain).and(
//                    bddAutomata.getManager().createBDD(1,v12Domain)).and(
//                    bddAutomata.getManager().createBDD(1,v21Domain)).and(
//                    bddAutomata.getManager().createBDD(0,v22Domain));
//
//            BDD state2 = bddAutomata.getManager().createBDD(1,v11Domain).and(
//                    bddAutomata.getManager().createBDD(0,v12Domain)).and(
//                    bddAutomata.getManager().createBDD(0,v21Domain)).and(
//                    bddAutomata.getManager().createBDD(1,v22Domain));
//
//
//            BDD minimalDeadlocks = state1.or(state2);
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
//            System.err.println("The currrrrent event is: "+ sigmaName);
//            Set<String> genes = new HashSet<String>(bddAutomata.variableOrderingNames);
//            genes.remove("Events");
//            genes.remove("1");
//            FitnessEvaluation fitnessEvaluation = new FitnessEvaluation() {
//                Map<Chromosome,Integer> cache = new HashMap<Chromosome, Integer>();
//                public int eval(Chromosome chromosome)
//                {
//                    if(cache.containsKey(chromosome))
//                    {
//                        return cache.get(chromosome);
//                    }
//                    else
//                    {
//                        int[] optimalVarOrdering = decodeToIntArray(chromosome);
////                        System.err.println("Changing var order...");
//                        bddAutomata.getManager().getFactory().setVarOrder(optimalVarOrdering);
////                        System.err.println("Change of var order completed.");
//                        BDDExtendedGuardGenerator bddgg = new BDDExtendedGuardGenerator(bddAutomata, sigmaName, statesAfterSynthesis, options);
//                        cache.put(chromosome, bddgg.getNbrOfTerms());
//                        return bddgg.getNbrOfTerms();
//                    }
//                }
//            };
//            int[] optimalVarOrdering = decodeToIntArray((new Genetics(fitnessEvaluation, genes, new Genetics.GeneticOptions()).runGenetics()));
//            bddAutomata.getManager().getFactory().setVarOrder(optimalVarOrdering);

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
            // create an extra edge labeled by the event, copy the existing guards and actions and
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

                  manipulatedEdge = new EdgeSubject(ep.getSource(),
                                                    ep.getTarget(),
                                                    lbs,
                                                    ep.getGuardActionBlock() == null? null : ep.getGuardActionBlock().clone(),
                                                    null, // straight line
                                                    ep.getStartPoint(),
                                                    ep.getEndPoint());

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
      for (final ExtendedAutomaton aut: autTobeDeclaredAsVars)
      {
        final Set<String> markedValues = new HashSet<>();
        for (final NodeProxy node: aut.getMarkedLocations())
           markedValues.add(node.getName());

        final String autVarName = aut.getName() + ExtendedAutomata.getlocVarSuffix();
        aut.getExAutomata().addEnumerationVariable(autVarName,
                                                   aut.getNameToLocationMap().keySet(),
                                                   aut.getInitialLocation().getName(), markedValues);

        // add updates to the edges
        for (final Map.Entry<NodeProxy, ArrayList<EdgeSubject>> entry: aut.getLocationToIngoingEdgesMap().entrySet())
        {
          final String target = entry.getKey().getName();
          for (final EdgeSubject edge: entry.getValue())
          {
            if (edge.getGuardActionBlock() == null)
              edge.setGuardActionBlock(new GuardActionBlockSubject());
            final String assignment = autVarName + " = " + target;
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

