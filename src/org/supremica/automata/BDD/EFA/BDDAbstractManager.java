package org.supremica.automata.BDD.EFA;

import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.subject.module.IntConstantSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.BDD.SupremicaBDDBitVector.BDDOverflows;
import org.supremica.automata.BDD.SupremicaBDDBitVector.PSupremicaBDDBitVector;
import org.supremica.automata.BDD.SupremicaBDDBitVector.ResultOverflows;
import org.supremica.automata.BDD.SupremicaBDDBitVector.SupremicaBDDBitVector;
import org.supremica.automata.BDD.SupremicaBDDBitVector.TCSupremicaBDDBitVector;


/**
 * @author Sajed Miremadi, Zhennan Fei
 */

public abstract class BDDAbstractManager {

    private static Logger logger = LogManager.getLogger(BDDExtendedManager.class);
    protected BDDFactory factory;
//    private BDDDomain constantDomain;
    private Map<String, Integer> variableStringToIndexMap;
    BDDExtendedAutomata bddExAutomata;
    BufferedWriter out = null;

    public SupremicaBDDBitVector createSupremicaBDDBitVector(final int P_TC, final boolean negativesIncluded, final int bitnum) {
        if (P_TC == 0) {
            return new PSupremicaBDDBitVector(getFactory(), bitnum);
        } else if (P_TC == 1) {
            final TCSupremicaBDDBitVector output = new TCSupremicaBDDBitVector(getFactory(), bitnum);
            if (!negativesIncluded) {
                output.setBit(output.length() - 1, getZeroBDD());
            }

            return output;
        }

        throw new IllegalArgumentException("BDDBitVector type not defined!");
    }

    public SupremicaBDDBitVector createSupremicaBDDBitVector(final int P_TC, final int bitnum, final boolean b) {
        if (P_TC == 0) {
            return new PSupremicaBDDBitVector(getFactory(), bitnum, b);
        } else if (P_TC == 1) {
            return new TCSupremicaBDDBitVector(getFactory(), bitnum, b);
        }

        throw new IllegalArgumentException("BDDBitVector type not defined!");
    }

    public SupremicaBDDBitVector createSupremicaBDDBitVector(final int P_TC, final int bitnum, final long val) {
        if (P_TC == 0) {
            return new PSupremicaBDDBitVector(getFactory(), bitnum, val);
        } else if (P_TC == 1) {
            return new TCSupremicaBDDBitVector(getFactory(), bitnum, val);
        }

        throw new IllegalArgumentException("BDDBitVector type not defined!");
    }

    public SupremicaBDDBitVector createSupremicaBDDBitVector(final int P_TC, final boolean negativesIncluded, final BDDDomain domain) {
        if (P_TC == 0) {
            return new PSupremicaBDDBitVector(getFactory(), domain.varNum(), domain);
        } else if (P_TC == 1) {
            final TCSupremicaBDDBitVector output = new TCSupremicaBDDBitVector(getFactory(), domain);
            if (!negativesIncluded) {
                output.setBit(output.length() - 1, factory.ithVar(domain.vars()[0]).not());
            }

            return output;
        }
        throw new IllegalArgumentException("BDDBitVector type not defined!");
    }

    public void setBDDExAutomata(final BDDExtendedAutomata bddExAutomata) {
        this.bddExAutomata = bddExAutomata;
    }

    public BDDFactory getFactory() {
        return factory;
    }

    public void done() {
        if (factory != null) {
            factory.done();
            factory = null;
        }
    }

    public BDD getZeroBDD() {
        return factory.zero();
    }

    public BDD getOneBDD() {
        return factory.one();
    }

    public BDDDomain createDomain(final int size) {
        return factory.extDomain(size);
    }

    public int[] partialReverseVarOrdering(final int[] varOrdering) {
        final int[] reversedVarOrdering = new int[varOrdering.length];
        final int[] updatedVarOrdering = factory.getVarOrder();
        for (int i = 0; i < varOrdering.length; i++) {
            reversedVarOrdering[i] = varOrdering[varOrdering.length - 1 - i];
            updatedVarOrdering[varOrdering[i]] = varOrdering[varOrdering.length - 1 - i];
        }

        factory.setVarOrder(updatedVarOrdering);

        return reversedVarOrdering;

    }

    public void setVariableStringToIndexMap(final Map<String, Integer> v) {
        variableStringToIndexMap = v;
    }

    public BDDVarSet createEmptyVarSet() {
        return factory.emptySet();
    }

    public BDD createBDD(final int index, final BDDDomain domain) {
        return factory.buildCube(index, domain.vars());
    }

    public BDDPairing makePairing(final BDDDomain[] source, final BDDDomain[] dest) {
        final BDDPairing pairing = factory.makePair();
        pairing.set(source, dest);
        return pairing;
    }

    public BDDPairing makePairing(final BDDDomain source, final BDDDomain dest) {
        return factory.makePair(source, dest);
    }

    boolean isOpEqRel(final BinaryOperator op) {
        final CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
        if (op.equals(cot.getEqualsOperator()) || op.equals(cot.getGreaterEqualsOperator()) || op.equals(cot.getLessEqualsOperator())
                || op.equals(cot.getGreaterThanOperator()) || op.equals(cot.getLessThanOperator()) || op.equals(cot.getNotEqualsOperator())) {
            return true;
        } else {
            return false;
        }
    }

