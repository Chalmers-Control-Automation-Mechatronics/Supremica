//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   SplitComputer
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.dnf.CompiledClause;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


class SplitComputer
{

  //#########################################################################
  //# Constructors
  SplitComputer(final EFAModuleContext context)
  {
    final int size = context.getVariableNames().size();
    mIndexSplitVisitor = new IndexSplitVisitor();
    mVariableCollector = new EFAVariableCollector();
    mContext = context;
    mCandidateMap = new HashMap<EFAVariable,SplitCandidate>(size);
    mCandidateList = new ArrayList<SplitCandidate>(size);
  }


  //#########################################################################
  //# Invocation
  List<EFAVariable> computeSplitList(final CompiledClause clause)
  {
    final EFAVariable isplit = computeIndexSplit(clause);
    if (isplit != null) {
      return Collections.singletonList(isplit);
    } else {
      return computeProperSplit(clause);
    }
  }

  EFAVariable computeIndexSplit(final CompiledClause clause)
  {
    return mIndexSplitVisitor.getIndexSplit(clause);
  }

  List<EFAVariable> computeProperSplit(final CompiledClause clause)
  {
    final Collection<SimpleExpressionProxy> literals = clause.getLiterals();
    final int size = literals.size();
    final Collection<EFAVariableCombination> combinations =
      new HashSet<EFAVariableCombination>(size);
    for (final SimpleExpressionProxy literal : literals) {
      final Collection<EFAVariable> vars = mVariableCollector.collect(literal);
      final EFAVariableCombination combination =
        EFAVariableCombination.create(vars);
      if (combination != null) {
        combinations.add(combination);
      }
    }
    return computeSplitList(combinations);
  }


  //#########################################################################
  //# Algorithm
  private List<EFAVariable> computeSplitList
    (final Collection<EFAVariableCombination> combinations)
  {
    try {
      for (final EFAVariableCombination combination : combinations) {
        for (final EFAVariable var : combination.getContents()) {
          final SplitCandidate cand = createCandidate(var);
          cand.addCombination();
        }
      }
      Collections.sort(mCandidateList);
      final int numcandidates = mCandidateList.size();
      final List<SplitCandidate> current =
        new ArrayList<SplitCandidate>(numcandidates);
      mBestCost = MAX_COST;
      mBestSolution = null;
      searchCandidatesList(0, 1, current, combinations);
      if (mBestSolution == null) {
        // throw
        return null;
      } else {
        final int size = mBestSolution.size();
        final List<EFAVariable> result = new ArrayList<EFAVariable>(size);
        for (final SplitCandidate cand : mBestSolution) {
          final EFAVariable var = cand.getVariable();
          result.add(var);
        }
        return result;
      }
    } finally {
      mCandidateMap.clear();
      mCandidateList.clear();
    }
  }

