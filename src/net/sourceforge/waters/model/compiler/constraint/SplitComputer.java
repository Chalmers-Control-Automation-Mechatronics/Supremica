//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.ExpressionComparator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.DescendingModuleProxyVisitor;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
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
    mCollector = new IndexDistinguishingVariableCollector();
    mEquality = new ModuleEqualityVisitor(false);
    mExpressionComparator = new ExpressionComparator(optable);
    mCandidateComparator = new CandidateComparator();
    mCombinations = new THashSet<>();
    mCandidateMap = new ProxyAccessorHashMap<>(mEquality);
  }


  //#########################################################################
  //# Invocation
  public SplitCandidate proposeSplit(final ConstraintList constraints,
                                     final VariableContext context)
    throws EvalException
  {
    try {
      mContext = context;
      boolean hasIndexVars = false;
      for (final SimpleExpressionProxy constraint :
             constraints.getConstraints()) {
        final ProxyAccessorSet<SimpleExpressionProxy> collection =
          mCollector.collect(constraint, hasIndexVars);
        if (!hasIndexVars && mCollector.hasIndexVariables()) {
          mCombinations.clear();
          hasIndexVars = true;
        }
        final VariableCombination comb =
          createVariableCombination(collection, hasIndexVars);
        if (comb != null) {
          mCombinations.add(comb);
        }
      }
      for (final VariableCombination comb : mCombinations) {
        createVariableSplitCandidates(comb);
      }
      if (mCandidateMap.isEmpty()) {
        return null;
      } else {
        final Collection<VariableSplitCandidate> candidates =
          mCandidateMap.values();
        return Collections.min(candidates, mCandidateComparator);
      }
    } finally {
      mContext = null;
      mCombinations.clear();
      mCandidateMap.clear();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private VariableCombination createVariableCombination
    (final ProxyAccessorSet<SimpleExpressionProxy> contents,
     final boolean hasIndexVars)
  {
    if (contents == null || contents.isEmpty()) {
      return null;
    } else if (hasIndexVars) {
      return new VariableCombination(contents);
    } else {
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
  }

  private void createVariableSplitCandidates(final VariableCombination comb)
  {
    final UnaryOperator nextOp = mOperatorTable.getNextOperator();
    for (final SimpleExpressionProxy varName : comb.getVariables()) {
      final VariableSplitCandidate cand =
        createVariableSplitCandidate(varName);
      cand.addOccurrence();
      if (varName instanceof IdentifierProxy) {
        final UnaryExpressionProxy nextVarName =
          mFactory.createUnaryExpressionProxy(nextOp, varName);
        if (comb.contains(nextVarName)) {
          cand.setOccursWithNext();
        }
      } else if (varName instanceof UnaryExpressionProxy) {
        final UnaryExpressionProxy unary = (UnaryExpressionProxy) varName;
        final SimpleExpressionProxy subTerm = unary.getSubTerm();
        if (comb.contains(subTerm)) {
          cand.setOccursWithNext();
        }
      }
    }
  }


  private VariableSplitCandidate createVariableSplitCandidate
    (final SimpleExpressionProxy varName)
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      mCandidateMap.createAccessor(varName);
    VariableSplitCandidate cand = mCandidateMap.get(accessor);
    if (cand == null) {
      final CompiledRange range = mContext.getVariableRange(varName);
      cand = new VariableSplitCandidate(varName, range);
      mCandidateMap.put(accessor, cand);
    }
    return cand;
  }

  private boolean isNextOf(final SimpleExpressionProxy expr,
                           final SimpleExpressionProxy varname)
  {
    if (expr instanceof UnaryExpressionProxy) {
      final UnaryExpressionProxy unary = (UnaryExpressionProxy) expr;
      if (unary.getOperator() != mOperatorTable.getNextOperator()) {
        return false;
      }
      final SimpleExpressionProxy subterm = unary.getSubTerm();
      return mEquality.equals(subterm, varname);
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Inner Class CandidateComparator
  private class CandidateComparator
    implements Comparator<VariableSplitCandidate>
  {

    //#######################################################################
    //# Interface java.util.Comparator
    @Override
    public int compare(final VariableSplitCandidate cand1,
                       final VariableSplitCandidate cand2)
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
  //# Inner Class VariableCollector
  /**
   * A visitor that collects the variables in an expression.
   */
  private class VariableCollector
    extends DescendingModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    ProxyAccessorSet<SimpleExpressionProxy>
      collect(final SimpleExpressionProxy expr)
      throws VisitorException
    {
      try {
        expr.acceptVisitor(this);
        return mCollection;
      } finally {
        mCollection = null;
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Boolean visitIdentifierProxy(final IdentifierProxy ident)
      throws VisitorException
    {
      if (mInQualification) {
        return true;
      } else if (mContext.isEnumAtom(ident)) {
        return true;
      } else if (mContext.getVariableRange(ident) != null) {
        if (mPrimedExpression != null) {
          assert mPrimedExpression.getSubTerm() == ident;
          addOrdinaryVariable(mPrimedExpression);
        } else {
          addOrdinaryVariable(ident);
        }
        return true;
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
      if (mIndexCollector == null) {
        mIndexCollector = new VariableCollector();
      }
      boolean ground = true;
      for (final SimpleExpressionProxy index : ident.getIndexes()) {
        final ProxyAccessorSet<SimpleExpressionProxy> indexVars =
          mIndexCollector.collect(index);
        ground &= indexVars.isEmpty();
        addIndexVariables(indexVars);
      }
      if (ground) {
        return visitIdentifierProxy(ident);
      } else {
        return false;
      }
    }

    @Override
    public Boolean visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      boolean ground;
      if (mInQualification) {
        final IdentifierProxy base = ident.getBaseIdentifier();
        final Object r = base.acceptVisitor(this);
        ground = (Boolean) r;
        final IdentifierProxy comp = ident.getComponentIdentifier();
        ground &= (Boolean) comp.acceptVisitor(this);
      } else {
        try {
          mInQualification = true;
          ground = visitQualifiedIdentifierProxy(ident);
        } finally {
          mInQualification = false;
        }
        if (ground) {
          return visitIdentifierProxy(ident);
        }
      }
      return ground;
    }

    @Override
    public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy subTerm = expr.getSubTerm();
      if (expr.getOperator() == mOperatorTable.getNextOperator()) {
        try {
          assert mPrimedExpression == null;
          mPrimedExpression = expr;
          return subTerm.acceptVisitor(this);
        } finally {
          mPrimedExpression = null;
        }
      } else {
        return subTerm.acceptVisitor(this);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    void addIndexVariables
      (final ProxyAccessorSet<SimpleExpressionProxy> collection)
    {
      if (collection != null) {
        if (mCollection == null) {
          mCollection = new ProxyAccessorHashSet<>(mEquality);
        }
        for (final SimpleExpressionProxy expr : collection.values()) {
          mCollection.addProxy(expr);
        }
      }
    }

    void addOrdinaryVariable(final SimpleExpressionProxy expr)
    {
      if (mCollection == null) {
        mCollection = new ProxyAccessorHashSet<>(mEquality);
      }
      mCollection.addProxy(expr);
    }

    void resetCollection()
    {
      if (mCollection != null) {
        mCollection.clear();
      }
    }

    //#######################################################################
    //# Data Members
    private VariableCollector mIndexCollector = null;
    private ProxyAccessorSet<SimpleExpressionProxy> mCollection = null;
    private boolean mInQualification = false;
    private UnaryExpressionProxy mPrimedExpression = null;
  }


  //#########################################################################
  //# Inner Class IndexDistinguishingVariableCollector
  private class IndexDistinguishingVariableCollector
    extends VariableCollector
  {
    //#######################################################################
    //# Invocation
    ProxyAccessorSet<SimpleExpressionProxy>
    collect(final SimpleExpressionProxy expr, final boolean indexOnly)
      throws EvalException
    {
      try {
        mHasIndexVariables = indexOnly;
        return super.collect(expr);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }

    boolean hasIndexVariables()
    {
      return mHasIndexVariables;
    }

    //#######################################################################
    //# Auxiliary Methods
    @Override
    void addIndexVariables
      (final ProxyAccessorSet<SimpleExpressionProxy> collection)
    {
      if (collection != null) {
        if (!mHasIndexVariables) {
          resetCollection();
          mHasIndexVariables = true;
        }
        super.addIndexVariables(collection);
      }
    }

    @Override
    void addOrdinaryVariable(final SimpleExpressionProxy expr)
    {
      if (!mHasIndexVariables) {
        super.addOrdinaryVariable(expr);
      }
    }

    //#######################################################################
    //# Data Members
    private boolean mHasIndexVariables = false;
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
  private final IndexDistinguishingVariableCollector
    mCollector;
  private final ModuleEqualityVisitor mEquality;
  private final Comparator<SimpleExpressionProxy> mExpressionComparator;
  private final Comparator<VariableSplitCandidate> mCandidateComparator;
  private final Set<VariableCombination> mCombinations;
  private final ProxyAccessorMap<SimpleExpressionProxy,VariableSplitCandidate>
    mCandidateMap;

  private VariableContext mContext;

}
