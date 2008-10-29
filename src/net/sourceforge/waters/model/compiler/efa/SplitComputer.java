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
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


class SplitComputer
{

  //#########################################################################
  //# Constructors
  SplitComputer
    (final Map<ProxyAccessor<IdentifierProxy>,EFAVariable> variables)
  {
    final int size = variables.size();
    mVariables = variables;
    mCandidatesMap =
      new HashMap<ProxyAccessor<SimpleExpressionProxy>,SplitCandidate>(size);
    mCandidatesList = new ArrayList<SplitCandidate>(size);
  }


  //#########################################################################
  //# Invocation
  List<SimpleExpressionProxy> computeSplitList
    (final Collection<EFAVariableCombination> combinations)
  {
    try {
      for (final EFAVariableCombination combination : combinations) {
        for (final SimpleExpressionProxy varname : combination.getContents()) {
          final SplitCandidate cand = createCandidate(varname);
          cand.addCombination();
        }
      }
      Collections.sort(mCandidatesList);
      final int numcandidates = mCandidatesList.size();
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
        final List<SimpleExpressionProxy> result =
          new ArrayList<SimpleExpressionProxy>(size);
        for (final SplitCandidate cand : mBestSolution) {
          final SimpleExpressionProxy varname = cand.getVariableName();
          result.add(varname);
        }
        return result;
      }
    } finally {
      mCandidatesMap.clear();
      mCandidatesList.clear();
    }
  }


  //#########################################################################
  //# Algorithm
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
      final int numcandidates = mCandidatesList.size();
      for (int index = start; index < numcandidates; index++) {
        final SplitCandidate cand = mCandidatesList.get(index);
        final int rangesize = cand.getRangeSize();
        final long nextcost = cost * rangesize;
        if (nextcost >= mBestCost) {
          continue;
        }
        final SimpleExpressionProxy varname = cand.getVariableName();
        boolean contained = false;
        for (final EFAVariableCombination combination : open) {
          if (combination.contains(varname)) {
            contained = true;
            break;
          }
        }
        if (!contained) {
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
            combination.getReducedCombination(varname);
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
  private SplitCandidate createCandidate(final SimpleExpressionProxy varname)
  {
    final ProxyAccessor<SimpleExpressionProxy> vaccessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
    SplitCandidate cand = mCandidatesMap.get(vaccessor);
    if (cand == null) {
      final IdentifierProxy ident;
      if (varname instanceof IdentifierProxy) {
        ident = (IdentifierProxy) varname;
      } else if (varname instanceof UnaryExpressionProxy) {
        final UnaryExpressionProxy unary = (UnaryExpressionProxy) varname;
        ident = (IdentifierProxy) unary.getSubTerm();
      } else {
        throw new ClassCastException("Unsupported variable type: " + varname);
      }
      final ProxyAccessor<IdentifierProxy> iaccessor =
        new ProxyAccessorByContents<IdentifierProxy>(ident);
      final EFAVariable var = mVariables.get(iaccessor);
      cand = new SplitCandidate(varname, var);
      mCandidatesMap.put(vaccessor, cand);
      mCandidatesList.add(cand);
    }
    return cand;
  }

  private SplitCandidate getCandidate(final SimpleExpressionProxy varname)
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
    return mCandidatesMap.get(accessor);
  }


  //#########################################################################
  //# Inner Class SplitCandidate
  private class SplitCandidate implements Comparable<SplitCandidate> {

    //#######################################################################
    //# Constructors
    private SplitCandidate(final SimpleExpressionProxy varname,
                           final EFAVariable var)
    {
      mVariableName = varname;
      mIsNext = varname instanceof UnaryExpressionProxy;
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
      } else if (mIsNext != candidate.mIsNext) {
        return mIsNext ? 1 : -1;
      } else {
        return mVariable.compareTo(candidate.mVariable);
      }
    }

    //#######################################################################
    //# Simple Access
    private SimpleExpressionProxy getVariableName()
    {
      return mVariableName;
    }

    private boolean isNext()
    {
      return mIsNext;
    }

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
    private final SimpleExpressionProxy mVariableName;
    private final boolean mIsNext;
    private final EFAVariable mVariable;
    private final int mRangeSize;
    private int mNumCombinations;

  }


  //#########################################################################
  //# Data Members
  private final Map<ProxyAccessor<IdentifierProxy>,EFAVariable> mVariables;
  private final Map<ProxyAccessor<SimpleExpressionProxy>,SplitCandidate>
    mCandidatesMap;
  private final List<SplitCandidate> mCandidatesList;

  private long mBestCost;
  private List<SplitCandidate> mBestSolution;


  //#########################################################################
  //# Class Constants
  private static final long MAX_COST = 0x100000;

}
