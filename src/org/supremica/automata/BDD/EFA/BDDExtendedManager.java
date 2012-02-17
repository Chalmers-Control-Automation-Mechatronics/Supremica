
package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi, Zhennan Fei
 */

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import org.supremica.automata.BDD.SupremicaBDDBitVector.PSupremicaBDDBitVector;
import org.supremica.automata.BDD.SupremicaBDDBitVector.ResultOverflows;
import org.supremica.automata.BDD.SupremicaBDDBitVector.SupremicaBDDBitVector;
import org.supremica.automata.BDD.SupremicaBDDBitVector.TCSupremicaBDDBitVector;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;


public class BDDExtendedManager
{
    private static Logger logger = LoggerFactory.createLogger(BDDExtendedManager.class);

    private BDDFactory factory;

//    private BDDDomain constantDomain;
    private Map<String, Integer> variableStringToIndexMap;

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

    public SupremicaBDDBitVector createSupremicaBDDBitVector(final int P_TC, final BDDFactory mFactory, final int bitnum)
    {
        if(P_TC == 0)
            return new PSupremicaBDDBitVector(mFactory, bitnum);
        else if (P_TC == 1)
            return new TCSupremicaBDDBitVector(mFactory, bitnum);

        throw new IllegalArgumentException("BDDBitVector type not defined!");
    }

    public SupremicaBDDBitVector createSupremicaBDDBitVector(final int P_TC, final BDDFactory mFactory, final int bitnum, final boolean b)
    {
        if(P_TC == 0)
            return new PSupremicaBDDBitVector(mFactory, bitnum, b);
        else if (P_TC == 1)
            return new TCSupremicaBDDBitVector(mFactory, bitnum, b);

        throw new IllegalArgumentException("BDDBitVector type not defined!");
    }

    public SupremicaBDDBitVector createSupremicaBDDBitVector(final int P_TC, final BDDFactory mFactory, final int bitnum, final long val)
    {
        if(P_TC == 0)
            return new PSupremicaBDDBitVector(mFactory, bitnum, val);
        else if (P_TC == 1)
            return new TCSupremicaBDDBitVector(mFactory, bitnum, val);

        throw new IllegalArgumentException("BDDBitVector type not defined!");
    }