    boolean isOpArith(final BinaryOperator op) {
        final CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
        if (op.equals(cot.getPlusOperator()) || op.equals(cot.getMinusOperator()) || op.equals(cot.getTimesOperator())
                || op.equals(cot.getDivideOperator()) || op.equals(cot.getModuloOperator())) {
            return true;
        } else {
            return false;
        }
    }

    public BDD guard2BDD(final SimpleExpressionProxy expr) {
      return guard2BDD(expr, null);
    }

    public BDD guard2BDD(final SimpleExpressionProxy expr,
                         final HashSet<String> updatedVars) {
      final ResultOverflows guardBDD = expr2BDDBitVec(expr, true, updatedVars);
      final BDD result = guardBDD.getResult().getBit(0);
      return result;
    }

    public BDD action2BDD(final BinaryExpressionProxy expr)
	{
		try // to hunt down this bug
		{
			final String leftVarName = ((SimpleIdentifierProxy) expr.getLeft()).getName();
			final int varIndex = bddExAutomata.theIndexMap.getVariableIndexByName(leftVarName);
			final SupremicaBDDBitVector leftSide = bddExAutomata.getBDDBitVecTarget(varIndex);
			ResultOverflows rightSide = null;

			if(bddExAutomata.orgExAutomata.getNonIntegerVarNameSet().contains(leftVarName)) {
			  final Map<String, String> leftVarInstanceIntMap =
			    bddExAutomata.orgExAutomata.getNonIntVar2InstanceIntMap().get(leftVarName);
			  final String rhs = expr.getRight().toString();
			  // case 1: the right hand side can be one of leftVarName's values
			  if (leftVarInstanceIntMap.containsKey(rhs)) {
			    final String mappedInstanceValue = leftVarInstanceIntMap.get(rhs);
			    final IntConstantProxy leftVarMappedIntProxy =
			      new IntConstantSubject(Integer.parseInt(mappedInstanceValue));
			    rightSide = expr2BDDBitVec(leftVarMappedIntProxy, false);
			  }
			  // case 2: the right hand side can be another variable
			  else {
			    rightSide = expr2BDDBitVec(expr.getRight(), false);
			  }
			} else {
			  rightSide = expr2BDDBitVec(expr.getRight(), false);
			}

			BDD overflow = getZeroBDD();
			final CompilerOperatorTable cot = CompilerOperatorTable.getInstance();
			if (expr.getOperator().equals(cot.getIncrementOperator())) {
				overflow = rightSide.getOverflows().id();
				rightSide = bddExAutomata.getBDDBitVecSource(varIndex).addConsideringOverflows(rightSide.getResult());
			}

			if (expr.getOperator().equals(cot.getDecrementOperator())) {
				overflow = rightSide.getOverflows().id();
				rightSide = bddExAutomata.getBDDBitVecSource(varIndex).subConsideringOverflows(rightSide.getResult());
			}

			return leftSide.equ(rightSide.getResult()).and((rightSide.getOverflows().or(overflow)).not());
		}
		catch(final Exception excp)
		{
			logger.debug("Exception! expr is " + expr.toString());
			throw excp;
		}
    }

    ResultOverflows expr2BDDBitVec(final SimpleExpressionProxy expr,
                                   final boolean guardAction) {
        return expr2BDDBitVec(expr, guardAction, null);
    }

