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

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.subject.module.IntConstantSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.ExtendedAutomata;
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
        // This method should be implemented using Visitor Pattern.
        if (expr instanceof UnaryExpressionProxy) {
            final UnaryExpressionProxy unExpr = (UnaryExpressionProxy) expr;
            if (unExpr.getOperator().equals(CompilerOperatorTable.getInstance().getNotOperator())) {
                final ResultOverflows ro = expr2BDDBitVec(unExpr.getSubTerm(), true, updatedVariables);
                final SupremicaBDDBitVector tmp = ro.getResult().copy();
                tmp.setBit(0, tmp.getBit(0).not());
                return new ResultOverflows(tmp, ro.getOverflows());
            } else if (unExpr.getOperator().equals(CompilerOperatorTable.getInstance().getUnaryMinusOperator())) {
                final ResultOverflows ro = expr2BDDBitVec(unExpr.getSubTerm(), true, updatedVariables);
                return new ResultOverflows(((TCSupremicaBDDBitVector) ro.getResult()).toTwosComplement(), ro.getOverflows());
            } else if (unExpr.getOperator().equals(CompilerOperatorTable.getInstance().getNextOperator())) {
                final String primedVarName = unExpr.getSubTerm().toString();
                if (updatedVariables != null) {
                  updatedVariables.add(primedVarName);
                }
                final Map<String, Integer> varToIndexMap = bddExAutomata.theIndexMap.variableStringToIndexMap;
                final Integer index = varToIndexMap.get(primedVarName);
                final SupremicaBDDBitVector leftSide = bddExAutomata.getBDDBitVecTarget(index);
                return new ResultOverflows(leftSide, getZeroBDD());
            }
            else {
                throw new IllegalArgumentException("Type of operator not known!");
            }
        } else if (expr instanceof BinaryExpressionProxy) {
            final BinaryExpressionProxy bexpr = (BinaryExpressionProxy) expr;
            if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getAndOperator())) {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(), true, updatedVariables);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(), true, updatedVariables);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                final SupremicaBDDBitVector rightGuard = roRight.getResult();
                tmp.setBit(0, tmp.getBit(0).and(rightGuard.getBit(0)));
                return new ResultOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
            } else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getOrOperator())) {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(), true, updatedVariables);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(), true, updatedVariables);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                final SupremicaBDDBitVector rightGuard = roRight.getResult();
                tmp.setBit(0, tmp.getBit(0).or(rightGuard.getBit(0)));
                return new ResultOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
            } else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getModuloOperator())) {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(), false, updatedVariables);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(), false, updatedVariables);
                final SupremicaBDDBitVector v2 = roRight.getResult();
                if (v2.isConst()) {
                    return new ResultOverflows(roLeft.getResult().divmod(v2.val(), false).optimizeSize(), roLeft.getOverflows().or(roRight.getOverflows()));
                } else {
                    throw new IllegalArgumentException("Divisor is not constant");
                }
            } else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getMinusOperator())) {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(), false, updatedVariables);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(), false, updatedVariables);
                final int m = Math.max(roLeft.getResult().length(), roRight.getResult().length()) + 1;
                SupremicaBDDBitVector t = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, m, false);
                t = t.map2(roLeft.getResult(), BDDFactory.or);
                final ResultOverflows ro = t.subConsideringOverflows(roRight.getResult());
                return new ResultOverflows(ro.getResult().optimizeSize(), ro.getOverflows().or(roLeft.getOverflows().or(roRight.getOverflows())));
            } else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getPlusOperator())) {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(), false, updatedVariables);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(), false, updatedVariables);
                final int m = Math.max(roLeft.getResult().length(), roRight.getResult().length()) + 1;
                SupremicaBDDBitVector t = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, m, false);
                t = t.map2(roLeft.getResult(), BDDFactory.or);
                final ResultOverflows ro = t.addConsideringOverflows(roRight.getResult());
                return new ResultOverflows(ro.getResult().optimizeSize(), ro.getOverflows().or(roLeft.getOverflows().or(roRight.getOverflows())));
            } else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getEqualsOperator())) {
                SupremicaBDDBitVector tmp = null;
                BDD leftOverflows = getZeroBDD();
                BDD rightOverflows = getZeroBDD();
                if (isAutVar(bexpr.getLeft().toString())) {
                    final String leftString = bexpr.getLeft().toString();
                    final String locName = bexpr.getRight().toString();
                    final String autName = leftString.substring(0, leftString.indexOf(bddExAutomata.getLocVarSuffix()));
                    tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                                                      false,
                                                      bddExAutomata.getSourceLocationDomain(autName));
                    final BDD locBDD = createBDD(bddExAutomata.getIndexMap().getLocationIndex(autName, locName),
                            bddExAutomata.getSourceLocationDomain(autName));
                    tmp.setBit(0, locBDD);
                }
                else {
                    final SimpleExpressionProxy left = bexpr.getLeft();
                    final SimpleExpressionProxy right = bexpr.getRight();
                    final Set<String> nonIntegerVarNameSet = bddExAutomata.orgExAutomata.getNonIntegerVarNameSet();
                    final ResultOverflows roLeft = expr2BDDBitVec(left, false, updatedVariables);
                    ResultOverflows roRight = null;

                    String leftVarName = null;
                    if (left instanceof UnaryExpressionProxy) {
                      final UnaryExpressionProxy unExpr = (UnaryExpressionProxy) left;
                      if (unExpr.getOperator().equals(CompilerOperatorTable.getInstance().getNextOperator())) {
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
                      roRight = expr2BDDBitVec(mappedIntProxy, false, updatedVariables);
                    } else {
                      roRight = expr2BDDBitVec(bexpr.getRight(), false, updatedVariables);
                    }
                    tmp = roLeft.getResult().copy();
                    tmp.setBit(0, tmp.equ(roRight.getResult()));
                    leftOverflows = roLeft.getOverflows();
                    rightOverflows = roRight.getOverflows();
                }
                return new ResultOverflows(tmp, leftOverflows.or(rightOverflows));
            } else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getNotEqualsOperator())) {
                SupremicaBDDBitVector tmp = null;
                BDD leftOverflows = getZeroBDD();
                BDD rightOverflows = getZeroBDD();
                if (isAutVar(bexpr.getLeft().toString())) {
                    final String leftString = bexpr.getLeft().toString();
                    final String locName = bexpr.getRight().toString();
                    final String autName = leftString.substring(0, leftString.indexOf(bddExAutomata.getLocVarSuffix()));
                    tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                                                      false,
                                                      bddExAutomata.getSourceLocationDomain(autName));
                    final BDD locBDD = createBDD(bddExAutomata.getIndexMap().getLocationIndex(autName, locName),
                            bddExAutomata.getSourceLocationDomain(autName)).not();
                    tmp.setBit(0, locBDD);
                } else {
                    final SimpleExpressionProxy left = bexpr.getLeft();
                    final SimpleExpressionProxy right = bexpr.getRight();
                    final Set<String> nonIntegerVarNameSet = bddExAutomata.orgExAutomata.getNonIntegerVarNameSet();
                    final ResultOverflows roLeft = expr2BDDBitVec(left, false, updatedVariables);
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
                      roRight = expr2BDDBitVec(mappedIntProxy, false, updatedVariables);
                    } else {
                      roRight = expr2BDDBitVec(bexpr.getRight(), false, updatedVariables);
                    }
                    tmp = roLeft.getResult().copy();
                    tmp.setBit(0, tmp.neq(roRight.getResult()));
                    leftOverflows = roLeft.getOverflows();
                    rightOverflows = roRight.getOverflows();
                }
                return new ResultOverflows(tmp, leftOverflows.or(rightOverflows));
            } else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getGreaterThanOperator())) {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(), false, updatedVariables);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(), false, updatedVariables);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                tmp.setBit(0, tmp.gth(roRight.getResult()));
                return new ResultOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
            } else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getGreaterEqualsOperator())) {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(), false, updatedVariables);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(), false, updatedVariables);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                tmp.setBit(0, tmp.gte(roRight.getResult()));
                return new ResultOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
            } else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getLessThanOperator())) {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(), false, updatedVariables);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(), false, updatedVariables);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                tmp.setBit(0, tmp.lth(roRight.getResult()));
                return new ResultOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
            } else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getLessEqualsOperator())) {
                final ResultOverflows roLeft = expr2BDDBitVec(bexpr.getLeft(), false, updatedVariables);
                final ResultOverflows roRight = expr2BDDBitVec(bexpr.getRight(), false, updatedVariables);
                final SupremicaBDDBitVector tmp = roLeft.getResult().copy();
                tmp.setBit(0, tmp.lte(roRight.getResult()));
                return new ResultOverflows(tmp, roLeft.getOverflows().or(roRight.getOverflows()));
            } else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getDivideOperator())) {
                final SupremicaBDDBitVector v2 = expr2BDDBitVec(bexpr.getRight(), false, updatedVariables).getResult().copy();
                if (v2.isConst()) {
                  final SupremicaBDDBitVector left = expr2BDDBitVec(bexpr.getLeft(), false, updatedVariables).getResult();
                  final SupremicaBDDBitVector res = left.divmod(v2.val(), true).optimizeSize();
                    return new ResultOverflows(res, getZeroBDD());
                } else {
                    throw new IllegalArgumentException("Divisor is not constant!");
                }
            }else if (bexpr.getOperator().equals(CompilerOperatorTable.getInstance().getTimesOperator())) {
                final SupremicaBDDBitVector v2 = expr2BDDBitVec(bexpr.getRight(), false, updatedVariables).getResult().copy();
                if (v2.isConst()) {
                  final SupremicaBDDBitVector left = expr2BDDBitVec(bexpr.getLeft(), false, updatedVariables).getResult();
                  SupremicaBDDBitVector t = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, left.length() + v2.length(), false);
                  t = t.map2(left, BDDFactory.or);
                  final SupremicaBDDBitVector res = t.mulfixed(v2.val()).optimizeSize();
                    return new ResultOverflows(res, getZeroBDD());
                } else {
                    throw new IllegalArgumentException("Factor is not constant!");
                }
            }
            else {
                throw new IllegalArgumentException(bexpr + ":" + bexpr.getOperator() + " is not known!");
            }
            //I have added the other operators to SupremicaBDDBitVector... they should be verified though.
            //Currently, I don't have time to do that... maybe in the future (BTW it is not too much work).

        } else if (expr instanceof SimpleIdentifierProxy) {
            final String varNameOrInstValue = ((SimpleIdentifierProxy) expr).getName();
            final Map<String, Integer> varToIndexMap = bddExAutomata.theIndexMap.variableStringToIndexMap;
            final Integer index = varToIndexMap.get(varNameOrInstValue);
            return new ResultOverflows(bddExAutomata.getBDDBitVecSource(index), getZeroBDD());
        }
		else if (expr instanceof IntConstantProxy)
		{
            if (guardAction)
			{
                // SupremicaBDDBitVector tmp = null;
//                if(constantDomain ==null)
                final SupremicaBDDBitVector tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, bddExAutomata.BDDBitVectoryType + 1, 0).copy();
//                    tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory, bddExAutomata.constantDomain.varNum(),0).copy();
//                else
//                    tmp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory, constantDomain.varNum(),0).copy();

                if (((IntConstantProxy) expr).getValue() == 0) {
                    tmp.setBit(0, getZeroBDD());
                } else {
                    tmp.setBit(0, getOneBDD());
                }

                return new ResultOverflows(tmp, getZeroBDD());
            } else {
                final int value = ((IntConstantProxy) expr).getValue();
                final boolean inDomain = true || value >= bddExAutomata.theIndexMap.getVariableLowerBound() && value <= bddExAutomata.theIndexMap.getVariableUpperBound();
                if (inDomain) {
                    return new ResultOverflows(
                      createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                        ((int) Math.pow(2, (int) Math.ceil(Math.log(Math.abs(value) + 1) / Math.log(2)) + 1)),
                        value),
                      getZeroBDD());
                } else {
                    logger.error(expr.toString() + " is out of the bounds. The value will be set to 0!");
                    return new ResultOverflows(createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, bddExAutomata.BDDBitVectoryType + 1, 0), getZeroBDD());
                }
            }
        }

        throw new IllegalArgumentException("Type of expression not known!");
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
}
