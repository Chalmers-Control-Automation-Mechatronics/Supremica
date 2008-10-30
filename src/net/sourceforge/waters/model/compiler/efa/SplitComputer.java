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
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class SplitComputer
{

  //#########################################################################
  //# Constructors
  SplitComputer
    (final Map<ProxyAccessor<SimpleExpressionProxy>,EFAVariable> variables)
  {
    final int size = variables.size();
    mVariablesMap = variables;
    mCandidatesMap =
      new HashMap<ProxyAccessor<SimpleExpressionProxy>,SplitCandidate>(size);
    mCandidatesList = new ArrayList<SplitCandidate>(size);
  }


  //#########################################################################
  //# Invocation
  List<EFAVariable> computeSplitList
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
        final List<EFAVariable> result = new ArrayList<EFAVariable>(size);
        for (final SplitCandidate cand : mBestSolution) {
          final EFAVariable var = cand.getVariable();
          result.add(var);
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
      final ProxyAccessorMap<SimpleExpressionProxy> openvars =
        new ProxyAccessorHashMapByContents<SimpleExpressionProxy>();
      for (final EFAVariableCombination combination : open) {
        final ProxyAccessorMap<SimpleExpressionProxy> contents =
          combination.getContentsMap();
        openvars.putAll(contents);
      }
      final int numcandidates = mCandidatesList.size();
      for (int index = start; index < numcandidates; index++) {
        final SplitCandidate cand = mCandidatesList.get(index);
        final SimpleExpressionProxy varname = cand.getVariableName();
        if (!openvars.containsProxy(varname)) {
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
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
    SplitCandidate cand = mCandidatesMap.get(accessor);
    if (cand == null) {
      final EFAVariable var = mVariablesMap.get(accessor);
      cand = new SplitCandidate(var);
      mCandidatesMap.put(accessor, cand);
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
    private SimpleExpressionProxy getVariableName()
    {
      return mVariable.getVariableName();
    }

    private boolean isNext()
    {
      return mVariable.isNext();
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
    private final EFAVariable mVariable;
    private final int mRangeSize;
    private int mNumCombinations;

  }


  //#########################################################################
  //# Data Members
  private final Map<ProxyAccessor<SimpleExpressionProxy>,EFAVariable>
    mVariablesMap;
  private final Map<ProxyAccessor<SimpleExpressionProxy>,SplitCandidate>
    mCandidatesMap;
  private final List<SplitCandidate> mCandidatesList;

  private long mBestCost;
  private List<SplitCandidate> mBestSolution;


  //#########################################################################
  //# Class Constants
  private static final long MAX_COST = 0x100000;

}