    /**
     * Recursively traverses the expression tree. Every operator and identifier
     * in the tree is converted to an equivalent BDD bit vector.
     * <p>
     * A BDD bit vector is a bit vector representation of a number where each
     * bit is represented by a BDD instead of logical 1 and 0. This way it is
     * possible to do integer arithmetic with BDDs. See Clarke et. al. (1993)
     * for a bit more information.
     * <p>
     * C. M. Clarke et. al. "Spectral transforms for large boolean functions
     * with applications to technology mapping" 30th ACM/IEEE Design Automation
     * Conference, 1993
     *
     * @param expr The root node of an expression tree.
     * @param guardAction True if the expression is a guard.
     * @param updatedVariables The names of all variables that are updated by
     * the next operator are added to this set.
     * @return A BDD bit vector that represents the complete expression tree.
     */
    ResultOverflows expr2BDDBitVec(final SimpleExpressionProxy expr,
                                   final boolean guardAction,
                                   final HashSet<String> updatedVariables) {

      Expression2BDDBitVectorVisitor visitor;
      if (guardAction) {
        visitor = new GuardExpression2BDDVisitor(guardAction, updatedVariables);
        try {
          final BDDOverflows bo = (BDDOverflows) expr.acceptVisitor(visitor);
          final SupremicaBDDBitVector tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, 1, 0);
          tmp.setBit(0, bo.getResult());
          return new ResultOverflows(tmp, bo.getOverflows());
        } catch (final VisitorException ve) {
          throw ve.getRuntimeException();
        }
      } else {
        visitor = new ArithmeticExpression2BDDVisitor(guardAction, updatedVariables);
        try {
          return (ResultOverflows) expr.acceptVisitor(visitor);
        } catch (final VisitorException ve) {
          throw ve.getRuntimeException();
        }
      }
    }

    private boolean isAutVar(final String str)
    {
      // check if the variable with the suffix "_curr" is the
      // variable for an automaton or just a regular variable
      boolean autVar = false;
      if (str.contains(bddExAutomata.getLocVarSuffix())) {
        final ExtendedAutomata exAut = bddExAutomata.getExtendedAutomata();
        final Set<String> autNameSet = exAut.getStringToExAutomaton().keySet();
        for (final String name : autNameSet) {
          if (str.contains(name)) {
            autVar = true;
            break;
          }
        }
      }
      return autVar;
    }

    public void addLocation(final BDD bdd, final int locationIndex, final BDDDomain domain) {
        final BDD newLocationBDD = factory.buildCube(locationIndex, domain.vars());
        bdd.orWith(newLocationBDD);
    }

    public void addLocationInvariant(final BDD bdd, final BDD invariant, final int locationIndex, final BDDDomain domain) {
        final BDD newLocationBDD = factory.buildCube(locationIndex, domain.vars());
        bdd.orWith(newLocationBDD.and(invariant));
    }

    // Quite big method, however, encapsulation has been considered well :)
    public void addEdge(final EdgeProxy theEdge,
                        final BDD forwardEdgesBDD,
                        final BDD forwardEdgesWithoutDestBDD,
                        final BDD[] transWhereVisUpdated,
                        final BDD[] transAndNextValsForV,
                        final int sourceLocationIndex,
                        final BDDDomain sourceDomain,
                        final int destLocationIndex,
                        final BDDDomain destDomain,
                        final int eventIndex,
                        final BDDDomain eventDomain,
                        final List<SimpleExpressionProxy> guards,
                        final List<BinaryExpressionProxy> actions) {
        BDD sourceBDD = getOneBDD();
        BDD destBDD = getOneBDD();

        // Create a BDD representing the source location
        sourceBDD = factory.buildCube(sourceLocationIndex, sourceDomain.vars());

        // Create a BDD representing the dest location
        destBDD = factory.buildCube(destLocationIndex, destDomain.vars());

        // Create a BDD representing the event
        final BDD eventBDD = factory.buildCube(eventIndex, eventDomain.vars());

        sourceBDD = sourceBDD.and(eventBDD);
        final BDD sourceEventBDD = sourceBDD.id();

        sourceBDD = sourceBDD.and(destBDD);

        // keep track of variables that are updated either by actions or
        // the next operator appearing in guards
        final HashSet<String> updatedVars = new HashSet<String>();

        BDD guardBDD = factory.one();
        if (guards != null && guards.size() > 0) {
            //Only the first guard should be considered. If there are more, it will just affect the GUI.
            guardBDD = guard2BDD(guards.get(0), updatedVars);
        }

        //Add guard to event and source and dest state
        sourceBDD = sourceBDD.and(guardBDD);

        if (theEdge != null) {
          final String eventName =
            bddExAutomata.getIndexMap().getEventAt(eventIndex).getName();
          final Map<EdgeProxy, BDD> eventEdge2BDDMap =
            bddExAutomata.getEventName2EdgeBDDMap().get(eventName);
          final BDD edgeSourceBDD = sourceBDD
            .exist(bddExAutomata.getDestStatesVarSet())
            .exist(bddExAutomata.getEventVarSet());
          if (!edgeSourceBDD.isZero())
            eventEdge2BDDMap.put(theEdge, edgeSourceBDD);
        }

        forwardEdgesWithoutDestBDD.orWith(sourceEventBDD.and(guardBDD));

        BDD actionBDD = factory.one();
        if (actions != null) {
            for (final SimpleExpressionProxy a : actions) {
                final BinaryExpressionProxy bep = (BinaryExpressionProxy) a;
                updatedVars.add(((SimpleIdentifierProxy) bep.getLeft()).getName());

                actionBDD = actionBDD.and(action2BDD(bep));
            }
        }
//        actionBDD.printDot();

        for (final String varName : updatedVars) {
            final int variableIndex = variableStringToIndexMap.get(varName);

//            bddExAutomata.allForwardTransWhereVisUpdated[variableIndex] = bddExAutomata.allForwardTransWhereVisUpdated[variableIndex].or(sourceBDD);
            transWhereVisUpdated[variableIndex] = transWhereVisUpdated[variableIndex].or(sourceBDD);
            transAndNextValsForV[variableIndex] = transAndNextValsForV[variableIndex].or(sourceBDD.and(actionBDD));
        }

        // Add the transition to the set of existing transitions
        forwardEdgesBDD.orWith(sourceBDD);

    }


    /*
    We assume the the most significant bit is at the top of the variable ordering.
    'bdd' represents the values for one single clock.
     */
