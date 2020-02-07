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
        // This expression is expected to return a boolean value.
        // Use the Guard visitor to parse the root node.
        visitor = new GuardExpression2BDDVisitor(updatedVariables);
        try {
          final BDDOverflows bo = (BDDOverflows) expr.acceptVisitor(visitor);
          final SupremicaBDDBitVector tmp =
            createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, 1, 0);
          tmp.setBit(0, bo.getResult());
          return new ResultOverflows(tmp, bo.getOverflows());
        } catch (final VisitorException ve) {
          throw ve.getRuntimeException();
        }
      } else {
        // The expression is expected to return an integer value.
        // Use the Arithmetic visitor to parse the root node.
        visitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
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

    /**
     * Parent inner class for parsing guard/action expressions into BDDs.
     * The parsing uses the visitor design pattern.
     * @author jonkro
     */
    private abstract class Expression2BDDBitVectorVisitor
      extends DefaultModuleProxyVisitor
    {

      HashSet<String> updatedVariables;
      final CompilerOperatorTable operatorTable = CompilerOperatorTable.getInstance();

      /**
       * Construct a visitor instance.
       * @param updatedVariables A set in which variables with the next
       * operator applied to them in the expression are added.
       */
      Expression2BDDBitVectorVisitor(
                                     final HashSet<String> updatedVariables) {
        this.updatedVariables = updatedVariables;
      }

      /**
       * Instantiate an instance of the class.
       * @param updatedVariables Next variables in the expression.
       * @return One of the sub classes.
       */
      public abstract Expression2BDDBitVectorVisitor buildVisitor(final HashSet<String> updatedVariables);

      /**
       * Returns an object with a boolean BDD or a BDD bit vector and an
       * overflow BDD, depending on class.
       * @param vector The result from sub-expression parsing.
       * @param overflows Overflows from sub-expression parsing.
       * @return A pair of the result and the overflows.
       */
      public abstract Object createOverflows(final SupremicaBDDBitVector vector, final BDD overflows);

      @Override
      public Object visitBinaryExpressionProxy(final BinaryExpressionProxy binExpr)
        throws VisitorException
      {
        final BinaryOperator operator = binExpr.getOperator();
        throw new IllegalArgumentException(binExpr + ":" + operator + " is not known!");
      }

      /**
       * A simple identifier shall be parsed. A simple identifier can be either
       * a variable identifier, i.e., a variable name, or it can be a named
       * constant identifier.
       * @param proxy The (sub-)expression to parse.
       * @return BDDOverflows or ResultOverflows depending on class.
       */
      @Override
      public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy proxy)
        throws VisitorException
      {
        final String varNameOrInstValue = proxy.getName();
        if (bddExAutomata.orgExAutomata.getVariableIdentifiers().contains(varNameOrInstValue)) {
          // We have a variable identifier, so we must get its BDD
          // representation.
          final Map<String, Integer> varToIndexMap = bddExAutomata.theIndexMap.variableStringToIndexMap;
          final Integer index = varToIndexMap.get(varNameOrInstValue);
          // Pack the variable representation. Depending on class, the result
          // must be integer or boolean, so use the appropriate method in the
          // sub class.
          return createOverflows(bddExAutomata.getBDDBitVecSource(index), getZeroBDD());

        } else if (bddExAutomata.orgExAutomata.getNamedConstantIdentifiers().contains(varNameOrInstValue)) {
          // We have a named constant. Get its expression and continue to parse
          // it.
          final ExpressionProxy constantExpr = bddExAutomata.orgExAutomata.getNamedConstants().get(varNameOrInstValue).getExpression();
          // The type of the result (integer or boolean) depends on the current
          // class, so let the sub class determine which visitor to use.
          final Expression2BDDBitVectorVisitor visitor = buildVisitor(updatedVariables);
          return constantExpr.acceptVisitor(visitor);

        } else {
          // These identifiers we cannot handle.
          throw new IllegalArgumentException(varNameOrInstValue + " is not a known identifier.");
        }
      }

      /**
       * A unary expression shall be parsed. This superclass handles the next
       * operator since the code is almost the same regardless of whether the
       * result is expected to be boolean or integer valued.
       * @param unExpr the expression to parse.
       * @return BDDOverflows or ResultOverflows depending on class.
       */
      @Override
      public Object visitUnaryExpressionProxy(final UnaryExpressionProxy unExpr)
        throws VisitorException
      {
        final UnaryOperator operator = unExpr.getOperator();

        if (operator.equals(operatorTable.getNextOperator())) {

          // The next operator is applied to variable names, so extract the
          // variable name.
          final String primedVarName = unExpr.getSubTerm().toString();
          if (updatedVariables != null) {
            updatedVariables.add(primedVarName);
          }
          final Map<String, Integer> varToIndexMap = bddExAutomata.theIndexMap.variableStringToIndexMap;
          final Integer index = varToIndexMap.get(primedVarName);
          // Get the BDD representation of the variable.
          final SupremicaBDDBitVector leftSide = bddExAutomata.getBDDBitVecTarget(index);
          // Pack the variable representation. Depending on class, the result
          // must be integer or boolean, so use the appropriate method in the
          // sub class.
          return createOverflows(leftSide, getZeroBDD());

        } else {

          throw new IllegalArgumentException(unExpr + ":" + operator + " is not known!");
        }
      }

    }

    /**
     * Subclass for parsing (sub-)expressions that are exprected to return
     * boolean values.
     * @author jonkro
     */
    private class GuardExpression2BDDVisitor
      extends Expression2BDDBitVectorVisitor {

      /**
       * Construct a visitor instance that parses expressions that are expected
       * to return boolean values.
       * @param updatedVariables A set in which variables with the next
       * operator applied to them in the expression are added.
       */
      GuardExpression2BDDVisitor(final HashSet<String> updatedVariables) {
        super(updatedVariables);
      }

      /**
       * Parse expressions where the root node is a binary expression (and, or,
       * equals, greater than, etc.).
       * @param binExpr A binary expression
       * @return a BDD representing the operation.
       */
      @Override
      public BDDOverflows visitBinaryExpressionProxy(final BinaryExpressionProxy binExpr)
        throws VisitorException
      {
        final BinaryOperator operator = binExpr.getOperator();

        if (operator.equals(operatorTable.getAndOperator())) {

            final BDDFactory.BDDOp op = BDDFactory.and;
            return binaryBooleanExpression(binExpr, op);

        } else if (operator.equals(operatorTable.getOrOperator())) {

            final BDDFactory.BDDOp op = BDDFactory.or;
            return binaryBooleanExpression(binExpr, op);

        } else if (operator.equals(operatorTable.getEqualsOperator())) {

          final SupremicaBDDBitVector.BitVectorOp bitVectorOp = SupremicaBDDBitVector.equ;
          final BDDFactory.BDDOp bddOp = BDDFactory.and;

          // Need to check whether the left hand side expression is a primed
          // variable, and then extract it.
          String leftVarName = null;
          if (binExpr.getLeft() instanceof UnaryExpressionProxy) {
            final UnaryExpressionProxy unExpr = (UnaryExpressionProxy) binExpr.getLeft();
            if (unExpr.getOperator().equals(operatorTable.getNextOperator())) {
              leftVarName = unExpr.getSubTerm().toString();
            }
          } else {
            leftVarName = binExpr.getLeft().toString();
          }

          return binaryEqualityOperator(binExpr, bitVectorOp, bddOp,
                                        leftVarName);

      } else if (operator.equals(operatorTable.getNotEqualsOperator())) {

          final SupremicaBDDBitVector.BitVectorOp bitVectorOp = SupremicaBDDBitVector.neq;
          final BDDFactory.BDDOp bddOp = BDDFactory.nand;

          // The method need the name of the left hand side sub expression.
          final String leftVarName = binExpr.getLeft().toString();

          return binaryEqualityOperator(binExpr, bitVectorOp, bddOp,
                                             leftVarName);

      } else if (operator.equals(operatorTable.getGreaterThanOperator())) {

          final SupremicaBDDBitVector.BitVectorOp op = SupremicaBDDBitVector.gth;
          return binaryInequalityOperator(binExpr, op);

      } else if (operator.equals(operatorTable.getGreaterEqualsOperator())) {

          final SupremicaBDDBitVector.BitVectorOp op = SupremicaBDDBitVector.gte;
          return binaryInequalityOperator(binExpr, op);

      } else if (operator.equals(operatorTable.getLessThanOperator())) {

          final SupremicaBDDBitVector.BitVectorOp op = SupremicaBDDBitVector.lth;
          return binaryInequalityOperator(binExpr, op);

      } else if (operator.equals(operatorTable.getLessEqualsOperator())) {

          final SupremicaBDDBitVector.BitVectorOp op = SupremicaBDDBitVector.lte;
          return binaryInequalityOperator(binExpr, op);

      } else {

          // See if the superclass might know how to parse the operator.
          // Otherwise it throws an exception.
          return (BDDOverflows) super.visitBinaryExpressionProxy(binExpr);
        }
      }

      /**
       * The expression is a literal and it is expected to be a boolean value.
       * Interpret zero as false and all else as true.
       * @param proxy An integer to parse.
       * @return A BDD representing true or false.
       */
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

      /**
       * The expression is a unary operator, and since it is expected to return
       * a boolean value it is either the not or the next operator.
       * @param unExpr A unary expression to parse.
       * @return A BDD representing true or false.
       */
      @Override
      public BDDOverflows visitUnaryExpressionProxy(final UnaryExpressionProxy unExpr)
        throws VisitorException
      {
        final UnaryOperator operator = unExpr.getOperator();

        if (operator.equals(operatorTable.getNotOperator())) {

          // The not operator is applied to boolean values, so use the guard
          // visitor.
          final GuardExpression2BDDVisitor visitor = new GuardExpression2BDDVisitor(updatedVariables);
          final BDDOverflows ro = (BDDOverflows) unExpr.getSubTerm().acceptVisitor(visitor);
          final BDD tmp = ro.getResult().not();
          return new BDDOverflows(tmp, ro.getOverflows());

        } else {

          // Leave the handling of the next operator to the superclass.
          return (BDDOverflows) super.visitUnaryExpressionProxy(unExpr);
        }
      }

      @Override
      public Expression2BDDBitVectorVisitor buildVisitor(final HashSet<String> updatedVariables)
      {
        return new GuardExpression2BDDVisitor(updatedVariables);
      }

      @Override
      public BDDOverflows createOverflows(final SupremicaBDDBitVector vector,
                                    final BDD overflows)
      {
        return new BDDOverflows(vector.getBit(0), overflows);
      }

      /**
       * Parses the left and right hand side of a binary expression and applies
       * the supplied operators on the results. The results are expected to be
       * integer values.
       * @param binExpr A binary expression
       * @param bitVectorOp The operation on bit vectors if the expression does
       * not contain automaton names.
       * @param bddOp The operation on BDDs if the expression contains automaton
       * names.
       * @param leftVarName The string representation of the left hand sub
       * expression.
       * @return A BDD representing the result.
       * @throws VisitorException
       */
      private BDDOverflows binaryEqualityOperator(final BinaryExpressionProxy binExpr,
                                                  final SupremicaBDDBitVector.BitVectorOp bitVectorOp,
                                                  final BDDFactory.BDDOp bddOp,
                                                  final String leftVarName)
        throws VisitorException
      {
        BDD tmp = null;
        BDD leftOverflows = getZeroBDD();
        BDD rightOverflows = getZeroBDD();
        if (isAutVar(binExpr.getLeft().toString())) {
            final String leftString = binExpr.getLeft().toString();
            final String locName = binExpr.getRight().toString();
            final String autName = leftString.substring(0, leftString.indexOf(bddExAutomata.getLocVarSuffix()));
            tmp = createBDD(bddExAutomata.getIndexMap().getLocationIndex(autName, locName),
                    bddExAutomata.getSourceLocationDomain(autName));
            tmp = tmp.apply(tmp, bddOp);
        } else {
            final SimpleExpressionProxy left = binExpr.getLeft();
            final SimpleExpressionProxy right = binExpr.getRight();
            final Set<String> nonIntegerVarNameSet = bddExAutomata.orgExAutomata.getNonIntegerVarNameSet();
            final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
            final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
            final ResultOverflows roLeft = (ResultOverflows) left.acceptVisitor(leftVisitor);
            ResultOverflows roRight = null;

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
            } else {
              roRight = (ResultOverflows) right.acceptVisitor(rightVisitor);
            }
            tmp = roLeft.getResult().apply(roRight.getResult(), bitVectorOp);
            leftOverflows = roLeft.getOverflows();
            rightOverflows = roRight.getOverflows();
        }
        return new BDDOverflows(tmp, leftOverflows.or(rightOverflows));
      }

      /**
       * Parses the left and right hand side of a binary expression and applies
       * the supplied operator on the results. The two sub-expressions are both
       * expected to return boolean values.
       * @param binExpr A binary expression
       * @param op A BDD operator on boolean values (and, or, etc.)
       * @return A BDD representing the operation.
       * @throws VisitorException
       */
      private BDDOverflows binaryBooleanExpression(final BinaryExpressionProxy binExpr,
                                                   final BDDFactory.BDDOp op)
        throws VisitorException
      {

        // The left hand side and right hand side of the operator both must
        // return boolean values, so use the guard visitor.
        final Expression2BDDBitVectorVisitor leftVisitor = new GuardExpression2BDDVisitor(updatedVariables);
        final Expression2BDDBitVectorVisitor rightVisitor = new GuardExpression2BDDVisitor(updatedVariables);
        final BDDOverflows roLeft = (BDDOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
        final BDDOverflows roRight = (BDDOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
        BDD tmp = roLeft.getResult().id();
        final BDD rightGuard = roRight.getResult();
        tmp = tmp.apply(rightGuard, op);
        return new BDDOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
      }

      /**
       * Parses the left and right hand side of a binary expression and applies
       * the supplied operator on the results. The two sub-expressions are both
       * expected to return integer values.
       * @param binExpr A binary expression
       * @param op A comparison operator (less than, greater or equal, etc.).
       * @return A BDD representing the operation.
       * @throws VisitorException
       */
      private BDDOverflows binaryInequalityOperator(final BinaryExpressionProxy binExpr,
                                                    final SupremicaBDDBitVector.BitVectorOp op)
        throws VisitorException
      {
        // The left hand side and right hand side of the operator both must
        // return integer values, so use the arithmetic visitor.
        final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
        final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
        final ResultOverflows roLeft = (ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
        final ResultOverflows roRight = (ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
        final BDD tmp = roLeft.getResult().apply(roRight.getResult(), op);
        return new BDDOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
      }
    }

    /**
     * Subclass for parsing (sub-)expressions that are expected to return
     * integer values.
     * @author jonkro
     */
    private class ArithmeticExpression2BDDVisitor
      extends Expression2BDDBitVectorVisitor {

      /**
       * Construct a visitor instance that parses expressions that are expected
       * to return integer values.
       * @param updatedVariables A set in which variables with the next
       * operator applied to them in the expression are added.
       */
      ArithmeticExpression2BDDVisitor(final HashSet<String> updatedVariables) {
        super(updatedVariables);
      }

      /**
       * Parse expressions where the root node is a binary expression (plus,
       * minus, modulo, etc.).
       * @param binExpr A binary expression
       * @return a BDDBitVector representing the operation.
       */
      @Override
      public ResultOverflows visitBinaryExpressionProxy(final BinaryExpressionProxy binExpr)
        throws VisitorException
      {
        final BinaryOperator operator = binExpr.getOperator();

        if (operator.equals(operatorTable.getModuloOperator())) {

          // The left hand side and right hand side of the operator both must
          // return integer values, so use the arithmetic visitor.
          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
          final ResultOverflows roLeft = (ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
          final ResultOverflows roRight = (ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
          final SupremicaBDDBitVector v2 = roRight.getResult();
          if (v2.isConst()) {
            return new ResultOverflows(roLeft.getResult().divmod(v2.val(), false).optimizeSize(), roLeft.getOverflows().or(roRight.getOverflows()));
          } else {
            throw new IllegalArgumentException("Divisor is not constant");
          }

        } else if (operator.equals(operatorTable.getMinusOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
          final ResultOverflows roLeft = (ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
          final ResultOverflows roRight = (ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
          final int m = Math.max(roLeft.getResult().length(), roRight.getResult().length()) + 1;
          final SupremicaBDDBitVector left = roLeft.getResult().resize(m);
          final ResultOverflows ro = left.subConsideringOverflows(roRight.getResult());
          return new ResultOverflows(ro.getResult().optimizeSize(), ro.getOverflows().or(roLeft.getOverflows().or(roRight.getOverflows())));

        } else if (operator.equals(operatorTable.getPlusOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
          final ResultOverflows roLeft = (ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor);
          final ResultOverflows roRight = (ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor);
          final int m = Math.max(roLeft.getResult().length(), roRight.getResult().length()) + 1;
          final SupremicaBDDBitVector left = roLeft.getResult().resize(m);
          final ResultOverflows ro = left.addConsideringOverflows(roRight.getResult());
          return new ResultOverflows(ro.getResult().optimizeSize(), ro.getOverflows().or(roLeft.getOverflows().or(roRight.getOverflows())));

        } else if (operator.equals(operatorTable.getDivideOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
          final SupremicaBDDBitVector v2 = ((ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor)).getResult().copy();
          if (v2.isConst()) {
            final SupremicaBDDBitVector left = ((ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor)).getResult();
            final SupremicaBDDBitVector res = left.divmod(v2.val(), true).optimizeSize();
            return new ResultOverflows(res, getZeroBDD());
          } else {
            throw new IllegalArgumentException("Divisor is not constant!");
          }

        } else if (operator.equals(operatorTable.getTimesOperator())) {

          final ArithmeticExpression2BDDVisitor leftVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
          final ArithmeticExpression2BDDVisitor rightVisitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
          final SupremicaBDDBitVector v2 = ((ResultOverflows) binExpr.getRight().acceptVisitor(rightVisitor)).getResult().copy();
          if (v2.isConst()) {
            SupremicaBDDBitVector left = ((ResultOverflows) binExpr.getLeft().acceptVisitor(leftVisitor)).getResult();
            left = left.resize(left.length() + v2.length() + 1);
            final SupremicaBDDBitVector res = left.mulfixed(v2.val()).optimizeSize();
            return new ResultOverflows(res, getZeroBDD());
          } else {
            throw new IllegalArgumentException("Factor is not constant!");
          }

        } else {

          // See if the superclass might know how to parse the operator.
          // Otherwise it throws an exception.
          return (ResultOverflows) super.visitBinaryExpressionProxy(binExpr);
        }
      }

      /**
       * The expression is a literal. Since the result is expected to be an
       * integer value we use a BDD bit vector to pass the result.
       * @param proxy An integer to parse.
       * @return A BDD bit vector.
       */
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

      /**
       * The expression is a unary operator, and since it is expected to return
       * an integer value it is either the unary minus or the next operator.
       * @param unExpr A unary expression to parse.
       * @return A BDD bit vector.
       */
      @Override
      public ResultOverflows visitUnaryExpressionProxy(final UnaryExpressionProxy unExpr)
        throws VisitorException
      {
        final UnaryOperator operator = unExpr.getOperator();

        if (operator.equals(operatorTable.getUnaryMinusOperator())) {

            // The unary minus operator is applied to integer values, so use the
            // arithmetic visitor.
            final ArithmeticExpression2BDDVisitor visitor = new ArithmeticExpression2BDDVisitor(updatedVariables);
            final ResultOverflows ro = (ResultOverflows) unExpr.getSubTerm().acceptVisitor(visitor);
            // TODO: toTwosComplement might result in an overflow if the
            //       bit vector stores the minimum.
            //       For instance, with 3 bits the minimum is -4. Twos
            //       complement is 3+1, but that wraps around to -4.
            return new ResultOverflows(((TCSupremicaBDDBitVector) ro.getResult()).toTwosComplement(), ro.getOverflows());
        }
        else {

            // Leave the handling of the next operator to the superclass.
            return (ResultOverflows) super.visitUnaryExpressionProxy(unExpr);
        }
      }

      @Override
      public Expression2BDDBitVectorVisitor buildVisitor(final HashSet<String> updatedVariables)
      {
        return new ArithmeticExpression2BDDVisitor(updatedVariables);
      }

      @Override
      public Object createOverflows(final SupremicaBDDBitVector vector,
                                    final BDD overflows)
      {
        return new ResultOverflows(vector, overflows);
      }



    }
}
