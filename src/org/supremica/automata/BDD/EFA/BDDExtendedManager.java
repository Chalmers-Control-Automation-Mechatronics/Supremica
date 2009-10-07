
package org.supremica.automata.BDD.EFA;

/**
 *
 * @author sajed
 */
import org.supremica.log.*;
import org.supremica.automata.BDD.BDDLibraryType;
import org.supremica.properties.Config;
import net.sf.javabdd.*;
import java.util.*;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import org.supremica.automata.ExtendedAutomataIndexMap;

public class BDDExtendedManager
{
    private static Logger logger = LoggerFactory.createLogger(BDDExtendedManager.class);

    static BDDFactory factory;

    private static HashMap<String,BDDBitVector> bddBitVecSourceMap;
    private static HashMap<String,BDDBitVector> bddBitVecTargetMap;

    private static BDDDomain constantDomain;
    private static Map<String, Integer> variableStringToIndexMap;

    public BDDExtendedManager()
    {
        this(BDDLibraryType.fromDescription(Config.BDD2_BDDLIBRARY.getAsString()));
    }

    public BDDExtendedManager(BDDLibraryType bddpackage)
    {
        this(bddpackage, Config.BDD2_INITIALNODETABLESIZE.get(), Config.BDD2_CACHESIZE.get());
    }

    public BDDExtendedManager(BDDLibraryType bddpackage, int nodenum, int cachesize)
    {
        if (factory == null)
        {
            factory = BDDFactory.init(bddpackage.getLibraryname(), nodenum, cachesize);
            factory.setMaxIncrease(Config.BDD2_MAXINCREASENODES.get());
            factory.setIncreaseFactor(Config.BDD2_INCREASEFACTOR.get());
            factory.setCacheRatio(Config.BDD2_CACHERATIO.get());

        }
    }

    public BDDFactory getFactory()
    {
        return factory;
    }

    public void done()
    {
        if (factory != null)
        {
            factory.done();
            factory = null;
        }
    }

    public BDD getZeroBDD()
    {
        return factory.zero();
    }

    public BDD getOneBDD()
    {
        return factory.one();
    }

    public BDDDomain createDomain(int size)
    {
        return factory.extDomain(size);
    }

    public void setConstantDomain(BDDDomain cd)
    {
        constantDomain = cd;
    }

    public void setBDDBitVecSourceMap(HashMap<String,BDDBitVector> b)
    {
        bddBitVecSourceMap = b;
    }

    public void setBDDBitVecTargetMap(HashMap<String,BDDBitVector> b)
    {
        bddBitVecTargetMap = b;
    }

    public void setVariableStringToIndexMap(Map<String, Integer> v)
    {
        variableStringToIndexMap = v;
    }

    public BDDVarSet createEmptyVarSet()
    {
        return factory.emptySet();
    }

    public BDD createBDD(int index, BDDDomain domain)
    {
        return factory.buildCube(index, domain.vars());
    }
    public BDDPairing makePairing(BDDDomain[] source, BDDDomain[] dest)
    {
        BDDPairing pairing = factory.makePair();
        pairing.set(source, dest);
        return pairing;
    }

    public BDDPairing makePairing(BDDDomain source, BDDDomain dest)
    {
        return factory.makePair(source, dest);
    }

    static BDD guard2BDD(BinaryExpressionProxy expr)
    {
        if(expr.getOperator().getName().equals("|"))
            return guard2BDD((BinaryExpressionProxy)expr.getLeft()).or(guard2BDD((BinaryExpressionProxy)expr.getRight()));
        else if(expr.getOperator().getName().equals("&"))
            return guard2BDD((BinaryExpressionProxy)expr.getLeft()).and(guard2BDD((BinaryExpressionProxy)expr.getRight()));
        else if(isOpEqRel(expr.getOperator().getName()))
        {
            BDDBitVector v1,v2;
            if(expr.getLeft() instanceof BinaryExpressionProxy)
            {
                v1 = arithExpr2BDDBitVec((BinaryExpressionProxy)expr.getLeft());
            }
            else
            {
                BDDBitVector vec =  bddBitVecSourceMap.get(expr.getLeft().toString());
                if(vec == null)
                {
                    vec = factory.buildVector(constantDomain);
                    vec.initialize(Integer.parseInt(expr.getLeft().toString()));
                }

                v1 = vec;
            }

            if(expr.getRight() instanceof BinaryExpressionProxy)
            {
                v2 = arithExpr2BDDBitVec((BinaryExpressionProxy)expr.getRight());
            }
            else
            {
                BDDBitVector vec =  bddBitVecSourceMap.get(expr.getRight().toString());
                if(vec == null)
                {
                    vec = factory.buildVector(constantDomain);
                    vec.initialize(Integer.parseInt(expr.getRight().toString()));
                }

                v2 = vec;
            }

            return BDDBitVecEqRelOp(v1, v2,expr.getOperator().getName());
        }

        return null;
    }