    public SupremicaBDDBitVector createSupremicaBDDBitVector(final int P_TC, final BDDFactory mFactory, final BDDDomain domain)
    {
        if(P_TC == 0)
            return new PSupremicaBDDBitVector(mFactory, domain);
        else if (P_TC == 1)
            return new TCSupremicaBDDBitVector(mFactory, domain);

        throw new IllegalArgumentException("BDDBitVector type not defined!");
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

    public void partialReverseVarOrdering(final int[] varOrdering)
    {
        final int[] updatedVarOrdering = factory.getVarOrder();
        for(int i = 0; i<varOrdering.length; i++)
        {
            updatedVarOrdering[varOrdering[i]] = varOrdering[varOrdering.length-1-i];
        }

        factory.setVarOrder(updatedVarOrdering);
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

    public BDD guard2BDD(final SimpleExpressionProxy expr)
    {
        final ResultOverflows guardBDD = expr2BDDBitVec(expr, true);
        return guardBDD.getResult().getBit(0).and(guardBDD.getOverflows().not());
    }

    public BDD action2BDD(final BinaryExpressionProxy expr)
    {
        final String leftVarName = ((SimpleIdentifierProxy)expr.getLeft()).getName();
        final SupremicaBDDBitVector leftSide = bddExAutomata.BDDBitVecTargetVarsMap.get(leftVarName);
        ResultOverflows rightSide = expr2BDDBitVec(expr.getRight(),false);

        BDD overflow = getZeroBDD();
        final CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
        if(expr.getOperator().equals(cot.getIncrementOperator()))
        {
            overflow = rightSide.getOverflows().id();
            rightSide = bddExAutomata.BDDBitVecSourceVarsMap.get(leftVarName).addConsideringOverflows(rightSide.getResult());
        }

        if(expr.getOperator().equals(cot.getDecrementOperator()))
        {
            overflow = rightSide.getOverflows().id();
            rightSide = bddExAutomata.BDDBitVecSourceVarsMap.get(leftVarName).subConsideringOverflows(rightSide.getResult());
        }

//        (rightSide.getOverflows().or(overflow)).printDot();
        return leftSide.equ(rightSide.getResult()).and((rightSide.getOverflows().or(overflow)).not());
    }

    ResultOverflows expr2BDDBitVec(final SimpleExpressionProxy expr, final boolean guardAction)
    {
        if(expr instanceof UnaryExpressionProxy)
        {
            final UnaryExpressionProxy unExpr = (UnaryExpressionProxy)expr;
            if(unExpr.getOperator().equals(CompilerOperatorTable.getInstance().getNotOperator()))
            {
                final ResultOverflows ro = expr2BDDBitVec(unExpr.getSubTerm(),true);
                final SupremicaBDDBitVector tmp = ro.getResult().copy();
                tmp.setBit(0, tmp.getBit(0).not());
                return new ResultOverflows(tmp,ro.getOverflows());
            }
            else if(((UnaryExpressionProxy)expr).getOperator().equals(CompilerOperatorTable.getInstance().getUnaryMinusOperator()))
            {
                final ResultOverflows ro = expr2BDDBitVec(unExpr.getSubTerm(),true);
                return new ResultOverflows(((TCSupremicaBDDBitVector)ro.getResult()).toTwosComplement(),ro.getOverflows());
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
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(),true);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(),true);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                final SupremicaBDDBitVector rightGuard = roRight.getResult();
                tmp.setBit(0, tmp.getBit(0).and(rightGuard.getBit(0)));
                return new ResultOverflows(tmp,roLeft.getOverflows().or(roRight.getOverflows()));
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getOrOperator()))
            {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(),true);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(),true);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                final SupremicaBDDBitVector rightGuard = roRight.getResult();
                tmp.setBit(0, tmp.getBit(0).or(rightGuard.getBit(0)));
                return new ResultOverflows(tmp,roLeft.getOverflows().or(roRight.getOverflows()));
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getModuloOperator()))
            {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(),false);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(),false);
                final SupremicaBDDBitVector v2 = roRight.getResult();
                if(v2.isConst())
                    return new ResultOverflows(roLeft.getResult().divmod(v2.val(), false),roLeft.getOverflows().or(roRight.getOverflows()));
                else
                    throw new IllegalArgumentException("Divisor is not constant");
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getMinusOperator()))
            {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(),false);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(),false);
                final ResultOverflows ro = roLeft.getResult().subConsideringOverflows(roRight.getResult());
                return new ResultOverflows(ro.getResult(), ro.getOverflows().or(roLeft.getOverflows().or(roRight.getOverflows())));
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getPlusOperator()))
            {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(),false);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(),false);
                final ResultOverflows ro = roLeft.getResult().addConsideringOverflows(roRight.getResult());
                return new ResultOverflows(ro.getResult(), ro.getOverflows().or(roLeft.getOverflows().or(roRight.getOverflows())));
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getEqualsOperator()))
            {
                SupremicaBDDBitVector tmp = null;
                BDD leftOverflows = getZeroBDD();
                BDD rightOverflows = getZeroBDD();
                if(bexpr.getLeft().toString().contains(bddExAutomata.locaVarSuffix))
                {
                    final String leftString = bexpr.getLeft().toString();
                    final String locName = bexpr.getRight().toString();
                    final String autName = leftString.substring(0, leftString.indexOf(bddExAutomata.locaVarSuffix));
                    tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory, bddExAutomata.getSourceLocationDomain(autName));
                    final BDD locBDD = createBDD(bddExAutomata.getIndexMap().getLocationIndex(autName, locName),
                            bddExAutomata.getSourceLocationDomain(autName));
                    tmp.setBit(0, locBDD);
                }
                else
                {
                    final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(),false);
                    final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(),false);
                    tmp = roLeft.getResult().copy();
                    tmp.setBit(0, tmp.equ(roRight.getResult()));
                    leftOverflows = roLeft.getOverflows();
                    rightOverflows = roRight.getOverflows();
                }
                return new ResultOverflows(tmp, leftOverflows.or(rightOverflows));
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getNotEqualsOperator()))
            {
                SupremicaBDDBitVector tmp = null;
                BDD leftOverflows = getZeroBDD();
                BDD rightOverflows = getZeroBDD();
                if(bexpr.getLeft().toString().contains(bddExAutomata.locaVarSuffix))
                {
                    final String leftString = bexpr.getLeft().toString();
                    final String locName = bexpr.getRight().toString();
                    final String autName = leftString.substring(0, leftString.indexOf(bddExAutomata.locaVarSuffix));
                    tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory, bddExAutomata.getSourceLocationDomain(autName));
                    final BDD locBDD = createBDD(bddExAutomata.getIndexMap().getLocationIndex(autName, locName),
                            bddExAutomata.getSourceLocationDomain(autName)).not();
                    tmp.setBit(0, locBDD);
                }
                else
                {
                    final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(),false);
                    final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(),false);
                    tmp = roLeft.getResult().copy();
                    tmp.setBit(0, tmp.neq(roRight.getResult()));
                    leftOverflows = roLeft.getOverflows();
                    rightOverflows = roRight.getOverflows();
                }
                return new ResultOverflows(tmp, leftOverflows.or(rightOverflows));
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getGreaterThanOperator()))
            {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(),false);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(),false);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                tmp.setBit(0, tmp.gth(roRight.getResult()));
                return new ResultOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getGreaterEqualsOperator()))
            {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(),false);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(),false);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                tmp.setBit(0, tmp.gte(roRight.getResult()));
                return new ResultOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getLessThanOperator()))
            {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(),false);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(),false);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                tmp.setBit(0, tmp.lth(roRight.getResult()));
                return new ResultOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getLessEqualsOperator()))
            {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(),false);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(),false);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                tmp.setBit(0, tmp.lte(roRight.getResult()));
                return new ResultOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
            }
            else if(bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getDivideOperator()))
            {
                final SupremicaBDDBitVector v2 = expr2BDDBitVec(bexpr.getRight(),false).getResult().copy();
                if(v2.isConst())
                    return new ResultOverflows(expr2BDDBitVec(bexpr.getLeft(),false).getResult().divmod(v2.val(), true),getZeroBDD());
                else
                    throw new IllegalArgumentException("Divisor is not constant");
            }
            else
            {
                throw new IllegalArgumentException("Binary operator is not known!");
            }
        //I have added the other operators to SupremicaBDDBitVector... they should be verified though.
        //Currently, I don't have time to do that... maybe in the future (BTW it is not too much work).

        }
        else if(expr instanceof SimpleIdentifierProxy)
        {
            return new ResultOverflows(bddExAutomata.BDDBitVecSourceVarsMap.get(((SimpleIdentifierProxy)expr).getName()),getZeroBDD());
        }
        else if(expr instanceof IntConstantProxy)
        {
            if(guardAction)
            {
                SupremicaBDDBitVector tmp = null;
//                if(constantDomain ==null)
                    tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory, bddExAutomata.BDDBitVectoryType+1,0).copy();
//                    tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory, bddExAutomata.constantDomain.varNum(),0).copy();
//                else
//                    tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory, constantDomain.varNum(),0).copy();

                if(((IntConstantProxy)expr).getValue()==0)
                    tmp.setBit(0, getZeroBDD());
                else
                    tmp.setBit(0, getOneBDD());

                return new ResultOverflows(tmp, getZeroBDD());
            }
            else
            {
                final Integer index = bddExAutomata.getIndexMap().getIndexOfVal(expr.toString());
                if(index != null)
                {
//                    return new ResultOverflows(createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory, bddExAutomata.constantDomain.varNum(),index),getZeroBDD());
                  return new ResultOverflows(createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory,
                          bddExAutomata.BDDBitVectoryType+createDomain(Math.abs(index.intValue())+1).varNum(), index), getZeroBDD());
                }
                else
                {
                    logger.error(expr.toString()+" is out of the bounds. The value will be set to 0!");
                    return new ResultOverflows(createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory, bddExAutomata.BDDBitVectoryType+1,0),getZeroBDD());
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

    public void addEdge(final BDD bdd, final BDD[] transWhereVisUpdated, final BDD[] transAndNextValsForV, final int sourceLocationIndex,
            final BDDDomain sourceDomain, final int destLocationIndex, final BDDDomain destDomain, final int eventIndex, final BDDDomain eventDomain,
            final List<SimpleExpressionProxy> guards, final List<BinaryExpressionProxy> actions)
    {
        BDD sourceBDD = getOneBDD();
        BDD destBDD = getOneBDD();

        // Create a BDD representing the source location
        sourceBDD = factory.buildCube(sourceLocationIndex, sourceDomain.vars());

        // Create a BDD representing the dest location
        destBDD = factory.buildCube(destLocationIndex, destDomain.vars());

        // Create a BDD representing the event
        final BDD eventBDD = factory.buildCube(eventIndex, eventDomain.vars());

        // Add source and dest state
        sourceBDD = sourceBDD.and(destBDD);

        // Add event to source and dest state
        sourceBDD = sourceBDD.and(eventBDD);

        BDD guardBDD = factory.one();
        if(guards != null && guards.size() > 0)
        {
            //Only the first guard should be considered. If there are more, it will just affect the GUI.
            guardBDD = guard2BDD(guards.get(0));
//            guardBDD.printDot();
        }
        //Add guard to event and source and dest state
        sourceBDD = sourceBDD.and(guardBDD);

        final HashSet<String> updatedVars = new HashSet<String>();
        BDD actionBDD = factory.one();
        if(actions !=null)
        {
            for(final SimpleExpressionProxy a:actions)
            {
                final BinaryExpressionProxy bep = (BinaryExpressionProxy)a;
                updatedVars.add(((SimpleIdentifierProxy)bep.getLeft()).getName());

                final BDD currActionBDD = action2BDD(bep);
//                currActionBDD.printDot();
                actionBDD = actionBDD.and(currActionBDD);
            }
        }
//        actionBDD.printDot();

        for(final String var:updatedVars)
        {
            transWhereVisUpdated[variableStringToIndexMap.get(var)] = transWhereVisUpdated[variableStringToIndexMap.get(var)].or(sourceBDD);
            transAndNextValsForV[variableStringToIndexMap.get(var)] = transAndNextValsForV[variableStringToIndexMap.get(var)].or(sourceBDD.and(actionBDD));
        }


        // Add the transition to the set of existing transitions
        bdd.orWith(sourceBDD);
    }

    private int getMin(final int[] array)
    {
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < array.length; i++)
        {
            if(array[i] < min)
                min = array[i];
        }

        return min;
    }


    /*
        We assume the the most significant bit is at the top of the variable ordering.
        'bdd' represents the values for one single clock.
     */
    public int getMinimalValue(final BDD bdd)
    {
        int minValue = 0;

        final String nameOfClock = bddExAutomata.getAutVarName(bdd.var());
        final int minBDDVarContent =  getMin(bddExAutomata.getSourceVariableDomain(nameOfClock).vars());

        BDD iterateBDD = bdd.id();
        while(!iterateBDD.isOne())
        {
            final BDD lowChild = iterateBDD.low();
            BDD nextIterateBDD = lowChild.id();

            if(lowChild.isZero())
            {
                minValue = minValue + (int)Math.pow(2, iterateBDD.var() - minBDDVarContent);
                nextIterateBDD = iterateBDD.high().id();
            }

            iterateBDD = nextIterateBDD.id();
        }

        return minValue;
    }

    public BDD getInitiallyUncontrollableStates()
    {
        final BDDMonolithicEdges edges = ((BDDMonolithicEdges)bddExAutomata.getBDDEdges());

        if(bddExAutomata.orgExAutomata.modelHasNoPlants() || bddExAutomata.orgExAutomata.modelHasNoSpecs())
        {
            return getZeroBDD();
        }
        else
        {
        final BDD t1 = bddExAutomata.getReachableStates().and(edges.getPlantMonolithicUncontrollableEdgesForwardBDD());
        final BDD t2 = edges.getSpecMonolithicUncontrollableEdgesForwardBDD().and(t1).exist(bddExAutomata.getDestStatesVarSet());
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
            Qkn = Qk.or(image_preImage(Qk,t_u));
        }while (!Qkn.equals(Qk));

        return Qkn;
    }

    public BDD restrictedBackward(final BDD forbidden)
    {
        final BDD delta_all = ((BDDMonolithicEdges)bddExAutomata.getBDDEdges()).getMonolithicEdgesBackwardBDD();

        BDD Qkn = bddExAutomata.getMarkedStates().and(forbidden.not()).and(bddExAutomata.getReachableStates());
        BDD Qk = null;
        BDD Qm = null;
/*
        FileWriter fstream = null;
        try
        {
            fstream = new FileWriter("C:/Users/sajed/Documents/My Dropbox/Documents/Papers/Supremica_Models/FisherThompson/BDDstatistics_RB.txt");
 //           fstream = new FileWriter("/Users/sajed/Dropbox/Documents/Papers/Supremica_Models/FisherThompson/BDDstatistics_RB.txt");

        } catch (final Exception e){}
        final BufferedWriter out = new BufferedWriter(fstream);

        int iteration = 1;
*/
        do
        {
//            System.err.println("RBackward "+iteration+ "\t" + Qkn.nodeCount());
/*            try
            {
                out.write((iteration++) + "\t" + Qkn.nodeCount());
                out.newLine();
                out.close();
            } catch (final Exception e){}
*/
            Qk = Qkn.id();
            Qm = image_preImage(Qk,delta_all);
/*
            BDD clockBDD = Qm.exist(bddExAutomata.sourceLocationVariables);
            String nameOfClock = bddExAutomata.getAutVarName(clockBDD.var());

            int minClockValue = getMinimalValue(clockBDD);
            SupremicaBDDBitVector minClockValueBDDVec = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory,
                    bddExAutomata.getSourceVariableDomain(nameOfClock).size().intValue(),minClockValue);
            SupremicaBDDBitVector clockBDDVec = bddExAutomata.getBDDBitVecSource(nameOfClock);

            BDD extendedClockBDD = clockBDDVec.lth(minClockValueBDDVec);
            Qm = Qm.or(extendedClockBDD);
*/

            Qkn = ((Qk.or(Qm)).and(forbidden.not())).and(bddExAutomata.getReachableStates());
        } while (!Qkn.equals(Qk));

//        try{out.close();}catch (final Exception e){}


        return Qkn;
    }

    public BDD restrictedForward(final BDD forbidden)
    {
        final BDD delta_all = ((BDDMonolithicEdges)bddExAutomata.getBDDEdges()).getMonolithicEdgesForwardBDD();

//        System.err.println(delta_all.support().toString());

        BDD Qkn = bddExAutomata.getInitialState().and(forbidden.not());
        BDD Qk = null;
        BDD Qm = null;


 /*       FileWriter fstream = null;
        try
        {
            fstream = new FileWriter("C:/Users/sajed/Documents/My Dropbox/Documents/Papers/Supremica_Models/FisherThompson/BDDstatistics_RF.txt");
//            fstream = new FileWriter("/Users/sajed/Dropbox/Documents/Papers/Supremica_Models/FisherThompson/BDDstatistics_RF.txt");
        } catch (final Exception e){}
        BufferedWriter out = new BufferedWriter(fstream);

        int iteration = 1;
*/
        do
        {
//            System.err.println("RForward "+iteration + "\t" + Qkn.nodeCount());
/*            try
            {
                out.write((iteration++) + "\t" + Qkn.nodeCount());
                out.newLine();
            } catch (final Exception e){}
*/
            Qk = Qkn.id();
            Qm = image_preImage(Qk,delta_all);
/*
            bddExAutomata.sourceLocationDomains[0].domain().printDot();
            BDD clockBDD = Qm.exist(bddExAutomata.sourceLocationVariables);
            String nameOfClock = bddExAutomata.getAutVarName(clockBDD.var());

            int minClockValue = getMinimalValue(clockBDD);
            SupremicaBDDBitVector minClockValueBDDVec = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory,
                    bddExAutomata.getSourceVariableDomain(nameOfClock).size().intValue(),minClockValue);
            SupremicaBDDBitVector clockBDDVec = bddExAutomata.getBDDBitVecSource(nameOfClock);

            BDD extendedClockBDD = clockBDDVec.gte(minClockValueBDDVec);
            Qm = Qm.or(extendedClockBDD);
*/
            Qkn = (Qk.or(Qm)).and(forbidden.not());
        } while (!Qkn.equals(Qk));

//        try{out.close();}catch (final Exception e){}
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

    public BDD image_preImage(final BDD states, final BDD transitions)
    {
        BDD nextStates = null;

        nextStates = (transitions.and(states)).exist(bddExAutomata.getSourceStatesVarSet());
//        nextStates = (transitions.and(states)).exist(bddExAutomata.getSourceStatesVarSet());
        nextStates.replaceWith(bddExAutomata.getDest2SourceLocationPairing());
        nextStates.replaceWith(bddExAutomata.getDest2SourceVariablePairing());

        return nextStates;
    }

     /** Return a set of initial uncontrollable states. */
    BDD getDisjunctiveInitiallyUncontrollableStates()
    {

        if (bddExAutomata.plants.isEmpty() || bddExAutomata.specs.isEmpty()) {
            return getZeroBDD();
        } else {

            final TIntArrayList plantUncontrollableEvents = bddExAutomata.plantUncontrollableEventIndexList;
            final TIntArrayList specUncontrollableEvents = bddExAutomata.specUncontrollableEventIndexList;

            final TIntObjectHashMap<BDD> plantsEnabledStates =
                    new UncontrollableEventDepSets(bddExAutomata, bddExAutomata.plants, plantUncontrollableEvents).getUncontrollableEvents2EnabledStates();
            final TIntObjectHashMap<BDD> specEnabledStates =
                    new UncontrollableEventDepSets(bddExAutomata, bddExAutomata.specs, specUncontrollableEvents).getUncontrollableEvents2EnabledStates();

            final BDD uncontrollableStates = getZeroBDD();

            for (int i = 0; i < specUncontrollableEvents.size(); i++) {
                if (plantUncontrollableEvents.contains(specUncontrollableEvents.get(i))) {
                    final int eventIndex = specUncontrollableEvents.get(i);
                    uncontrollableStates.orWith(plantsEnabledStates.get(eventIndex).and(specEnabledStates.get(eventIndex).not()));
                }
            }

            return uncontrollableStates;
        }
    }

    BDD disjunctiveNonblockingControllable(final BDD forbiddenStates, final boolean reachable)
    {

        BDD previousForbidenStates = null;
        BDD tmpCoreachableStates = null;
        BDD currentForbidenStates = forbiddenStates;

        boolean flag = false;
        do {
            previousForbidenStates = currentForbidenStates.id();
            currentForbidenStates = bddExAutomata.getDepSets().uncontrollableBackwardWorkSetAlgorithm(currentForbidenStates);
            if (flag && currentForbidenStates.equals(previousForbidenStates)) {
                break;
            } else {
                tmpCoreachableStates = bddExAutomata.getDepSets()
                        .reachableBackwardRestrictedWorkSetAlgorithm(bddExAutomata.getMarkedStates(), currentForbidenStates, bddExAutomata.getReachableStates());
                        //.backwardRestrictedWorkSetAlgorithm(bddExAutomata.getMarkedStates(), currentForbidenStates);
                currentForbidenStates = tmpCoreachableStates.not();
                flag = true;
            }
        } while (!previousForbidenStates.equals(currentForbidenStates));

        BDD nonblockingControllableStates = null;
        if (reachable) {
            nonblockingControllableStates = bddExAutomata.getDepSets().
                    forwardRestrictedWorkSetAlgorithm(bddExAutomata.getInitialState(), currentForbidenStates);
        } else {
            nonblockingControllableStates = currentForbidenStates.not();
        }
        return nonblockingControllableStates;
    }
}
