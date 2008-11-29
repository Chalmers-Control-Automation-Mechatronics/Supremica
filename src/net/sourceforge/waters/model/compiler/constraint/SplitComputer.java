//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   SplitComputer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.
  CompilerExpressionComparator;
import net.sourceforge.waters.model.compiler.context.
  UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
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
    mFactory = factory;
    mOperatorTable = optable;
    mDisjunctionCollectVisitor = new DisjunctionCollectVisitor();
    mCollectVisitor = new CollectVisitor();
    mExpressionComparator =
      new CompilerExpressionComparator(optable, root, true);
    mCandidateComparator = new CandidateComparator();
    mCombinations = new HashSet<VariableCombination>();
    mCandidateMap = new HashMap<ProxyAccessor<SimpleExpressionProxy>,
                                AbstractSplitCandidate>();
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
    (final ProxyAccessorMap<SimpleExpressionProxy> contents)
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
    (final ProxyAccessorMap<SimpleExpressionProxy> contents)
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
      final VariableSplitCandidate vcand;
      final AbstractSplitCandidate cand = mCandidateMap.get(varname);
      if (cand == null) {
        final CompiledRange range = mContext.getVariableRange(varname);
        vcand = new VariableSplitCandidate(varname, range);
        addCandidate(varname, vcand);
      } else {
        vcand = (VariableSplitCandidate) cand;
      }
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

  private void addCandidate(final SimpleExpressionProxy expr,
                            final AbstractSplitCandidate cand)
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(expr);
    mCandidateMap.put(accessor, cand);
  }

  private boolean isNextOf(final SimpleExpressionProxy expr,
                           final SimpleExpressionProxy varname)
  {
    if (expr instanceof UnaryExpressionProxy) {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) expr;
      return
        unary.getOperator() == mOperatorTable.getNextOperator() &&
        unary.getSubTerm().equalsByContents(varname);
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
    public int compare(final AbstractSplitCandidate cand1,
                       final AbstractSplitCandidate cand2)
    {
      final int numocc1 = cand1.getNumberOfOccurrences();
      final int numocc2 = cand2.getNumberOfOccurrences();
      if (numocc1 != numocc2) {
        return numocc1 - numocc2;
      }
      final int size1 = cand1.getSplitSize();
      final int size2 = cand2.getSplitSize();
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
    extends AbstractModuleProxyVisitor
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
          final ProxyAccessorMap<SimpleExpressionProxy> variables =
            disjunct.getVariables();
          return createRelevantVariableCombination(variables);
        } else {
          final int size = mDisjuncts.size();
          final ProxyAccessorMap<SimpleExpressionProxy> result =
            new ProxyAccessorHashMapByContents<SimpleExpressionProxy>(size);
          final Map<VariableCombination,List<SimpleExpressionProxy>> splitmap =
            new HashMap<VariableCombination,List<SimpleExpressionProxy>>(size);
          for (final Disjunct disjunct : mDisjuncts) {
            final ProxyAccessorMap<SimpleExpressionProxy> variables =
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

    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      final ProxyAccessorMap<SimpleExpressionProxy> collection =
        new ProxyAccessorHashMapByContents<SimpleExpressionProxy>();
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
  private class CollectVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    boolean process(final SimpleExpressionProxy expr,
                    final ProxyAccessorMap<SimpleExpressionProxy> collection)
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
    public Boolean visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy lhs = expr.getLeft();
      final boolean lhsresult = process(lhs);
      final SimpleExpressionProxy rhs = expr.getRight();
      final boolean rhsresult = process(rhs);
      return lhsresult | rhsresult;
    }

    public Boolean visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      if (mContext.isEnumAtom(ident)) {
        return false;
      } else if (mContext.getVariableRange(ident) != null) {
        return recordCandidate(ident);
      } else {
        final UndefinedIdentifierException exception =
          new UndefinedIdentifierException(ident);
        throw wrap(exception);
      }
    }

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

    public Boolean visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return false;
    }

    public Boolean visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      final boolean result = process(subterm);
      if (result) {
        return result;
      }
      final UnaryOperator op = expr.getOperator();
      if (op != mOperatorTable.getNextOperator()) {
        return result;
      } else if (mContext.getVariableRange(expr) != null) {
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

    //#######################################################################
    //# Data Members
    private ProxyAccessorMap<SimpleExpressionProxy> mCollection;
    private boolean mInIndex;
    private boolean mInQualification;

  }


  //#########################################################################
  //# Inner Class Disjunct
  private static class Disjunct {

    //#######################################################################
    //# Constructor
    Disjunct(final SimpleExpressionProxy expr,
             final ProxyAccessorMap<SimpleExpressionProxy> variables)
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

    ProxyAccessorMap<SimpleExpressionProxy> getVariables()
    {
      return mVariables;
    }

    //#######################################################################
    //# Data Members
    private final SimpleExpressionProxy mExpression;
    private final ProxyAccessorMap<SimpleExpressionProxy> mVariables;

  }


  //#########################################################################
  //# Inner Class VariableCombination
  private class VariableCombination {

    //#######################################################################
    //# Constructor
    private VariableCombination
      (final ProxyAccessorMap<SimpleExpressionProxy> contents)
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
    //# Equals & Hashcode
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

    public int hashCode()
    {
      return mContents.hashCodeByAccessorEquality();
    }

    //#######################################################################
    //# Data Members
    private final ProxyAccessorMap<SimpleExpressionProxy> mContents;

  }


  //#########################################################################
  //# Data Members
  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOperatorTable;
  private final DisjunctionCollectVisitor mDisjunctionCollectVisitor;
  private final CollectVisitor mCollectVisitor;
  private final Comparator<SimpleExpressionProxy> mExpressionComparator;
  private final Comparator<AbstractSplitCandidate> mCandidateComparator;
  private final Set<VariableCombination> mCombinations;
  private final Map<ProxyAccessor<SimpleExpressionProxy>,
                    AbstractSplitCandidate> mCandidateMap;

  private VariableContext mContext;
  private AbstractSplitCandidate mBestCandidate;

}
