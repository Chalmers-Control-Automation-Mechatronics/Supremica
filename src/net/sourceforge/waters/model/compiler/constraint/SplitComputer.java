//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   SplitComputer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.BuiltInFunction;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.DescendingModuleProxyVisitor;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


public class SplitComputer
{

  //#########################################################################
  //# Constructors
  public SplitComputer(final ModuleProxyFactory factory,
                       final CompilerOperatorTable optable,
                       final VariableContext root)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mFactory = factory;
    mOperatorTable = optable;
    mDisjunctionCollectVisitor = new DisjunctionCollectVisitor();
    mCollectVisitor = new CollectVisitor();
    mOneVariableFinder = new OneVariableFinder();
    mEquality = new ModuleEqualityVisitor(false);
    mExpressionComparator = new ExpressionComparator(optable);
    mCandidateComparator = new CandidateComparator();
    mCombinations = new THashSet<>();
    mCandidateMap = new ProxyAccessorHashMap<>(eq);
  }


  //#########################################################################
  //# Invocation
  public SplitCandidate proposeSplit(final ConstraintList constraints,
                                     final VariableContext context)
    throws EvalException
  {
    try {
      mContext = context;
      mBestCandidate = null;
      for (final SimpleExpressionProxy constraint :
             constraints.getConstraints()) {
        final VariableCombination comb =
          mDisjunctionCollectVisitor.collect(constraint);
        if (comb != null && mBestCandidate == null) {
          mCombinations.add(comb);
        }
      }
      if (mBestCandidate == null) {
        for (final VariableCombination comb : mCombinations) {
          createVariableSplitCandidates(comb);
        }
        if (!mCandidateMap.isEmpty()) {
          final Collection<AbstractSplitCandidate> candidates =
            mCandidateMap.values();
          mBestCandidate = Collections.min(candidates, mCandidateComparator);
        }
      }
      return mBestCandidate;
    } finally {
      mContext = null;
      mBestCandidate = null;
      mCombinations.clear();
      mCandidateMap.clear();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private VariableCombination createRelevantVariableCombination
    (final ProxyAccessorSet<SimpleExpressionProxy> contents)
  {
    switch (contents.size()) {
    case 0:
    case 1:
      return null;
    case 2:
      final Iterator<SimpleExpressionProxy> iter =
        contents.values().iterator();
      final SimpleExpressionProxy first = iter.next();
      final SimpleExpressionProxy second = iter.next();
      if (isNextOf(first, second) || isNextOf(second, first)) {
        return null;
      } else {
        return new VariableCombination(contents);
      }
    default:
      return new VariableCombination(contents);
    }
  }

  private VariableCombination createTrimVariableCombination
    (final ProxyAccessorSet<SimpleExpressionProxy> contents)
  {
    switch (contents.size()) {
    case 1:
      final SimpleExpressionProxy expr = contents.values().iterator().next();
      if (expr instanceof UnaryExpressionProxy) {
        final UnaryExpressionProxy unary = (UnaryExpressionProxy) expr;
        final SimpleExpressionProxy subterm = unary.getSubTerm();
        contents.clear();
        contents.addProxy(subterm);
      }
      break;
    case 2:
      final Iterator<SimpleExpressionProxy> iter =
        contents.values().iterator();
      final SimpleExpressionProxy first = iter.next();
      final SimpleExpressionProxy second = iter.next();
      if (isNextOf(first, second)) {
        contents.removeProxy(first);
      } else if (isNextOf(second, first)) {
        contents.removeProxy(second);
      }
      break;
    default:
      break;
    }
    return new VariableCombination(contents);
  }

  private void createIndexSplitCandidate(final SimpleExpressionProxy varname)
  {
    final CompiledRange range = mContext.getVariableRange(varname);
    final VariableSplitCandidate cand =
      new VariableSplitCandidate(varname, range);
    if (mBestCandidate == null) {
      mBestCandidate = cand;
      mCandidateMap.clear();
      mCombinations.clear();
    } else if (mCandidateComparator.compare(cand, mBestCandidate) < 0) {
      mBestCandidate = cand;
    }
  }

  private void createDisjunctionSplitCandidate
    (final SimpleExpressionProxy disj,
     final int size)
  {
    if (mBestCandidate == null) {
      final AbstractSplitCandidate cand =
        new DisjunctionSplitCandidate(disj, size);
      addCandidate(disj, cand);
    }
  }

  private void createDisjunctionSplitCandidate
    (final SimpleExpressionProxy disj,
     final Collection<List<SimpleExpressionProxy>> parts)
  {
    if (mBestCandidate == null) {
      final AbstractSplitCandidate cand =
        new DisjunctionSplitCandidate(disj, parts);
      addCandidate(disj, cand);
    }
  }

  private void createVariableSplitCandidates(final VariableCombination comb)
  {
    final UnaryOperator nextop = mOperatorTable.getNextOperator();
    for (final SimpleExpressionProxy varname : comb.getVariables()) {
      final VariableSplitCandidate vcand =
        createVariableSplitCandidate(varname);
      vcand.addOccurrence();
      if (varname instanceof IdentifierProxy) {
        final UnaryExpressionProxy nextvarname =
          mFactory.createUnaryExpressionProxy(nextop, varname);
        if (comb.contains(nextvarname)) {
          vcand.setOccursWithNext();
        }
      } else if (varname instanceof UnaryExpressionProxy) {
        final UnaryExpressionProxy unary = (UnaryExpressionProxy) varname;
        final SimpleExpressionProxy subterm = unary.getSubTerm();
        if (comb.contains(subterm)) {
          vcand.setOccursWithNext();
        }
      }
    }
  }


  private VariableSplitCandidate createVariableSplitCandidate
    (final SimpleExpressionProxy varname)
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      mCandidateMap.createAccessor(varname);
    final AbstractSplitCandidate cand = mCandidateMap.get(accessor);
    if (cand == null) {
      final CompiledRange range = mContext.getVariableRange(varname);
      final VariableSplitCandidate vcand =
        new VariableSplitCandidate(varname, range);
      mCandidateMap.put(accessor, vcand);
      return vcand;
    } else {
      return (VariableSplitCandidate) cand;
    }
  }

  private void addCandidate(final SimpleExpressionProxy expr,
                            final AbstractSplitCandidate cand)
  {
    mCandidateMap.putByProxy(expr, cand);
  }

  private boolean isNextOf(final SimpleExpressionProxy expr,
                           final SimpleExpressionProxy varname)
  {
    if (expr instanceof UnaryExpressionProxy) {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) expr;
      if (unary.getOperator() != mOperatorTable.getNextOperator()) {
        return false;
      }
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final SimpleExpressionProxy subterm = unary.getSubTerm();
      return eq.equals(subterm, varname);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Inner Class CandidateComparator
  private class CandidateComparator
    implements Comparator<AbstractSplitCandidate>
  {

    //#######################################################################
    //# Interface java.util.Comparator
    @Override
    public int compare(final AbstractSplitCandidate cand1,
                       final AbstractSplitCandidate cand2)
    {
      final int numocc1 = cand1.getNumberOfOccurrences();
      final int numocc2 = cand2.getNumberOfOccurrences();
      if (numocc1 != numocc2) {
        return numocc2 - numocc1;
      }
      final int size1 = cand1.getPredictedSplitSize();
      final int size2 = cand2.getPredictedSplitSize();
      if (size1 != size2) {
        return size1 - size2;
      }
      final boolean occnext1 = cand1.getOccursWithNext();
      final boolean occnext2 = cand2.getOccursWithNext();
      if (occnext1 && !occnext2) {
        return 1;
      } else if (!occnext1 && occnext2) {
        return -1;
      }
      final int kind1 = cand1.getKindValue();
      final int kind2 = cand2.getKindValue();
      if (kind1 != kind2) {
        return kind1 - kind2;
      }
      final SimpleExpressionProxy expr1 = cand1.getSplitExpression();
      final SimpleExpressionProxy expr2 = cand2.getSplitExpression();
      return mExpressionComparator.compare(expr1, expr2);
    }

  }


  //#########################################################################
  //# Inner Class DisjunctionCollectVisitor
  private class DisjunctionCollectVisitor
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    VariableCombination collect(final SimpleExpressionProxy expr)
      throws EvalException
    {
      try {
        mDisjuncts = new LinkedList<Disjunct>();
        final boolean hasindex = (Boolean) expr.acceptVisitor(this);
        if (hasindex) {
          return null;
        } else if (mDisjuncts.size() == 1) {
          final Disjunct disjunct = mDisjuncts.iterator().next();
          final ProxyAccessorSet<SimpleExpressionProxy> variables =
            disjunct.getVariables();
          return createRelevantVariableCombination(variables);
        } else {
          final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
          final int size = mDisjuncts.size();
          final ProxyAccessorSet<SimpleExpressionProxy> result =
            new ProxyAccessorHashSet<>(eq, size);
          final Map<VariableCombination,List<SimpleExpressionProxy>> splitmap =
            new HashMap<>(size);
          for (final Disjunct disjunct : mDisjuncts) {
            final ProxyAccessorSet<SimpleExpressionProxy> variables =
              disjunct.getVariables();
            result.putAll(variables);
            final VariableCombination comb =
              createTrimVariableCombination(variables);
            List<SimpleExpressionProxy> list = splitmap.get(comb);
            if (list == null) {
              list = new LinkedList<SimpleExpressionProxy>();
              splitmap.put(comb, list);
            }
            final SimpleExpressionProxy literal = disjunct.getExpression();
            list.add(literal);
          }
          final VariableCombination comb =
            createRelevantVariableCombination(result);
          if (splitmap.size() > 1) {
            createDisjunctionSplitCandidate(expr, splitmap.values());
          } else if (comb != null) {
            createDisjunctionSplitCandidate(expr, size);
          }
          return comb;
        }
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      } finally {
        mDisjuncts = null;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean process(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      return (Boolean) expr.acceptVisitor(this);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Boolean visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final BinaryOperator op = expr.getOperator();
      if (op == mOperatorTable.getOrOperator()) {
        final SimpleExpressionProxy lhs = expr.getLeft();
        final boolean lhsresult = process(lhs);
        final SimpleExpressionProxy rhs = expr.getRight();
        final boolean rhsresult = process(rhs);
        return lhsresult | rhsresult;
      } else {
        return visitSimpleExpressionProxy(expr);
      }
    }

    @Override
    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final ProxyAccessorSet<SimpleExpressionProxy> collection =
        new ProxyAccessorHashSet<>(eq);
      final boolean result = mCollectVisitor.process(expr, collection);
      if (!result) {
        final Disjunct disjunct = new Disjunct(expr, collection);
        mDisjuncts.add(disjunct);
      }
      return result;
    }

    //#######################################################################
    //# Data Members
    private List<Disjunct> mDisjuncts;

  }


  //#########################################################################
  //# Inner Class CollectVisitor
  /**
   * A visitor that collects the variables in an expression.
   * Splitting is performed by assigning to each variable all possible values
   * from is range.
   */
  private class CollectVisitor
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    boolean process(final SimpleExpressionProxy expr,
                    final ProxyAccessorSet<SimpleExpressionProxy> collection)
      throws VisitorException
    {
      try {
        mCollection = collection;
        return process(expr);
      } finally {
        mCollection = null;
      }
    }

    boolean process(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      return (Boolean) expr.acceptVisitor(this);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Boolean visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy lhs = expr.getLeft();
      final boolean lhsresult = process(lhs);
      final SimpleExpressionProxy rhs = expr.getRight();
      final boolean rhsresult = process(rhs);
      return lhsresult | rhsresult;
    }

    @Override
    public Boolean visitFunctionCallExpressionProxy
      (final FunctionCallExpressionProxy expr)
      throws VisitorException
    {
      final String name = expr.getFunctionName();
      final BuiltInFunction function = mOperatorTable.getBuiltInFunction(name);
      final List<SimpleExpressionProxy> args = expr.getArguments();
      if (function == mOperatorTable.getIteFunction()) {
        final SimpleExpressionProxy cond = args.get(0);
        recordIteSplit(cond);
      }
      boolean result = false;
      for (final SimpleExpressionProxy arg : args) {
        result |= process(arg);
      }
      return result;
    }

    @Override
    public Boolean visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      if (mInQualification) {
        return false;
      } else if (mContext.isEnumAtom(ident)) {
        return false;
      } else if (mContext.getVariableRange(ident) != null) {
        return recordCandidate(ident);
      } else {
        final UndefinedIdentifierException exception =
          new UndefinedIdentifierException(ident);
        throw wrap(exception);
      }
    }

    @Override
    public Boolean visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      final boolean save = mInIndex;
      boolean hasindexvar = false;
      try {
        mInIndex = true;
        final List<SimpleExpressionProxy> indexes = ident.getIndexes();
        for (final SimpleExpressionProxy index : indexes) {
          if (process(index)) {
            hasindexvar = true;
          }
        }
      } finally {
        mInIndex = save;
      }
      if (hasindexvar) {
        return true;
      } else {
        return visitIdentifierProxy(ident);
      }
    }

    @Override
    public Boolean visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      final IdentifierProxy comp = ident.getComponentIdentifier();
      if (mInQualification) {
        final boolean baseresult = process(base);
        final boolean compresult = process(comp);
        return baseresult | compresult;
      } else {
        try {
          mInQualification = true;
          final boolean baseresult = process(base);
          final boolean compresult = process(comp);
          mInQualification = false;
          if (baseresult | compresult) {
            return true;
          } else {
            return visitIdentifierProxy(ident);
          }
        } finally {
          mInQualification = false;
        }
      }
    }

    @Override
    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return false;
    }

    @Override
    public Boolean visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final ProxyAccessorSet<SimpleExpressionProxy> save = mCollection;
      final boolean isnext =
        (expr.getOperator() == mOperatorTable.getNextOperator());
      if (isnext) {
        mCollection = null;
      }
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      final boolean result = process(subterm);
      if (result || !isnext) {
        return result;
      }
      mCollection = save;
      if (mContext.getVariableRange(expr) != null) {
        return recordCandidate(expr);
      } else {
        final UndefinedIdentifierException exception =
          new UndefinedIdentifierException(expr);
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean recordCandidate(final SimpleExpressionProxy expr)
    {
      if (mInIndex) {
        createIndexSplitCandidate(expr);
        mCollection = null;
      } else if (mCollection != null) {
        mCollection.addProxy(expr);
      }
      return mInIndex;
    }

    private void recordIteSplit(final SimpleExpressionProxy cond)
    {
      final SimpleExpressionProxy varname =
        mOneVariableFinder.findUniqueVariable(cond);
      if (varname != null) {
        final VariableSplitCandidate vcand =
          createVariableSplitCandidate(varname);
        vcand.addIteSplit(cond);
      }
    }

    //#######################################################################
    //# Data Members
    private ProxyAccessorSet<SimpleExpressionProxy> mCollection;
    private boolean mInIndex;
    private boolean mInQualification;

  }


  //#########################################################################
  //# Inner Class OneVariableFinder
  /**
   * A visitor that collects the variables in an expression.
   * Splitting is performed by assigning to each variable all possible values
   * from is range.
   */
  private class OneVariableFinder
    extends DescendingModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private SimpleExpressionProxy findUniqueVariable
      (final SimpleExpressionProxy expr)
    {
      try {
        if (scan(expr)) {
          return mVariable;
        } else {
          return null;
        }
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mVariable = null;
      }
    }

    private Boolean scan(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      return (Boolean) expr.acceptVisitor(this);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Boolean visitBinaryExpressionProxy
      (final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy lhs = expr.getLeft();
      final SimpleExpressionProxy rhs = expr.getRight();
      return scan(lhs) || scan(rhs);
    }

    @Override
    public Boolean visitFunctionCallExpressionProxy
      (final FunctionCallExpressionProxy expr)
      throws VisitorException
    {
      final List<SimpleExpressionProxy> args = expr.getArguments();
      for (final SimpleExpressionProxy arg : args) {
        if (scan(arg)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Boolean visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return false;
    }

    @Override
    public Boolean visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      if (mContext.getVariableRange(ident) == null) {
        return true;
      } else if (mVariable == null) {
        mVariable = ident;
        return true;
      } else {
        return mEquality.equals(mVariable, ident);
      }
    }

    @Override
    public Boolean visitUnaryExpressionProxy
      (final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      if (expr.getOperator() != mOperatorTable.getNextOperator()) {
        return scan(subterm);
      } else if (!(subterm instanceof SimpleIdentifierProxy)) {
        return false;
      } else if (mContext.getVariableRange(subterm) == null) {
        return true;
      } else if (mVariable == null) {
        mVariable = expr;
        return true;
      } else {
        return mEquality.equals(mVariable, expr);
      }
    }

    //#######################################################################
    //# Data Members
    private SimpleExpressionProxy mVariable;
  }


  //#########################################################################
  //# Inner Class Disjunct
  private static class Disjunct {

    //#######################################################################
    //# Constructor
    Disjunct(final SimpleExpressionProxy expr,
             final ProxyAccessorSet<SimpleExpressionProxy> variables)
    {
      mExpression = expr;
      mVariables = variables;
    }

    //#######################################################################
    //# Simple Access
    SimpleExpressionProxy getExpression()
    {
      return mExpression;
    }

    ProxyAccessorSet<SimpleExpressionProxy> getVariables()
    {
      return mVariables;
    }

    //#######################################################################
    //# Data Members
    private final SimpleExpressionProxy mExpression;
    private final ProxyAccessorSet<SimpleExpressionProxy> mVariables;

  }


  //#########################################################################
  //# Inner Class VariableCombination
  private class VariableCombination {

    //#######################################################################
    //# Constructor
    private VariableCombination
      (final ProxyAccessorSet<SimpleExpressionProxy> contents)
    {
      mContents = contents;
    }

    //#######################################################################
    //# Simple Access
    Collection<SimpleExpressionProxy> getVariables()
    {
      return mContents.values();
    }

    boolean contains(final SimpleExpressionProxy expr)
    {
      return mContents.containsProxy(expr);
    }

    //#######################################################################
    //# Overrides for Baseclass java.lang.Object
    @Override
    public String toString()
    {
      return mContents.values().toString();
    }

    @Override
    public boolean equals(final Object other)
    {
      if (other != null && other.getClass() == getClass()) {
        final VariableCombination combination =
          (VariableCombination) other;
        return mContents.equalsByAccessorEquality(combination.mContents);
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      return mContents.hashCodeByAccessorEquality();
    }

    //#######################################################################
    //# Data Members
    private final ProxyAccessorSet<SimpleExpressionProxy> mContents;

  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final DisjunctionCollectVisitor mDisjunctionCollectVisitor;
  private final CollectVisitor mCollectVisitor;
  private final OneVariableFinder mOneVariableFinder;
  private final ModuleEqualityVisitor mEquality;
  private final Comparator<SimpleExpressionProxy> mExpressionComparator;
  private final Comparator<AbstractSplitCandidate> mCandidateComparator;
  private final Set<VariableCombination> mCombinations;
  private final ProxyAccessorMap<SimpleExpressionProxy,AbstractSplitCandidate>
    mCandidateMap;

  private VariableContext mContext;
  private AbstractSplitCandidate mBestCandidate;

}
