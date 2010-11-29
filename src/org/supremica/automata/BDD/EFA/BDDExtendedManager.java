
package org.supremica.automata.BDD.EFA;

/**
 *
 * @author sajed
 */

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;

import org.supremica.automata.BDD.BDDLibraryType;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;


public class BDDExtendedManager
{
    private static Logger logger = LoggerFactory.createLogger(BDDExtendedManager.class);

    BDDFactory factory;

    private BDDDomain constantDomain;
    private Map<String, Integer> variableStringToIndexMap;

    BDD localOverflows;
    BDDExtendedAutomata bddExAutomata;

    public BDDExtendedManager()
    {
        this(BDDLibraryType.fromDescription(Config.BDD2_BDDLIBRARY.getAsString()));
    }

    public BDDExtendedManager(final BDDLibraryType bddpackage)
    {
        this(bddpackage, Config.BDD2_INITIALNODETABLESIZE.get(), Config.BDD2_CACHESIZE.get());
    }

    public BDDExtendedManager(final BDDLibraryType bddpackage, final int nodenum, final int cachesize)
    {
        if (factory == null)
        {
            factory = BDDFactory.init(bddpackage.getLibraryname(), nodenum, cachesize);
            factory.setMaxIncrease(Config.BDD2_MAXINCREASENODES.get());
            factory.setIncreaseFactor(Config.BDD2_INCREASEFACTOR.get());
            factory.setCacheRatio(Config.BDD2_CACHERATIO.get());
        }
    }

