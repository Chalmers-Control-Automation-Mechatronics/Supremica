package org.supremica.automata.BDD.EFA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import net.sf.javabdd.BDD;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.Guard.BDDExtendedGuardGenerator;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.ActionTimer;


/**
 *
 * @author sajed
 */
public class BDDExtendedSynthesizer {

    private static Logger logger = LoggerFactory.createLogger(BDDExtendedSynthesizer.class);

    BDDExtendedAutomata bddAutomata;
    ExtendedAutomata theAutomata;
    private BDD statesAfterSynthesis;
    private ActionTimer synthesisTimer;
    private ActionTimer guardTimer;
    private HashMap<String,BDDExtendedGuardGenerator> event2guard;
    ModuleSubjectFactory factory = null;
    ExpressionParser parser = null;

    public BDDExtendedSynthesizer(final ExtendedAutomata theAutomata)
    {
        this.theAutomata = theAutomata;
        bddAutomata = new BDDExtendedAutomata(theAutomata);
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

        ExtendedAutomaton efa = theAutomata.iterator().next();
        EFAMonlithicReachability efaMR = new EFAMonlithicReachability(efa.getComponent(), theAutomata.getVars(),efa.getAlphabet());
        theAutomata.addAutomaton(new ExtendedAutomaton(theAutomata, efaMR.createEFA()));
*/


        synthesisTimer = new ActionTimer();
        if(options.getSynthesisType().equals(SynthesisType.CONTROLLABLE))
        {
            synthesisTimer.start();
            statesAfterSynthesis =  bddAutomata.getControllableStates(options.getReachability());
            synthesisTimer.stop();
        }
        else if(options.getSynthesisType().equals(SynthesisType.NONBLOCKING))
        {
            synthesisTimer.start();
            statesAfterSynthesis =  bddAutomata.getNonblockingStates();
            synthesisTimer.stop();
        }
        else if(options.getSynthesisType().equals(SynthesisType.NONBLOCKINGCONTROLLABLE))
        {
            synthesisTimer.start();
            statesAfterSynthesis =  bddAutomata.getNonblockingControllableStates(options.getReachability());
            synthesisTimer.stop();
        }

    }

    public int nbrOfStates()
    {
        return (int)bddAutomata.nbrOfStatesBDD(statesAfterSynthesis);
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
        eventNames.remove(0);
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

                    final String currEvent = ep.getLabelBlock().getEventList().iterator().next().toString();
                    currBDDGG = event2guard.get(currEvent);

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