    static BDD action2BDD(BinaryExpressionProxy expr)
    {
        BDDBitVector leftSide = bddBitVecTargetMap.get(expr.getLeft().toString());
        return leftSide.equ(arithExpr2BDDBitVec(expr.getRight()));
    }

    static BDDBitVector arithExpr2BDDBitVec(SimpleExpressionProxy expr)
    {
        if(!(expr instanceof BinaryExpressionProxy))
        {
            BDDBitVector vec =  bddBitVecSourceMap.get(expr.toString());
            if(vec == null)
            {
                vec = factory.buildVector(constantDomain);
                vec.initialize(Integer.parseInt(expr.toString()));
            }

            return vec;
        }


        BDDBitVector left = arithExpr2BDDBitVec(((BinaryExpressionProxy)expr).getLeft());
        BDDBitVector right = arithExpr2BDDBitVec(((BinaryExpressionProxy)expr).getRight());
        BDDBitVector result = BDDBitVecArithOp(left,right,((BinaryExpressionProxy)expr).getOperator().getName());

        return result;
    }


    static BDD BDDBitVecEqRelOp(BDDBitVector v1, BDDBitVector v2, String op)
    {
        if(op.equals("=="))
            return v1.equ(v2);
        else if(op.equals("!="))
            return v1.neq(v2);
        else if(op.equals(">"))
            return v1.gth(v2);
        else if(op.equals(">="))
            return v1.gte(v2);
        else if(op.equals("<"))
            return v1.lth(v2);
        else if(op.equals("<="))
            return v1.lte(v2);

        return null;
    }

    static BDDBitVector BDDBitVecArithOp(BDDBitVector v1, BDDBitVector v2, String op)
    {
        if(op.equals("+"))
            return v1.add(v2);
        else if(op.equals("-"))
            return v1.sub(v2);
/*        else if(op.equals("*"))
            return v1.mul(v2);
        else if(op.equals("/"))
            return v1.dev(v2);
        else if(op.equals("%"))
            return v1.devmod(v2);
*/
        return null;
    }

    static boolean isOpEqRel(String op)
    {
        if(op.equals("==") || op.equals(">=") || op.equals("<=") || op.equals(">") || op.equals("<") || op.equals("!="))
            return true;
        else
            return false;
    }