  private void searchCandidatesList
    (final int start,
     final long cost,
     final List<SplitCandidate> current,
     final Collection<EFAVariableCombination> open)
  {
    if (open.isEmpty()) {
      mBestCost = cost;
      mBestSolution = new ArrayList<SplitCandidate>(current);
    } else {
      final Set<EFAVariable> openvars = new HashSet<EFAVariable>();
      for (final EFAVariableCombination combination : open) {
        final Set<EFAVariable> contents = combination.getContents();
        openvars.addAll(contents);
      }
      final int numcandidates = mCandidateList.size();
      for (int index = start; index < numcandidates; index++) {
        final SplitCandidate cand = mCandidateList.get(index);
        final EFAVariable var = cand.getVariable();
        if (!openvars.contains(var)) {
          continue;
        }
        final int rangesize = cand.getRangeSize();
        final long nextcost = cost * rangesize;
        if (nextcost >= mBestCost) {
          continue;
        }
        final int nextindex = index + 1;
        final int pos = current.size();
        current.add(cand);
        final int numopen = open.size();
        final Collection<EFAVariableCombination> nextopen =
          new ArrayList<EFAVariableCombination>(numopen);
        for (final EFAVariableCombination combination : open) {
          final EFAVariableCombination reduced =
            combination.getReducedCombination(var);
          if (reduced != null) {
            nextopen.add(reduced);
          }
        }
        searchCandidatesList(nextindex, nextcost, current, nextopen);
        current.remove(pos);
      }
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private SplitCandidate createCandidate(final EFAVariable var)
  {
    SplitCandidate cand = mCandidateMap.get(var);
    if (cand == null) {
      cand = new SplitCandidate(var);
      mCandidateMap.put(var, cand);
      mCandidateList.add(cand);
    }
    return cand;
  }

  private SplitCandidate getCandidate(final EFAVariable var)
  {
    return mCandidateMap.get(var);
  }


  //#########################################################################
  //# Inner Class SplitCandidate
  private class SplitCandidate implements Comparable<SplitCandidate> {

    //#######################################################################
    //# Constructors
    private SplitCandidate(final EFAVariable var)
    {
      mVariable = var;
      mRangeSize = var.getRange().size();
      mNumCombinations = 0;
    }

    //#######################################################################
    //# Interface java.lang.Comparable
    public int compareTo(final SplitCandidate candidate)
    {
      if (mNumCombinations != candidate.mNumCombinations) {
        return candidate.mNumCombinations - mNumCombinations;
      } else if (mRangeSize != candidate.mRangeSize) {
        return mRangeSize - candidate.mRangeSize;
      } else {
        return mVariable.compareTo(candidate.mVariable);
      }
    }

    //#######################################################################
    //# Simple Access
    private EFAVariable getVariable()
    {
      return mVariable;
    }

    private int getRangeSize()
    {
      return mRangeSize;
    }

    private int getNumCombinations()
    {
      return mNumCombinations;
    }

    private void addCombination()
    {
      mNumCombinations++;
    }

    //#######################################################################
    //# Data Members
    private final EFAVariable mVariable;
    private final int mRangeSize;
    private int mNumCombinations;

  }


  //#########################################################################
  //# Inner Class IndexSplitVisitor
  private class IndexSplitVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    EFAVariable getIndexSplit(final CompiledClause clause)
    {
      try {
        mResult = null;
        mInIndex = false;
        for (final SimpleExpressionProxy literal : clause.getLiterals()) {
          literal.acceptVisitor(this);
        }
        return mResult;
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }


    //#######################################################################
    //# Auxiliary Methods
    void collect(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      if (mInIndex) {
        final EFAVariable var = mContext.getVariable(expr);
        if (var != null) {
          if (mResult == null || var.compareTo(mResult) < 0) {
            mResult = var;
            return;
          }
        }
      }
      expr.acceptVisitor(this);
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy lhs = expr.getLeft();
      collect(lhs);
      final SimpleExpressionProxy rhs = expr.getRight();
      collect(rhs);
      return null;
    }

    public Object visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      final boolean save = mInIndex;
      mInIndex = true;
      try {
        final List<SimpleExpressionProxy> indexes = ident.getIndexes();
        for (final SimpleExpressionProxy index : indexes) {
          collect(index);
        }
      } finally {
        mInIndex = save;
      }
      return null;
    }

    public Object visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      base.acceptVisitor(this);
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return comp.acceptVisitor(this);
    }

    public Object visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return null;
    }

    public Object visitUnaryExpressionProxy
      (final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      collect(subterm);
      return null;
    }

    //#######################################################################
    //# Data Members
    private boolean mInIndex;
    private EFAVariable mResult;

  }


  //#########################################################################
  //# Inner Class EFAVariableCollector
  private class EFAVariableCollector
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Constructor
    private EFAVariableCollector()
    {
      mMap = new HashMap<ProxyAccessor<SimpleExpressionProxy>,EFAVariable>();
    }

    //#######################################################################
    //# Invocation
    Collection<EFAVariable> collect(final SimpleExpressionProxy expr)
    {
      try {
        process(expr);
        final Collection<EFAVariable> values = mMap.values();
        return new ArrayList<EFAVariable>(values);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      } finally {
        mMap.clear();
      }
    }


    //#######################################################################
    //# Auxiliary Methods
    void process(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      final ProxyAccessor<SimpleExpressionProxy> accessor =
        new ProxyAccessorByContents<SimpleExpressionProxy>(expr);
      final EFAVariable var = mContext.getVariable(accessor);
      if (var == null) {
        expr.acceptVisitor(this);
      } else {
        mMap.put(accessor, var);
      }
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy lhs = expr.getLeft();
      process(lhs);
      final SimpleExpressionProxy rhs = expr.getRight();
      process(rhs);
      return null;
    }

    public Object visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      final List<SimpleExpressionProxy> indexes = ident.getIndexes();
      for (final SimpleExpressionProxy index : indexes) {
        process(index);
      }
      return null;
    }

    public Object visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      base.acceptVisitor(this);
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return comp.acceptVisitor(this);
    }

    public Object visitSimpleExpressionProxy
      (final SimpleExpressionProxy expr)
    {
      return null;
    }

    public Object visitUnaryExpressionProxy
      (final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy subterm = expr.getSubTerm();
      process(subterm);
      return null;
    }

    //#######################################################################
    //# Data Members
    private final Map<ProxyAccessor<SimpleExpressionProxy>,EFAVariable> mMap;

  }


  //#########################################################################
  //# Data Members
  private final IndexSplitVisitor mIndexSplitVisitor;
  private final EFAVariableCollector mVariableCollector;
  private final EFAModuleContext mContext;
  private final Map<EFAVariable,SplitCandidate> mCandidateMap;
  private final List<SplitCandidate> mCandidateList;

  private long mBestCost;
  private List<SplitCandidate> mBestSolution;


  //#########################################################################
  //# Class Constants
  private static final long MAX_COST = 0x100000;

}
