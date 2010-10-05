package org.supremica.automata.BDD.EFA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import net.sf.javabdd.BDD;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.UnaryExpressionSubject;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.EFAMonlithicReachability;
import org.supremica.automata.algorithms.Guard.BDDExtendedGuardGenerator;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
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

    public BDDExtendedSynthesizer(ExtendedAutomata theAutomata)
    {
        this.theAutomata = theAutomata;
        bddAutomata = new BDDExtendedAutomata(theAutomata);
        factory = ModuleSubjectFactory.getInstance();
        parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());
    }

    public void synthesize(EditorSynthesizerOptions options)
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

    public void generateGuard(Vector eventNames, EditorSynthesizerOptions options)
    {
        eventNames.remove(0);
        String expressionType = "";
        switch(options.getExpressionType())
        {
            case(0):
                expressionType = "Forbidden";
            break;

            case(1):
                expressionType = "Allowed";
            break;

            case(2):
                expressionType = "Adaptive";
            break;
        }
        if(!options.getEvent().equals(""))
        {
            eventNames = new Vector();
            eventNames.add(options.getEvent());
        }

        BDDExtendedGuardGenerator bddgg = null;


        event2guard = new HashMap<String,BDDExtendedGuardGenerator>();

        guardTimer = new ActionTimer();

        Iterator<String> it = eventNames.iterator();
        guardTimer.start();
        while(it.hasNext())
        {
            String sigmaName = it.next();
            bddgg = new BDDExtendedGuardGenerator(bddAutomata, sigmaName, statesAfterSynthesis, options.getExpressionType());
            String TF =bddgg.getGuard();
            if(TF.equals("1"))
                TF = "This event is always ENABLED by the supervisor.";
            if(TF.equals("0"))
                TF = "This event is always DISABLED by the supervisor.";

            logger.info(expressionType+" guard for event "+sigmaName+": "+TF);

            if(!bddgg.getGuard().equals("1") && !bddgg.getGuard().equals("0"))
                logger.info("Number of terms in the expression: "+bddgg.getNbrOfTerms());
//                    try
//                    {
//                        out.write(sigmaS.getName()+"\t"+bddgg.getBDDSize()+"\t"+bddgg.getNbrOfTerms()+"\t"+bddgg.getRunTime());
//                        out.newLine();
//                    }
//                    catch (IOException e) {}

            event2guard.put(sigmaName, bddgg);
        }
        guardTimer.stop();
/*
        String expressionSample = " w==1 & (x==1 | (y==1 | z==2)) & x==0 & (y==1 | z==1)";
//        String expressionSample = "w==1 & (x==1 | (y==1 & z==2))";
        SimpleExpressionProxy sep = null;
        try {
            sep = (SimpleExpressionSubject)(parser.parse(expressionSample,Operator.TYPE_BOOLEAN));
        }catch(ParseException pe){}

        System.out.println("Expression to be reduced: "+expressionSample);
        System.out.println("Reduced expression: "+reduceExpr(sep));
*/
/*        HashSet<String> testSet = new HashSet<String>();
        testSet.add("12");
        testSet.add("1");
        testSet.add("4");
        testSet.add("2");
        testSet.add("13");
        testSet.add("11");
        System.out.println(bddAutomata.getBDDManager().isIncrementalSeq(testSet));
*/
        
//        try
//        {
//            out.newLine();
//            out.write("Total time of generating guards for all events: "+totalGuardGenTime+" ms");
//            out.close();
//        }
//        catch (IOException e) {}
    }

    public void addGuardsToAutomata(ModuleSubject module)
    {
        String guard = "";
        BDDExtendedGuardGenerator currBDDGG = null;

        for(AbstractSubject simSubj: module.getComponentListModifiable())
        {
            if(simSubj instanceof SimpleComponentSubject)
            {
                for(EdgeSubject ep:((SimpleComponentSubject)simSubj).getGraph().getEdgesModifiable())
                {
                    SimpleExpressionSubject ses = null;
                    SimpleExpressionSubject ses1 = null;
                    SimpleExpressionSubject ses2 = null;
                    //&& simSubj.getKind().name().equals("SPEC")

                    String currEvent = ep.getLabelBlock().getEventList().iterator().next().toString();
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
                                currGuard = ses1.toString()+" & ";
                            }
                            ses = (SimpleExpressionSubject)(parser.parse(currGuard+guard,Operator.TYPE_BOOLEAN));
                            //The following line cocerns the new guards that will be attached to the automata with a DIFFERENT COLOR!
                            ses2 = (SimpleExpressionSubject)(parser.parse(guard,Operator.TYPE_BOOLEAN));
                        }
                        catch(ParseException pe)
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

    private String reduceExpr(SimpleExpressionProxy sep)
    {
        if(sep instanceof  BinaryExpressionProxy)
        {
            BinaryExpressionProxy bep = (BinaryExpressionProxy)sep;
            BinaryOperator rootOperator = bep.getOperator();
            String[] rp = new String[1]; // Since rp is going to passed by reference to findSeqExpr, it is put in an array
            rp[0]="";            
            String sequence = findSeqExpr(sep, rootOperator,rp);
            logger.info("sequnce: "+sequence);
            SimpleExpressionProxy remainPart = null;
            if(rp[0].length()>0)
            {
                rp[0] = rp[0].substring(0,rp[0].length()-1);// remove the redundant operator that is attached to the end
//                logger.info(rp[0]);
                try {
                    remainPart = parser.parse(rp[0],Operator.TYPE_BOOLEAN);
                }catch(ParseException pe){}

                if(sequence.trim().length()>0)
                {                    
                    return reduceExpr(remainPart)+rootOperator+findInterval(sequence);
                }
                else
                {
                    return (reduceExpr(bep.getLeft())+rootOperator+reduceExpr(bep.getRight()));
                }
                
            }
            else
            {
                return findInterval(sequence);
            }
        }
        else
            return sep.toString();
    }

    private String findInterval(String elements)
    {
        if(elements.length()>0)
            return elements.charAt(0)+" >= 0";
        else
            return "";
    }

    private String findSeqExpr(SimpleExpressionProxy sep, Operator rootOperator,String[] remainingPart)
    {
//        logger.info("in fin...: "+sep.toString());
        if(sep instanceof BinaryExpressionProxy)
        {            
            BinaryExpressionProxy bep =(BinaryExpressionProxy)sep;
            BinaryOperator operator = (bep).getOperator();

            if (bddAutomata.getBDDManager().isOpEqRel(operator))
                return (bep.toString());
            if(operator.equals(rootOperator))
            {
                return findSeqExpr(bep.getLeft(),rootOperator,remainingPart)+ " "+findSeqExpr(bep.getRight(),rootOperator,remainingPart);
            }             
            else
            {
                remainingPart[0] += "("+(bep.toString()+")"+rootOperator);
                return "";
            }
        }
        else if(sep instanceof UnaryExpressionProxy)
        {
            //Not implemented yet. A solution could be to consider the NOT operator in the expression and then call findSeqEpr.
        }
        else
            return sep.toString();

        return "-1";
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
