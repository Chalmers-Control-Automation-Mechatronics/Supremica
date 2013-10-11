package org.supremica.automata.BDD.EFA;

import gnu.trove.list.array.TIntArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import net.sf.javabdd.BDD;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.Guard.BDDExtendedGuardGenerator;
import org.supremica.automata.algorithms.Guard.GeneticMinimizer.Chromosome;
import org.supremica.automata.algorithms.SynthesisType;
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

    public BDDExtendedSynthesizer(final ExtendedAutomata theAutomata, final  EditorSynthesizerOptions options)
    {
        this.theAutomata = theAutomata;
        bddAutomata = new BDDExtendedAutomata(theAutomata, options);
        factory = ModuleSubjectFactory.getInstance();
        parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
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
            nbrOfStates = (long)bddAutomata.nbrOfBoundaryUnsafeStates;
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
                for(final EdgeSubject ep:((SimpleComponentSubject)simSubj).getGraph().getEdgesModifiable())
                {
                    SimpleExpressionSubject ses = null;
                    SimpleExpressionSubject ses1 = null;
                    SimpleExpressionSubject ses2 = null;

                    final String currEvent = ep.getLabelBlock().getEventIdentifierList().iterator().next().toString();
                    currBDDGG = event2guard.get(currEvent);

                    if(ep.getGuardActionBlock()==null)
                    {
                        ep.setGuardActionBlock(new GuardActionBlockSubject());
                    }

                    if( currBDDGG != null && !currBDDGG.guardIsTrue())
                    {
                        String currGuard="";
                        try
                        {
                            guard = currBDDGG.getGuard();
                            currGuard="";

                            if(!ep.getGuardActionBlock().getGuardsModifiable().isEmpty())
                            {
                                ses1 = ep.getGuardActionBlock().getGuardsModifiable().iterator().next().clone();
                                currGuard = "("+ses1.toString()+")"+" & ";
                            }
                            ses = (SimpleExpressionSubject)(parser.parse(currGuard+guard,Operator.TYPE_BOOLEAN));

                            //The following line cocerns the new guards that will be attached to the automata with a DIFFERENT COLOR!
                            ses2 = (SimpleExpressionSubject)(parser.parse(guard,Operator.TYPE_BOOLEAN));
                        }
                        catch(final ParseException pe)
                        {
                            System.err.println(pe);
                            logger.error("Some of the guards could not be parsed and attached to the automata: It is likely that there exists some 'strange' characters in some variables or values!");
                            break;
                        }
                        if(!ep.getGuardActionBlock().getGuardsModifiable().isEmpty())
                        {
                            ep.getGuardActionBlock().getGuardsModifiable().remove(0);
                            ep.getGuardActionBlock().getGuardsModifiable().add(ses);
                            //For color purposes
                            ep.getGuardActionBlock().getGuardsModifiable().add(ses1);
                            ep.getGuardActionBlock().getGuardsModifiable().add(ses2);
                        }
                        else
                        {
                            ep.getGuardActionBlock().getGuardsModifiable().add(ses);
                            //For color purposes
                            ep.getGuardActionBlock().getGuardsModifiable().add(ses2);
                        }


                    }
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