    static boolean isOpArith(String op)
    {
        if(op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") || op.equals("%"))
            return true;
        else
            return false;
    }

    public static void addLocation(BDD bdd, int locationIndex,  BDDDomain domain)
    {
        BDD newLocationBDD = factory.buildCube(locationIndex, domain.vars());
        bdd.orWith(newLocationBDD);
    }

    public static void addEdge(BDD bdd, BDD[] transWhereVisUpdated, BDD[] transAndNextValsForV, int sourceStateIndex, BDDDomain sourceDomain, int destStateIndex, BDDDomain destDomain, int eventIndex, BDDDomain eventDomain, List<SimpleExpressionProxy> guards, List<BinaryExpressionProxy> actions, HashMap<String,BDDBitVector> bddBitVecSourceMap, HashMap<String,BDDBitVector> bddBitVecTargetMap)
    {
        BDDExtendedManager.bddBitVecSourceMap = bddBitVecSourceMap;
        BDDExtendedManager.bddBitVecTargetMap = bddBitVecTargetMap;

        // Create a BDD representing the source state
        BDD sourceBDD = factory.buildCube(sourceStateIndex, sourceDomain.vars());

        // Create a BDD representing the dest state
        BDD destBDD = factory.buildCube(destStateIndex, destDomain.vars());

        // Create a BDD representing the event
        BDD eventBDD = factory.buildCube(eventIndex, eventDomain.vars());

        BDD guardBDD = factory.one();
        if(guards != null)
        {
            for(SimpleExpressionProxy g:guards)
            {
//                System.out.println(g.toString());
                guardBDD.andWith(guard2BDD((BinaryExpressionProxy)g));
            }
        }

        HashSet<String> updatedVars = new HashSet<String>();
        BDD actionBDD = factory.one();
        if(actions !=null)
        {
            for(SimpleExpressionProxy a:actions)
            {
//                System.out.println(a.toString());
                BinaryExpressionProxy bep = (BinaryExpressionProxy)a;
                updatedVars.add(bep.getLeft().toString());
                actionBDD.andWith(action2BDD(bep));
            }
        }

        // Add source and dest state
        sourceBDD.andWith(destBDD);

        // Add event to source and dest state
        sourceBDD.andWith(eventBDD);

        //Add guard to event and source and dest state
        sourceBDD.andWith(guardBDD);

        for(String var:updatedVars)
        {
            transWhereVisUpdated[variableStringToIndexMap.get(var)] = transWhereVisUpdated[variableStringToIndexMap.get(var)].or(sourceBDD);
            transAndNextValsForV[variableStringToIndexMap.get(var)] = transAndNextValsForV[variableStringToIndexMap.get(var)].or(sourceBDD.and(actionBDD));
        }

        //Add action to guard, event and source and dest state
//        sourceBDD.andWith(actionBDD);

        // Add the transition to the set of existing transitions
        bdd.orWith(sourceBDD);       
    }

    public static BDD reachableStates(BDD initialStates, BDDEdges edges, BDDVarSet sourceStateVariables, BDDVarSet eventVariables, BDDPairing destToSourceLocationPairing, BDDPairing destToSourceVariablePairing)
    {
        BDD reachableStatesBDD = initialStates.id();
        BDD previousReachableStatesBDD = null;

        logger.debug("In reachableStates");
        do
        {
            previousReachableStatesBDD = reachableStatesBDD.id();

            BDD nextStatesAndTransitionsBDD;
            if (edges instanceof BDDMonolithicEdges)
            {
                BDD monolithicEdgesBDD = ((BDDMonolithicEdges)edges).getMonolithicEdgesForwardBDD();
                //logger.debug("Number of nodes in monolithicTransitionsBDD: " + monolithicTransitionsBDD.nodeCount());

                nextStatesAndTransitionsBDD = reachableStatesBDD.relprod(monolithicEdgesBDD, sourceStateVariables);

                //logger.debug("Number of nodes in nextStatesAndTransitionsBDD: " + nextStatesAndTransitionsBDD.nodeCount());
            }
            else
            {
                logger.error("Unknown BDDTransition class: " + edges.getClass());
                return null;
            }


            BDD nextStatesBDD = nextStatesAndTransitionsBDD.replace(destToSourceLocationPairing);
            nextStatesBDD.replaceWith(destToSourceVariablePairing);

            reachableStatesBDD.orWith(nextStatesBDD);

            logger.debug("Number of nodes in reachableStatesBDD: " + reachableStatesBDD.nodeCount());
        }
        while (!reachableStatesBDD.equals(previousReachableStatesBDD)); // Until no new states are found

        return reachableStatesBDD;
    }

    public static BDD coreachableStates(BDD markedStates, BDDEdges edges, BDDVarSet sourceStateVariables, BDDVarSet eventVariables, BDDPairing destToSourceStatePairing, BDDPairing destToSourceVariablePairing)
    {
        BDD coreachableStatesBDD = markedStates.id();
        BDD previousCoreachableStatesBDD = null;

        do
        {
            previousCoreachableStatesBDD = coreachableStatesBDD.id();

            BDD previousStatesAndTransitionsBDD;
            if (edges instanceof BDDMonolithicEdges)
            {
                BDD monolithicEdges = ((BDDMonolithicEdges)edges).getMonolithicEdgesBackwardBDD();
                previousStatesAndTransitionsBDD = previousCoreachableStatesBDD.relprod(monolithicEdges, sourceStateVariables);
            }
            else
            {
                logger.error("Unknown BDDTransition class: " + edges.getClass());
                return null;
            }

            BDD previousStatesBDD = previousStatesAndTransitionsBDD.replace(destToSourceStatePairing);
            previousStatesBDD.replaceWith(destToSourceVariablePairing);


            coreachableStatesBDD.orWith(previousStatesBDD);
        }
        while (!coreachableStatesBDD.equals(previousCoreachableStatesBDD)); // Until no new states are found

        return coreachableStatesBDD;
    }

    public static BDD prelimUncontrollableStates(BDDExtendedAutomata bdda)
    {

        BDD uncontrollableStatesBDD = null;

        BDD commonUnconEvents = bdda.getSpecsUncontrollableEvents().and(bdda.getPlantsUncontrollableEvents());
        BDD outgoingTransitions = ((BDDMonolithicEdges)bdda.getBDDEdges()).getMyMonolithicEdgesForwardBDD().exist(bdda.getDestLocationVariables());
//        BDD outgoingTransitions = ((BDDMonolithicTransitions)bdda.bddTransitions).transitionForwardBDD.exist(bdda.getDestStateVariables());
        BDD nonexistingOutgTrans = outgoingTransitions.not();
        BDD nonexistingOutgTransWithUnconEvents = nonexistingOutgTrans.and(commonUnconEvents);

//        nonexistingOutgTransWithUnconEvents.printDot();
//        System.out.println("number of transitions: "+outgoingTransitions.pathCount());


        uncontrollableStatesBDD = bdda.getPlantsForwardTransitions().and(bdda.getPlantsSelfLoopsBDD().not()).relprod(nonexistingOutgTransWithUnconEvents,bdda.getEventVarSet());
//        uncontrollableStatesBDD = bdda.getPlantsForwardTransitions().relprod(nonexistingOutgTransWithUnconEvents,bdda.getEventVarSet());

        uncontrollableStatesBDD = uncontrollableStatesBDD.exist(bdda.getDestLocationVariables());

        return uncontrollableStatesBDD;
    }

/*    public BDD uncontrollableBackward(BDDAutomata bdda, BDD forbidden)
    {

        BDD delta_all = bdda.getPlantsForwardTransitions().and(bdda.getSpecsForwardTransitions());
        BDD t_u = delta_all.relprod(bdda.getUncontrollableEvents(), bdda.getEventVarSet());

        BDD r_all_p, r_all = forbidden.replace(makePairing(bdda.getSourceStateDomains(),bdda.getDestStateDomains()));

        BDD front = r_all.id();

        do
        {
            r_all_p = r_all;

            BDD tmp = t_u.relprod(front, bdda.getDestStateVariables());
            BDD tmp2 = tmp.replace(makePairing(bdda.getSourceStateDomains(),bdda.getDestStateDomains()));

 //           tmp.free();
 //           front.free();

            r_all = r_all.or(tmp2);
//            front = fso.choose(r_all, tmp2);    // Takes care of tmp2!
        } while (!r_all_p.equals(r_all));

//        front.free();
//        t_u.free();


        BDD ret = r_all.replace(makePairing(bdda.getDestStateDomains(),bdda.getSourceStateDomains()));
//        r_all.free();

        return ret;
    }*/

    public BDD uncontrollableBackward(BDDExtendedAutomata bdda, BDD forbidden)
    {
        BDD delta_all = ((BDDMonolithicEdges)bdda.getBDDEdges()).getMyMonolithicEdgesBackwardBDD();
        BDD t_u = delta_all.and(bdda.getUncontrollableEvents());

        BDD Qk = null;
        BDD Qkn = forbidden;

        do
        {
            Qk = Qkn.id();
            Qkn = Qk.or(preImage(bdda,Qk,t_u));
        } while (!Qkn.equals(Qk));

        return Qkn;
    }

    public BDD restrictedBackward(BDDExtendedAutomata bdda, BDD forbidden)
    {
        BDD delta_all = ((BDDMonolithicEdges)bdda.getBDDEdges()).getMonolithicEdgesBackwardBDD();

        BDD Qkn = bdda.getMarkedLocations().and(forbidden.not());
        BDD Qk = null;

        do
        {
          Qk = Qkn.id();
          Qkn = Qk.or(preImage(bdda,Qk,delta_all)).and(forbidden.not());
        } while (!Qkn.equals(Qk));

        return Qkn;
    }

    public BDD safeStateSynthesis(BDDExtendedAutomata bdda, BDD forbidden)
    {
        BDD Qkn = forbidden;
        BDD Qk = null;

        BDD Q1 = null;
        BDD Q2 = null;

        do
        {
            Qk = Qkn.id();
            Q1 = restrictedBackward(bdda, Qk);
            Q2 = uncontrollableBackward(bdda, Q1.not());
            Qkn = Qk.or(Q2);
        }while((!Qkn.equals(Qk)));

        return Qkn.not();
    }

    public BDD preImage(BDDExtendedAutomata bdda, BDD states, BDD backwardEdges)
    {
        BDD preStates = null;

        preStates = backwardEdges.relprod(states, bdda.getSourceLocationVariables());
        preStates = preStates.exist(bdda.getEventVarSet());
        preStates.replaceWith(bdda.getDest2SourcePairing());

        return preStates;
    }

}