    public void setBDDExAutomata(final BDDExtendedAutomata bddExAutomata)
    {
        this.bddExAutomata = bddExAutomata;
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

    public BDDDomain createDomain(final int size)
    {
        return factory.extDomain(size);
    }

    public void setConstantDomain(final BDDDomain cd)
    {
        constantDomain = cd;
    }

    public void setVariableStringToIndexMap(final Map<String, Integer> v)
    {
        variableStringToIndexMap = v;
    }

    public BDDVarSet createEmptyVarSet()
    {
        return factory.emptySet();
    }

    public BDD createBDD(final int index, final BDDDomain domain)
    {
        return factory.buildCube(index, domain.vars());
    }
    public BDDPairing makePairing(final BDDDomain[] source, final BDDDomain[] dest)
    {
        final BDDPairing pairing = factory.makePair();
        pairing.set(source, dest);
        return pairing;
    }

    public BDDPairing makePairing(final BDDDomain source, final BDDDomain dest)
    {
        return factory.makePair(source, dest);
    }

    boolean isOpEqRel(final BinaryOperator op)
    {
        final CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
        if(op.equals(cot.getEqualsOperator()) || op.equals(cot.getGreaterEqualsOperator()) || op.equals(cot.getLessEqualsOperator())
                || op.equals(cot.getGreaterThanOperator()) || op.equals(cot.getLessThanOperator()) || op.equals(cot.getNotEqualsOperator()))
            return true;
        else
            return false;
    }

    boolean isOpArith(final BinaryOperator op)
    {
        final CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
        if(op.equals(cot.getPlusOperator()) || op.equals(cot.getMinusOperator()) || op.equals(cot.getTimesOperator())
                || op.equals(cot.getDivideOperator()) || op.equals(cot.getModuloOperator()))
            return true;
        else
            return false;
    }

    BDD guard2BDD(final SimpleExpressionProxy sexpr)
    {
        return expr2BDDBitVec(sexpr, true).getBit(0);
    }

    BDD action2BDD(final BinaryExpressionProxy expr)
    {
        final SupremicaBDDBitVector leftSide = bddExAutomata.BDDBitVecTargetVarsMap.get(expr.getLeft().toString());
        final SupremicaBDDBitVector rightSide = expr2BDDBitVec(expr.getRight(),false);

        final CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
        if(expr.getOperator().equals(cot.getIncrementOperator()))
        {
            final SupremicaBDDBitVector.ResultOverflows ro = bddExAutomata.BDDBitVecSourceVarsMap.get(expr.getLeft().toString()).
                    addConsideringOverflows(rightSide);
            localOverflows = localOverflows.or(ro.getOverflows());
            return leftSide.equ(ro.getResult());
        }

        if(expr.getOperator().equals(cot.getDecrementOperator()))
        {
            final SupremicaBDDBitVector.ResultOverflows ro = bddExAutomata.BDDBitVecSourceVarsMap.get(expr.getLeft().toString()).
                    addConsideringOverflows(rightSide.toTwosComplement());
            localOverflows = localOverflows.or(ro.getOverflows());
            return leftSide.equ(ro.getResult());
        }

        return leftSide.equ(rightSide);
    }

    SupremicaBDDBitVector expr2BDDBitVec(final SimpleExpressionProxy expr, final boolean guardAction)
    {
        if(expr instanceof UnaryExpressionProxy)
        {
            final UnaryExpressionProxy unExpr = (UnaryExpressionProxy)expr;
            if(unExpr.getOperator().equals(CompilerOperatorTable.getInstance().getNotOperator()))
            {
                final SupremicaBDDBitVector tmp = expr2BDDBitVec(unExpr.getSubTerm(),true).copy();
                tmp.setBit(0, tmp.getBit(0).not());
                return tmp;
            }
            else if(((UnaryExpressionProxy)expr).getOperator().equals(CompilerOperatorTable.getInstance().getUnaryMinusOperator()))
            {
                return expr2BDDBitVec(unExpr.getSubTerm(),false).toTwosComplement();
            }
            else
            {
                throw new IllegalArgumentException("Type of operator not known!");
            }

        }
        else if(expr instanceof BinaryExpressionProxy)
        {
            final BinaryExpressionProxy bexpr = (BinaryExpressionProxy)expr;
            if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getAndOperator()))
            {
                final SupremicaBDDBitVector tmp = expr2BDDBitVec(bexpr.getLeft(),true).copy();
                tmp.setBit(0, tmp.getBit(0).and(expr2BDDBitVec(bexpr.getRight(),true).getBit(0)));
                return tmp;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getOrOperator()))
            {
                final SupremicaBDDBitVector tmp = expr2BDDBitVec(bexpr.getLeft(),true).copy();
                tmp.setBit(0, tmp.getBit(0).or(expr2BDDBitVec(bexpr.getRight(),true).getBit(0)));
                return tmp;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getModuloOperator()))
            {
                final SupremicaBDDBitVector v2 = expr2BDDBitVec(bexpr.getRight(),false);
                if(v2.isConst())
                    return expr2BDDBitVec(bexpr.getLeft(),false).divmod(v2.val(), false);
                else
                    throw new IllegalArgumentException("Divisor is not constant");
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getMinusOperator()))
            {
                final SupremicaBDDBitVector.ResultOverflows ro = expr2BDDBitVec(bexpr.getLeft(),false).addConsideringOverflows
                        (expr2BDDBitVec(bexpr.getRight(),false).toTwosComplement());
                localOverflows = localOverflows.or(ro.getOverflows());
                return ro.getResult();
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getPlusOperator()))
            {                
                final SupremicaBDDBitVector.ResultOverflows ro = expr2BDDBitVec(bexpr.getLeft(),false).
                        addConsideringOverflows(expr2BDDBitVec(bexpr.getRight(),false));
                localOverflows = localOverflows.or(ro.getOverflows());
                return ro.getResult();
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getEqualsOperator()))
            {
                SupremicaBDDBitVector tmp = null;
                if(bexpr.getLeft().toString().contains(bddExAutomata.locaVarSuffix))
                {
                    final String leftString = bexpr.getLeft().toString();
                    final String locName = bexpr.getRight().toString();
                    final String autName = leftString.substring(0, leftString.indexOf(bddExAutomata.locaVarSuffix));
                    tmp = new SupremicaBDDBitVector(factory, bddExAutomata.getSourceLocationDomain(autName));
                    final BDD locBDD = createBDD(bddExAutomata.getIndexMap().getLocationIndex(autName, locName),
                            bddExAutomata.getSourceLocationDomain(autName));
                    tmp.setBit(0, locBDD);
                }
                else
                {
                    tmp = expr2BDDBitVec(bexpr.getLeft(),false).copy();
                    tmp.setBit(0, tmp.equ(expr2BDDBitVec(bexpr.getRight(),false)));
                }
                return tmp;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getNotEqualsOperator()))
            {
                SupremicaBDDBitVector tmp = null;
                if(bexpr.getLeft().toString().contains(bddExAutomata.locaVarSuffix))
                {
                    final String leftString = bexpr.getLeft().toString();
                    final String locName = bexpr.getRight().toString();
                    final String autName = leftString.substring(0, leftString.indexOf(bddExAutomata.locaVarSuffix));
                    tmp = new SupremicaBDDBitVector(factory, bddExAutomata.getSourceLocationDomain(autName));
                    final BDD locBDD = createBDD(bddExAutomata.getIndexMap().getLocationIndex(autName, locName),
                            bddExAutomata.getSourceLocationDomain(autName)).not();
                    tmp.setBit(0, locBDD);
                }
                else
                {
                    tmp = expr2BDDBitVec(bexpr.getLeft(),false).copy();
                    tmp.setBit(0, tmp.neq(expr2BDDBitVec(bexpr.getRight(),false)));
                }
                return tmp;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getGreaterThanOperator()))
            {
                final SupremicaBDDBitVector tmp = expr2BDDBitVec(bexpr.getLeft(),false).copy();
                tmp.setBit(0, tmp.gth(expr2BDDBitVec(bexpr.getRight(),false)));
                return tmp;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getGreaterEqualsOperator()))
            {
                final SupremicaBDDBitVector tmp = expr2BDDBitVec(bexpr.getLeft(),false).copy();
                tmp.setBit(0, tmp.gte(expr2BDDBitVec(bexpr.getRight(),false)));
                return tmp;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getLessThanOperator()))
            {
                final SupremicaBDDBitVector tmp = expr2BDDBitVec(bexpr.getLeft(),false).copy();
                tmp.setBit(0, tmp.lth(expr2BDDBitVec(bexpr.getRight(),false)));
                return tmp;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getLessEqualsOperator()))
            {
                final SupremicaBDDBitVector tmp = expr2BDDBitVec(bexpr.getLeft(),false).copy();
                tmp.setBit(0, tmp.lte(expr2BDDBitVec(bexpr.getRight(),false)));
                return tmp;
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getDivideOperator()))
            {
                final SupremicaBDDBitVector v2 = expr2BDDBitVec(bexpr.getRight(),false).copy();
                if(v2.isConst())
                    return expr2BDDBitVec(bexpr.getLeft(),false).divmod(v2.val(), true);
                else
                    throw new IllegalArgumentException("Divisor is not constant");
            }
            else
            {
                throw new IllegalArgumentException("Binary operator is not known!");
            }
        //I have added the other operators to SupremicaBDDBitVector... they should be verified though.
        //Currently, I don't have time to do that... maybe in the future (BTW it will just take some hours).

        }
        else if(expr instanceof SimpleIdentifierProxy)
        {
            return bddExAutomata.BDDBitVecSourceVarsMap.get(((SimpleIdentifierProxy)expr).getName());
        }
        else if(expr instanceof IntConstantProxy)
        {
            if(guardAction)
            {
                SupremicaBDDBitVector tmp = null;
                if(constantDomain ==null)
                    tmp = new SupremicaBDDBitVector(factory, 2,0).copy();
                else
                    tmp = new SupremicaBDDBitVector(factory, constantDomain.varNum(),0).copy();

                if(((IntConstantProxy)expr).getValue()==0)
                    tmp.setBit(0, getZeroBDD());
                else
                    tmp.setBit(0, getOneBDD());
                return tmp;
            }
            else
            {
                final Integer index = bddExAutomata.getIndexMap().getIndexOfVal(expr.toString());
                if(index != null)
                    return new SupremicaBDDBitVector(factory, constantDomain.varNum(),index);
                else
                {
                    logger.error(expr.toString()+" is out of the bounds. The value will be set to 0!");
                    return new SupremicaBDDBitVector(factory, constantDomain.varNum(),0);
                }
            }
        }

        throw new IllegalArgumentException("Type of expression not known!");
    }

    public void addLocation(final BDD bdd, final int locationIndex,  final BDDDomain domain)
    {
        final BDD newLocationBDD = factory.buildCube(locationIndex, domain.vars());
        bdd.orWith(newLocationBDD);
    }

    public int getLocationIndex(final String locName)
    {
        final StringTokenizer st = new StringTokenizer(locName, "_S");
        st.nextToken();
        final String locationName = st.nextToken();
        int sourLoc = -1;

        if(locationName.equals("i"))
             sourLoc = 0;
        else if(locationName.equals("e"))
            sourLoc = 1;
        else if(locationName.equals("f"))
            sourLoc = 2;
        else
            sourLoc = Integer.parseInt(locationName);

        return sourLoc;
    }

    public void addEdge(final BDD bdd, final BDD[] transWhereVisUpdated, final BDD[] transAndNextValsForV, final int sourceLocationIndex, final BDDDomain sourceDomain, final int destLocationIndex, final BDDDomain destDomain, final int eventIndex, final BDDDomain eventDomain, final List<SimpleExpressionProxy> guards, final List<BinaryExpressionProxy> actions
            ,final String sorLocName,final String desLocName)
    {
        BDD sourceBDD = getOneBDD();
        BDD destBDD = getOneBDD();

        //Very dirty code for optimizing the performance for EFAs that are generated by Sequence Planner
 /*       StringTokenizer st = new StringTokenizer(sorLocName, "_S");
        String varName = st.nextToken()+"_"+"state";

        sourceBDD = bddExAutomata.BDDBitVecSourceVarsMap.get(varName).equ(factory.constantVector(constantDomain.varNum(),getLocationIndex(sorLocName)));
        if(actions == null)
            destBDD = bddExAutomata.BDDBitVecTargetVarsMap.get(varName).equ(bddExAutomata.BDDBitVecSourceVarsMap.get(varName));
        else
            destBDD = bddExAutomata.BDDBitVecTargetVarsMap.get(varName).equ(factory.constantVector(constantDomain.varNum(),getLocationIndex(desLocName)));
 */
        // Create a BDD representing the source location
        sourceBDD = factory.buildCube(sourceLocationIndex, sourceDomain.vars());

        // Create a BDD representing the dest location
        destBDD = factory.buildCube(destLocationIndex, destDomain.vars());

        // Create a BDD representing the event
        final BDD eventBDD = factory.buildCube(eventIndex, eventDomain.vars());

        BDD guardBDD = factory.one();
        if(guards != null && guards.size() > 0)
        {
            //Only the first guard should be considered. If there are more, it will just affcet the GUI.
            localOverflows = factory.zero();
            guardBDD = guard2BDD(guards.get(0));
        }

        final HashSet<String> updatedVars = new HashSet<String>();
        BDD actionBDD = factory.one();
        if(actions !=null)
        {
            for(final SimpleExpressionProxy a:actions)
            {
                final BinaryExpressionProxy bep = (BinaryExpressionProxy)a;
                updatedVars.add(bep.getLeft().toString());
                localOverflows = factory.zero();
                //localOverflows updates in action2BDD(bep)
                final BDD currActionBDD = action2BDD(bep);
/*
                if(localOverflows.isOne())
                    System.out.println("overflow");
                else if(localOverflows.isZero())
                    System.out.println("no problem");
                else
                    localOverflows.printDot();
*/
//                currActionBDD.printDot();
                actionBDD = actionBDD.and(currActionBDD);
                bddExAutomata.getForwardOverflows().orWith(currActionBDD.and(localOverflows));
            }
        }

        // Add source and dest state
        sourceBDD.andWith(destBDD);

        // Add event to source and dest state
        sourceBDD.andWith(eventBDD);

        //Add guard to event and source and dest state
        sourceBDD.andWith(guardBDD);

        for(final String var:updatedVars)
        {
            transWhereVisUpdated[variableStringToIndexMap.get(var)] = transWhereVisUpdated[variableStringToIndexMap.get(var)].or(sourceBDD);
            transAndNextValsForV[variableStringToIndexMap.get(var)] = transAndNextValsForV[variableStringToIndexMap.get(var)].or(sourceBDD.and(actionBDD));
        }

        // Add the transition to the set of existing transitions
        bdd.orWith(sourceBDD);
    }

    public BDD getInitiallyUncontrollableStates()
    {
        final BDDMonolithicEdges edges = ((BDDMonolithicEdges)bddExAutomata.getBDDEdges());

        if(edges.plantOrSpecDoesNotExist())
        {
            return getZeroBDD();
        }
        else
        {
            final BDD t1 = bddExAutomata.getReachableStates().and(edges.getPlantMonolithicUncontrollableEdgesForwardBDD());
    //        t1.printDot();
            final BDD t2 = edges.getSpecMonolithicUncontrollableEdgesForwardBDD().and(t1).exist(bddExAutomata.getDestStatesVarSet());
    //        t2.printDot();
    //        t1.and(t2.not()).exist(bddExAutomata.getDestStatesVarSet()).exist(bddExAutomata.getEventVarSet()).printDot();
            return t1.and(t2.not()).exist(bddExAutomata.getDestStatesVarSet()).exist(bddExAutomata.getEventVarSet());
        }
    }

    public BDD uncontrollableBackward(final BDD forbidden)
    {
        final BDD t_u = ((BDDMonolithicEdges)bddExAutomata.getBDDEdges()).getMonolithicUncontrollableEdgesBackwardBDD();

        BDD Qk = null;
        BDD Qkn = forbidden.id();
        do
        {
            Qk = Qkn.id();
            Qkn = Qk.or(image_preImage(Qk,t_u,bddExAutomata.getBackwardOverflows()));
        }while (!Qkn.equals(Qk));

        return Qkn;
    }

    public BDD restrictedBackward(final BDD forbidden)
    {
        final BDD delta_all = ((BDDMonolithicEdges)bddExAutomata.getBDDEdges()).getMonolithicEdgesBackwardBDD();

        BDD Qkn = bddExAutomata.getMarkedStates().and(forbidden.not());
        BDD Qk = null;
        do
        {
          Qk = Qkn.id();
          Qkn = Qk.or(image_preImage(Qk,delta_all,bddExAutomata.getBackwardOverflows())).and(forbidden.not());
        } while (!Qkn.equals(Qk));

        return Qkn;
    }

    public BDD restrictedForward(final BDD forbidden)
    {
        final BDD delta_all = ((BDDMonolithicEdges)bddExAutomata.getBDDEdges()).getMonolithicEdgesForwardBDD();

        BDD Qkn = bddExAutomata.getInitialState().and(forbidden.not());
        BDD Qk = null;

        do
        {
          Qk = Qkn.id();
          Qkn = (Qk.or(image_preImage(Qk,delta_all,bddExAutomata.getForwardOverflows()))).and(forbidden.not());
        } while (!Qkn.equals(Qk));

        return Qkn;
    }

    public BDD nonblockingControllable(final BDD forbidden, final boolean reachable)
    {
        BDD Qkn = forbidden;

        BDD Qk = null;

        BDD Q1 = null;
        BDD Q2 = null;

        do
        {
            Qk = Qkn.id();
            Q1 = restrictedBackward(Qk);
            Q2 = uncontrollableBackward(Q1.not());
            Qkn = Qk.or(Q2);
        }while((!Qkn.equals(Qk)));

        if(reachable)
            return restrictedForward(Qkn);
        else
            return Qkn.not();
    }

    public BDD image_preImage(final BDD states, final BDD transitions, final BDD overflows)
    {
        BDD nextStates = null;

        nextStates = (transitions.and(states).and(overflows.not())).exist(bddExAutomata.getSourceStatesVarSet());
//        nextStates = nextStates.exist(bdda.getEventVarSet());
        nextStates.replaceWith(bddExAutomata.getDest2SourceLocationPairing());
        nextStates.replaceWith(bddExAutomata.getDest2SourceVariablePairing());

        return nextStates;
    }

}