//    public int getMinimalValue(BDD bdd)
//    {
//        int minValue = 0;
//
//        String currentNameOfClock = bddExAutomata.getAutVarName(bdd.var());
//        int minBDDVarContent = bddExAutomata.getMinSourceBDDVar(currentNameOfClock);// getMin(bddExAutomata.getSourceVariableDomain(nameOfClock).vars());
//
//        BDD iterateBDD = bdd.id();
//        while(!iterateBDD.isOne())
//        {
//            BDD lowChild = iterateBDD.low();
//            BDD nextIterateBDD = lowChild.id();
//
//            if(lowChild.isZero())
//            {
//                minValue = minValue + (int)Math.pow(2, iterateBDD.var() - minBDDVarContent);
//                nextIterateBDD = iterateBDD.high().id();
//            }
//
//            iterateBDD = nextIterateBDD.id();
//        }
//
//        return minValue;
//    }
//
//    public int getMaximalValue(BDD bdd)
//    {
//        int maxValue = 0;
//
//        String currentNameOfClock = bddExAutomata.getAutVarName(bdd.var());
//        int minBDDVarContent = bddExAutomata.getMinSourceBDDVar(currentNameOfClock);
//
//        BDD iterateBDD = bdd.id();
//
//        int previousVar = bdd.var();
//        int currentVar = bdd.var();
//        while(!iterateBDD.isOne())
//        {
//            currentVar = iterateBDD.var();
//            BDD highChild = iterateBDD.high();
//
//            if(highChild.isZero())
//            {
//                iterateBDD = iterateBDD.low().id();
//                continue;
//            }
//
//            int missingValues = 0;
//            for(int m = previousVar - 1 ; m > currentVar; m--)
//                missingValues = missingValues + (int)Math.pow(2, m - minBDDVarContent);
//
//            maxValue = missingValues +  maxValue + (int)Math.pow(2, iterateBDD.var() - minBDDVarContent);
//
//            previousVar = iterateBDD.var();
//            iterateBDD = highChild.id();
//        }
//
//        for(int m = previousVar - 1 ; m >= minBDDVarContent; m--)
//            maxValue = maxValue + (int)Math.pow(2, m - minBDDVarContent);
//
//        return maxValue;
//    }
//    public BDD extendClock(BDD clock, boolean forwardBackward)
//    {
//        if(clock.isOne())
//            return getOneBDD();
//
//        if(clock.isZero())
//            return getZeroBDD();
//
//        int clockIndex = bddExAutomata.theIndexMap.getVariableIndexByName(bddExAutomata.getAutVarName(clock.var()));
//
//        int clockValue = forwardBackward ? getMinimalValue(clock) : getMaximalValue(clock);
//
//        SupremicaBDDBitVector clockValueBDDVec = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory,
//                bddExAutomata.getSourceVariableDomain(clockIndex).size().intValue(), clockValue);
//        SupremicaBDDBitVector clockBDDVec = bddExAutomata.getBDDBitVecSource(clockIndex);
//
//        return forwardBackward ? clockBDDVec.gte(clockValueBDDVec) : clockBDDVec.lte(clockValueBDDVec);
//    }
//
//    public BDD extendClocks(BDD clocks, boolean forwardBackward)
//    {
//        BDD extendedClocks = getOneBDD();
//        for(VariableComponentProxy clock:bddExAutomata.orgExAutomata.getClocks())
//        {
//            BDD clockBDD =  clocks.exist(bddExAutomata.MinusVarSet(
//                    bddExAutomata.getSourceClockVarSet(), bddExAutomata.getSourceVariableDomain(bddExAutomata.getIndexMap().getVariableIndex(clock)).set()));
//
//            extendedClocks = extendedClocks.and(extendClock(clockBDD, forwardBackward));
//        }
//
//        return extendedClocks;
//    }
    BDD image_preImage(final BDD states, final BDD transitions, final BDD clocks) {
        BDD nextStates = transitions.relprod(states.id(), bddExAutomata.getSourceStatesVarSet());
        if (!bddExAutomata.orgExAutomata.getClocks().isEmpty() && !clocks.isZero()) {
            nextStates = timeEvolDest(nextStates, clocks);
        }
        nextStates.replaceWith(bddExAutomata.getDestToSourceLocationPairing());
        nextStates.replaceWith(bddExAutomata.getDestToSourceVariablePairing());
        return nextStates;
    }

    BDD image_preImage(final BDD states, final BDD transitions) {
        final BDD nextStates = transitions.relprod(states.id(), bddExAutomata.getSourceStatesVarSet());

        nextStates.replaceWith(bddExAutomata.getDestToSourceLocationPairing());
        nextStates.replaceWith(bddExAutomata.getDestToSourceVariablePairing());

        return nextStates;
    }


    BDD timeEvolDest(final BDD states, final BDD clocksEvol) {
        BDD output = states.id();
        output = output.and(clocksEvol);
        output = output.exist(bddExAutomata.getDestClockVarSet());
        output = output.replace(bddExAutomata.tempClock1ToDestClockPairing).exist(bddExAutomata.tempClock1Varset);
        return output.and(bddExAutomata.getDestLocationInvariants());
    }

    BDD timeEvolSource(final BDD states, final BDD clocksEvol) {
        BDD output = states.id();
        output.replaceWith(bddExAutomata.getSourceToDestLocationPairing());
        output.replaceWith(bddExAutomata.getSourceToDestVariablePairing());

        output = timeEvolDest(output, clocksEvol);

        output.replaceWith(bddExAutomata.getDestToSourceLocationPairing());
        output.replaceWith(bddExAutomata.getDestToSourceVariablePairing());
        return output;
    }

    private abstract class Expression2BDDBitVectorVisitor
      extends DefaultModuleProxyVisitor
    {

      boolean guardAction;
      HashSet<String> updatedVariables;
      final CompilerOperatorTable operatorTable = CompilerOperatorTable.getInstance();

      Expression2BDDBitVectorVisitor(
                                     final boolean guardAction,
                                     final HashSet<String> updatedVariables) {
        this.guardAction = guardAction;
        this.updatedVariables = updatedVariables;
      }

      public abstract Expression2BDDBitVectorVisitor buildVisitor(final boolean guardAction, final HashSet<String> updatedVariables);

      public abstract Object createOverflows(final SupremicaBDDBitVector vector, final BDD overflows);

      @Override
      public Object visitBinaryExpressionProxy(final BinaryExpressionProxy binExpr)
        throws VisitorException
      {
        final BinaryOperator operator = binExpr.getOperator();
        throw new IllegalArgumentException(binExpr + ":" + operator + " is not known!");
      }

      @Override
      public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy proxy)
        throws VisitorException
      {
        final String varNameOrInstValue = proxy.getName();
        if (bddExAutomata.orgExAutomata.getVariableIdentifiers().contains(varNameOrInstValue)) {
          final Map<String, Integer> varToIndexMap = bddExAutomata.theIndexMap.variableStringToIndexMap;
          final Integer index = varToIndexMap.get(varNameOrInstValue);
          return createOverflows(bddExAutomata.getBDDBitVecSource(index), getZeroBDD());
        } else if (bddExAutomata.orgExAutomata.getNamedConstantIdentifiers().contains(varNameOrInstValue)) {
          final ExpressionProxy constantExpr = bddExAutomata.orgExAutomata.getNamedConstants().get(varNameOrInstValue).getExpression();
          final Expression2BDDBitVectorVisitor visitor = buildVisitor(guardAction, updatedVariables);
          return constantExpr.acceptVisitor(visitor);
          //return expr2BDDBitVec((SimpleExpressionProxy) constantExpr, guardAction, updatedVariables);
        } else {
          throw new IllegalArgumentException(varNameOrInstValue + " is not a known identifier.");
        }
      }

      @Override
      public Object visitUnaryExpressionProxy(final UnaryExpressionProxy unExpr)
        throws VisitorException
      {
        final UnaryOperator operator = unExpr.getOperator();

        if (operator.equals(operatorTable.getNextOperator())) {

          final String primedVarName = unExpr.getSubTerm().toString();
          if (updatedVariables != null) {
            updatedVariables.add(primedVarName);
          }
          final Map<String, Integer> varToIndexMap = bddExAutomata.theIndexMap.variableStringToIndexMap;
          final Integer index = varToIndexMap.get(primedVarName);
          final SupremicaBDDBitVector leftSide = bddExAutomata.getBDDBitVecTarget(index);
          return createOverflows(leftSide, getZeroBDD());

        } else {

          throw new IllegalArgumentException(unExpr + ":" + operator + " is not known!");
        }
      }

    }

    private class GuardExpression2BDDVisitor
      extends Expression2BDDBitVectorVisitor {

      GuardExpression2BDDVisitor(final boolean guardAction,
                                 final HashSet<String> updatedVariables) {
        super(guardAction, updatedVariables);
      }

      @Override
      public BDDOverflows visitBinaryExpressionProxy(final BinaryExpressionProxy binExpr)
        throws VisitorException
      {
        final BinaryOperator operator = binExpr.getOperator();

        if (operator.equals(operatorTable.getAndOperator())) {

            final Expression2BDDBitVectorVisitor leftVisitor = new GuardExpression2BDDVisitor(true, updatedVariables);
            final Expression2BDDBitVectorVisitor rightVisitor = new GuardExpression2BDDVisitor(true, updatedVariables);
            final BDDOverflows roLeft = (BDDOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
            final BDDOverflows roRight = (BDDOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
            //final ResultOverflows roLeft = expr2BDDBitVec(binExpr.getLeft(), true, updatedVariables);
            //final ResultOverflows roRight = expr2BDDBitVec(binExpr.getRight(), true, updatedVariables);
            BDD tmp = roLeft.getResult().id();
            final BDD rightGuard = roRight.getResult();
            tmp = tmp.and(rightGuard);
            //tmp.setBit(0, tmp.getBit(0).and(rightGuard.getBit(0)));
            return new BDDOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));

        } else if (operator.equals(operatorTable.getOrOperator())) {

            final Expression2BDDBitVectorVisitor leftVisitor = new GuardExpression2BDDVisitor(true, updatedVariables);
            final Expression2BDDBitVectorVisitor rightVisitor = new GuardExpression2BDDVisitor(true, updatedVariables);
            final BDDOverflows roLeft = (BDDOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
            final BDDOverflows roRight = (BDDOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
            //final ResultOverflows roLeft = expr2BDDBitVec(binExpr.getLeft(), true, updatedVariables);
            //final ResultOverflows roRight = expr2BDDBitVec(binExpr.getRight(), true, updatedVariables);
            BDD tmp = roLeft.getResult().id();
            final BDD rightGuard = roRight.getResult();
            tmp = tmp.or(rightGuard);
            //tmp.setBit(0, tmp.getBit(0).or(rightGuard.getBit(0)));
            return new BDDOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));

        } else if (operator.equals(operatorTable.getEqualsOperator())) {

          BDD tmp = null;
          BDD leftOverflows = getZeroBDD();
          BDD rightOverflows = getZeroBDD();
          if (isAutVar(binExpr.getLeft().toString())) {
              final String leftString = binExpr.getLeft().toString();
              final String locName = binExpr.getRight().toString();
              final String autName = leftString.substring(0, leftString.indexOf(bddExAutomata.getLocVarSuffix()));
              //tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
              //                                  false,
              //                                  bddExAutomata.getSourceLocationDomain(autName));
              tmp = createBDD(bddExAutomata.getIndexMap().getLocationIndex(autName, locName),
                      bddExAutomata.getSourceLocationDomain(autName));
              //tmp.setBit(0, locBDD);
          }
          else {
              final SimpleExpressionProxy left = binExpr.getLeft();
              final SimpleExpressionProxy right = binExpr.getRight();
              final Set<String> nonIntegerVarNameSet = bddExAutomata.orgExAutomata.getNonIntegerVarNameSet();
              final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
              final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
              final ResultOverflows roLeft = (ResultOverflows) left.acceptVisitor(leftVisitor);
              //final ResultOverflows roLeft = expr2BDDBitVec(left, false, updatedVariables);
              ResultOverflows roRight = null;

              String leftVarName = null;
              if (left instanceof UnaryExpressionProxy) {
                final UnaryExpressionProxy unExpr = (UnaryExpressionProxy) left;
                if (unExpr.getOperator().equals(operatorTable.getNextOperator())) {
                  leftVarName = unExpr.getSubTerm().toString();
                }
              } else {
                leftVarName = left.toString();
              }

              if (nonIntegerVarNameSet.contains(leftVarName) &&
                  !nonIntegerVarNameSet.contains(right.toString())) {
                final Map<String, String> var2InstIntMap =
                  bddExAutomata.orgExAutomata.getNonIntVar2InstanceIntMap().get(leftVarName);
                IntConstantProxy mappedIntProxy = null;
                if (var2InstIntMap.containsKey(right.toString())) {
                  mappedIntProxy = new IntConstantSubject(Integer.parseInt(var2InstIntMap.get(right.toString())));
                } else {
                  mappedIntProxy = new IntConstantSubject(Integer.parseInt(right.toString()));
                }
                roRight = (ResultOverflows) mappedIntProxy.acceptVisitor(rightVisitor);
                //roRight = expr2BDDBitVec(mappedIntProxy, false, updatedVariables);
              } else {
                roRight = (ResultOverflows) right.acceptVisitor(rightVisitor);
                //roRight = expr2BDDBitVec(binExpr.getRight(), false, updatedVariables);
              }
              tmp = roLeft.getResult().equ(roRight.getResult());
              //tmp = roLeft.getResult().copy();
              //tmp.setBit(0, tmp.equ(roRight.getResult()));
              leftOverflows = roLeft.getOverflows();
              rightOverflows = roRight.getOverflows();
          }
          return new BDDOverflows(tmp, leftOverflows.or(rightOverflows));

      } else if (operator.equals(operatorTable.getNotEqualsOperator())) {

          BDD tmp = null;
          BDD leftOverflows = getZeroBDD();
          BDD rightOverflows = getZeroBDD();
          if (isAutVar(binExpr.getLeft().toString())) {
              final String leftString = binExpr.getLeft().toString();
              final String locName = binExpr.getRight().toString();
              final String autName = leftString.substring(0, leftString.indexOf(bddExAutomata.getLocVarSuffix()));
              //tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
              //                                  false,
              //                                  bddExAutomata.getSourceLocationDomain(autName));
              tmp = createBDD(bddExAutomata.getIndexMap().getLocationIndex(autName, locName),
                      bddExAutomata.getSourceLocationDomain(autName)).not();
              //tmp.setBit(0, locBDD);
          } else {
              final SimpleExpressionProxy left = binExpr.getLeft();
              final SimpleExpressionProxy right = binExpr.getRight();
              final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
              final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
              final Set<String> nonIntegerVarNameSet = bddExAutomata.orgExAutomata.getNonIntegerVarNameSet();
              final ResultOverflows roLeft = (ResultOverflows) left.acceptVisitor(leftVisitor);
              //final ResultOverflows roLeft = expr2BDDBitVec(left, false, updatedVariables);
              ResultOverflows roRight = null;
              if (nonIntegerVarNameSet.contains(left.toString()) &&
                  !nonIntegerVarNameSet.contains(right.toString())) {
                final Map<String, String> var2InstIntMap =
                  bddExAutomata.orgExAutomata.getNonIntVar2InstanceIntMap().get(left.toString());
                IntConstantProxy mappedIntProxy = null;
                if (var2InstIntMap.containsKey(right.toString())) {
                  mappedIntProxy = new IntConstantSubject(Integer.parseInt(var2InstIntMap.get(right.toString())));
                } else {
                  mappedIntProxy = new IntConstantSubject(Integer.parseInt(right.toString()));
                }
                roRight = (ResultOverflows) mappedIntProxy.acceptVisitor(rightVisitor);
                //roRight = expr2BDDBitVec(mappedIntProxy, false, updatedVariables);
              } else {
                roRight = (ResultOverflows) right.acceptVisitor(rightVisitor);
                //roRight = expr2BDDBitVec(binExpr.getRight(), false, updatedVariables);
              }
              tmp = roLeft.getResult().neq(roRight.getResult());
              //tmp = roLeft.getResult().copy();
              //tmp.setBit(0, tmp.neq(roRight.getResult()));
              leftOverflows = roLeft.getOverflows();
              rightOverflows = roRight.getOverflows();
          }
          return new BDDOverflows(tmp, leftOverflows.or(rightOverflows));

      } else if (operator.equals(operatorTable.getGreaterThanOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ResultOverflows roLeft = (ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
          final ResultOverflows roRight = (ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
          //final ResultOverflows roLeft = expr2BDDBitVec(binExpr.getLeft(), false, updatedVariables);
          //final ResultOverflows roRight = expr2BDDBitVec(binExpr.getRight(), false, updatedVariables);
          final BDD tmp = roLeft.getResult().gth(roRight.getResult());
          //final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
          //tmp.setBit(0, tmp.gth(roRight.getResult()));
          return new BDDOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));

      } else if (operator.equals(operatorTable.getGreaterEqualsOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ResultOverflows roLeft = (ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
          final ResultOverflows roRight = (ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
          //final ResultOverflows roLeft = expr2BDDBitVec(binExpr.getLeft(), false, updatedVariables);
          //final ResultOverflows roRight = expr2BDDBitVec(binExpr.getRight(), false, updatedVariables);
          final BDD tmp = roLeft.getResult().gte(roRight.getResult());
          //final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
          //tmp.setBit(0, tmp.gte(roRight.getResult()));
          return new BDDOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));

      } else if (operator.equals(operatorTable.getLessThanOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ResultOverflows roLeft = (ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
          final ResultOverflows roRight = (ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
          //final ResultOverflows roLeft = expr2BDDBitVec(binExpr.getLeft(), false, updatedVariables);
          //final ResultOverflows roRight = expr2BDDBitVec(binExpr.getRight(), false, updatedVariables);
          final BDD tmp = roLeft.getResult().lth(roRight.getResult());
          //final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
          //tmp.setBit(0, tmp.lth(roRight.getResult()));
          return new BDDOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));

      } else if (operator.equals(operatorTable.getLessEqualsOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ResultOverflows roLeft = (ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
          final ResultOverflows roRight = (ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
          //final ResultOverflows roLeft = expr2BDDBitVec(binExpr.getLeft(), false, updatedVariables);
          //final ResultOverflows roRight = expr2BDDBitVec(binExpr.getRight(), false, updatedVariables);
          final BDD tmp = roLeft.getResult().lte(roRight.getResult());
          //final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
          //tmp.setBit(0, tmp.lte(roRight.getResult()));
          return new BDDOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));

      } else {
          return (BDDOverflows) super.visitBinaryExpressionProxy(binExpr);
        }
      }

      @Override
      public BDDOverflows visitIntConstantProxy(final IntConstantProxy proxy)
        throws VisitorException
      {

        BDD tmp = null;

        if (proxy.getValue() == 0) {
          tmp = getZeroBDD();
        } else {
          tmp = getOneBDD();
        }

        return new BDDOverflows(tmp, getZeroBDD());
      }

      @Override
      public BDDOverflows visitUnaryExpressionProxy(final UnaryExpressionProxy unExpr)
        throws VisitorException
      {
        final UnaryOperator operator = unExpr.getOperator();

        if (operator.equals(operatorTable.getNotOperator())) {

          final GuardExpression2BDDVisitor visitor = new GuardExpression2BDDVisitor(true, updatedVariables);
          final BDDOverflows ro = (BDDOverflows) unExpr.getSubTerm().acceptVisitor(visitor);
          //final ResultOverflows ro = expr2BDDBitVec(unExpr.getSubTerm(), true, updatedVariables);
          final BDD tmp = ro.getResult().not();
          //final SupremicaBDDBitVector tmp = ro.getResult().copy();
          //tmp.setBit(0, tmp.getBit(0).not());
          return new BDDOverflows(tmp, ro.getOverflows());

        } else {
          return (BDDOverflows) super.visitUnaryExpressionProxy(unExpr);
        }
      }

      @Override
      public Expression2BDDBitVectorVisitor buildVisitor(final boolean guardAction,
                                                         final HashSet<String> updatedVariables)
      {
        return new GuardExpression2BDDVisitor(guardAction, updatedVariables);
      }

      @Override
      public BDDOverflows createOverflows(final SupremicaBDDBitVector vector,
                                    final BDD overflows)
      {
        return new BDDOverflows(vector.getBit(0), overflows);
      }
    }

    private class ArithmeticExpression2BDDVisitor
      extends Expression2BDDBitVectorVisitor {

      ArithmeticExpression2BDDVisitor(final boolean guardAction,
                                 final HashSet<String> updatedVariables) {
        super(guardAction, updatedVariables);
      }

      @Override
      public ResultOverflows visitBinaryExpressionProxy(final BinaryExpressionProxy binExpr)
        throws VisitorException
      {
        final BinaryOperator operator = binExpr.getOperator();

        if (operator.equals(operatorTable.getModuloOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ResultOverflows roLeft = (ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
          final ResultOverflows roRight = (ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
          //final ResultOverflows roLeft = expr2BDDBitVec(binExpr.getLeft(), false, updatedVariables);
          //final ResultOverflows roRight = expr2BDDBitVec(binExpr.getRight(), false, updatedVariables);
          final SupremicaBDDBitVector v2 = roRight.getResult();
          if (v2.isConst()) {
            return new ResultOverflows(roLeft.getResult().divmod(v2.val(), false).optimizeSize(), roLeft.getOverflows().or(roRight.getOverflows()));
          } else {
            throw new IllegalArgumentException("Divisor is not constant");
          }

        } else if (operator.equals(operatorTable.getMinusOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ResultOverflows roLeft = (ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
          final ResultOverflows roRight = (ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
          //final ResultOverflows roLeft = expr2BDDBitVec(binExpr.getLeft(), false, updatedVariables);
          //final ResultOverflows roRight = expr2BDDBitVec(binExpr.getRight(), false, updatedVariables);
          final int m = Math.max(roLeft.getResult().length(), roRight.getResult().length()) + 1;
          final SupremicaBDDBitVector left = roLeft.getResult().resize(m);
          final ResultOverflows ro = left.subConsideringOverflows(roRight.getResult());
          return new ResultOverflows(ro.getResult().optimizeSize(), ro.getOverflows().or(roLeft.getOverflows().or(roRight.getOverflows())));

        } else if (operator.equals(operatorTable.getPlusOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ResultOverflows roLeft = (ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
          final ResultOverflows roRight = (ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
          //final ResultOverflows roLeft = expr2BDDBitVec(binExpr.getLeft(), false, updatedVariables);
          //final ResultOverflows roRight = expr2BDDBitVec(binExpr.getRight(), false, updatedVariables);
          final int m = Math.max(roLeft.getResult().length(), roRight.getResult().length()) + 1;
          final SupremicaBDDBitVector left = roLeft.getResult().resize(m);
          final ResultOverflows ro = left.addConsideringOverflows(roRight.getResult());
          return new ResultOverflows(ro.getResult().optimizeSize(), ro.getOverflows().or(roLeft.getOverflows().or(roRight.getOverflows())));

        } else if (operator.equals(operatorTable.getDivideOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final SupremicaBDDBitVector v2 = ((ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor)).getResult().copy();
          //final SupremicaBDDBitVector v2 = expr2BDDBitVec(binExpr.getRight(), false, updatedVariables).getResult().copy();
          if (v2.isConst()) {
            final SupremicaBDDBitVector left = ((ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor)).getResult();
            //final SupremicaBDDBitVector left = expr2BDDBitVec(binExpr.getLeft(), false, updatedVariables).getResult();
            final SupremicaBDDBitVector res = left.divmod(v2.val(), true).optimizeSize();
            return new ResultOverflows(res, getZeroBDD());
          } else {
            throw new IllegalArgumentException("Divisor is not constant!");
          }

        } else if (operator.equals(operatorTable.getTimesOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
          final SupremicaBDDBitVector v2 = ((ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor)).getResult().copy();
          //final SupremicaBDDBitVector v2 = expr2BDDBitVec(binExpr.getRight(), false, updatedVariables).getResult().copy();
          if (v2.isConst()) {
            SupremicaBDDBitVector left = ((ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor)).getResult();
            //SupremicaBDDBitVector left = expr2BDDBitVec(binExpr.getLeft(), false, updatedVariables).getResult();
            left = left.resize(left.length() + v2.length() + 1);
            final SupremicaBDDBitVector res = left.mulfixed(v2.val()).optimizeSize();
            return new ResultOverflows(res, getZeroBDD());
          } else {
            throw new IllegalArgumentException("Factor is not constant!");
          }

        } else {
          return (ResultOverflows) super.visitBinaryExpressionProxy(binExpr);
        }
      }

      @Override
      public ResultOverflows visitIntConstantProxy(final IntConstantProxy proxy)
        throws VisitorException
      {

        final int value = proxy.getValue();
        return new ResultOverflows(
                                   createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                                                               ((int) Math.pow(2, (int) Math.ceil(Math.log(Math.abs(value) + 1) / Math.log(2)) + 1)),
                                                               value),
                                   getZeroBDD());

      }

      @Override
      public ResultOverflows visitUnaryExpressionProxy(final UnaryExpressionProxy unExpr)
        throws VisitorException
      {
        final UnaryOperator operator = unExpr.getOperator();

        if (operator.equals(operatorTable.getUnaryMinusOperator())) {

            final ArithmeticExpression2BDDVisitor visitor = new ArithmeticExpression2BDDVisitor(false, updatedVariables);
            final ResultOverflows ro = (ResultOverflows) unExpr.getSubTerm().acceptVisitor(visitor);
            //final ResultOverflows ro = expr2BDDBitVec(unExpr.getSubTerm(), true, updatedVariables);
            // TODO: toTwosComplement might result in an overflow if the
            //       bit vector stores the minimum.
            //       For instance, with 3 bits the minimum is -4. Twos
            //       complement is 3+1, but that wraps around to -4.
            return new ResultOverflows(((TCSupremicaBDDBitVector) ro.getResult()).toTwosComplement(), ro.getOverflows());
        }
        else {
            return (ResultOverflows) super.visitUnaryExpressionProxy(unExpr);
        }
      }

      @Override
      public Expression2BDDBitVectorVisitor buildVisitor(final boolean guardAction,
                                                         final HashSet<String> updatedVariables)
      {
        return new ArithmeticExpression2BDDVisitor(guardAction, updatedVariables);
      }

      @Override
      public Object createOverflows(final SupremicaBDDBitVector vector,
                                    final BDD overflows)
      {
        return new ResultOverflows(vector, overflows);
      }



    }
}
