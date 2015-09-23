//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import java.util.Collection;
import java.util.HashSet;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAVariableContext;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

/**
 * An implementation of the {@link AbstractEFAVariableContext}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAVariableContext extends AbstractEFAVariableContext<Integer, SimpleEFAVariable>
{

  public SimpleEFAVariableContext(final ModuleProxy module,
                                  final CompilerOperatorTable op,
                                  final ModuleProxyFactory factory)
  {
    super(module, op);
    mVariableEncoding = new SimpleInfoEncoder<>();
    mFactory = factory;
    mOp = op;
    mVarFinder = new SimpleEFAVariableFinder(op);
    mVarCollector = new SimpleEFAVariableCollector(op, this);
  }

  public int createVariables(final VariableComponentProxy comp, final CompiledRange range)
   throws DuplicateIdentifierException
  {
    final IdentifierProxy ident = comp.getIdentifier();
    if (getBoundExpression(ident) != null) {
      throw new DuplicateIdentifierException(ident);
    }
    final SimpleEFAVariable var =
     new SimpleEFAVariable(comp, range, mFactory, mOp);
    final ProxyAccessor<IdentifierProxy> key =
            mGlobalVariableMap.createAccessor(ident);
    if (mGlobalVariableMap.containsKey(key)) {
      throw new DuplicateIdentifierException(ident);
    }
    mGlobalVariableMap.put(key, var);
    int varId = mVariableEncoding.encode(var);
    return varId;
  }

  @Override
  public void addVariable(final SimpleEFAVariable var)
  {
    final IdentifierProxy ident = var.getVariableName();
    mGlobalVariableMap.putByProxy(ident, var);
    mVariableEncoding.encode(var);
  }

  public Collection<SimpleEFAVariable> getVariables()
  {
    return mGlobalVariableMap.values();
  }

  public SimpleEFAVariable getVariable(final IdentifierProxy proxy)
  {
    return mGlobalVariableMap.getByProxy(proxy);
  }

  public SimpleEFAVariable getVariable(final int varId)
  {
    return mVariableEncoding.decode(varId);
  }

  public Collection<SimpleEFAVariable> getVariables(final TIntSet varIds)
  {
    final HashSet<SimpleEFAVariable> vars = new HashSet<>(varIds.size());
    for (final int id : varIds.toArray()) {
      vars.add(getVariable(id));
    }
    return vars;
  }

  public int getVariableId(final SimpleEFAVariable var)
  {
    return mVariableEncoding.getInfoId(var);
  }

  public TIntArrayList getVariablesId(final Collection<SimpleEFAVariable> vars)
  {
    final TIntArrayList list = new TIntArrayList();
    if (vars != null && !vars.isEmpty()) {
      for (final SimpleEFAVariable var : vars) {
        list.add(getVariableId(var));
      }
    }
    return list;
  }

  public boolean findPrimeVariables(final ConstraintList constraints,
                                    final Collection<SimpleEFAVariable> vars)
  {
    for (final SimpleEFAVariable var : vars) {
      if (mVarFinder.findPrimeVariable(constraints, var)) {
        return true;
      }
    }
    return false;
  }

  public boolean findPrimeVariables(final SimpleExpressionProxy exp,
                                    final Collection<SimpleEFAVariable> vars)
  {
    for (final SimpleEFAVariable var : vars) {
      if (mVarFinder.findPrimeVariable(exp, var)) {
        return true;
      }
    }
    return false;
  }

  public boolean findPrimeVariables(final ConstraintList constraints,
                                    final TIntSet varIds)
  {
    for (final int varId : varIds.toArray()) {
      if (mVarFinder.findPrimeVariable(constraints, getVariable(varId))) {
        return true;
      }
    }
    return false;
  }

  public boolean findPrimeVariables(final SimpleExpressionProxy exp,
                                    final TIntSet varIds)
  {
    for (final int varId : varIds.toArray()) {
      if (mVarFinder.findPrimeVariable(exp, getVariable(varId))) {
        return true;
      }
    }
    return false;
  }

  public void collectAllVariables(final SimpleEFALabelEncoding encoding,
                                  final Collection<SimpleEFAVariable> vars)
  {
    collectAllVariables(encoding, vars, vars);
  }

  public void collectAllVariables(final SimpleEFALabelEncoding encoding,
                                  final Collection<SimpleEFAVariable> unprimed,
                                  final Collection<SimpleEFAVariable> primed)
  {
    for (final int label : encoding.getTransitionLabels()) {
      final ConstraintList constraint = encoding.getConstraint(label);
      mVarCollector.collectAllVariables(constraint, unprimed, primed);
    }
  }

  public void collectAllVariables(final ConstraintList constraints,
                                  final TIntSet vars)
  {
    final HashSet<SimpleEFAVariable> var = new HashSet<>();
    mVarCollector.collectAllVariables(constraints, var);
    for (final SimpleEFAVariable v : var) {
      vars.add(getVariableId(v));
    }
  }

  public void collectAllVariables(final SimpleExpressionProxy exp,
                                  final TIntSet unprimed,
                                  final TIntSet primed)
  {
    final HashSet<SimpleEFAVariable> prs = new HashSet<>();
    final HashSet<SimpleEFAVariable> unprs = new HashSet<>();
    mVarCollector.collectAllVariables(exp, unprs, prs);
    unprimed.addAll(getVariablesId(unprs));
    primed.addAll(getVariablesId(prs));
  }

  public void collectAllVariables(final ConstraintList constraint,
                                  final TIntSet unprimed,
                                  final TIntSet primed)
  {
    final HashSet<SimpleEFAVariable> prs = new HashSet<>();
    final HashSet<SimpleEFAVariable> unprs = new HashSet<>();
    for (final SimpleExpressionProxy exp : constraint.getConstraints()) {
      mVarCollector.collectAllVariables(exp, unprs, prs);
    }
    unprimed.addAll(getVariablesId(unprs));
    primed.addAll(getVariablesId(prs));
  }

  public void collectAllVariables(final SimpleEFALabelEncoding encoding,
                                  final TIntSet vars)
  {
    collectAllVariables(encoding, vars, vars);
  }

  public void collectAllVariables(final SimpleEFALabelEncoding encoding,
                                  final TIntSet unprimed,
                                  final TIntSet primed)
  {
    final HashSet<SimpleEFAVariable> prs = new HashSet<>();
    final HashSet<SimpleEFAVariable> unprs = new HashSet<>();
    for (final int label : encoding.getTransitionLabels()) {
      final ConstraintList update = encoding.getConstraint(label);
      mVarCollector.collectAllVariables(update, unprs, prs);
    }
    unprimed.addAll(getVariablesId(unprs));
    primed.addAll(getVariablesId(prs));
  }

  public boolean findPrimeVariable(final SimpleExpressionProxy expr, final int varId)
  {
    return mVarFinder.findPrimeVariable(expr, getVariable(varId));
  }

  public boolean findPrimeVariable(final ConstraintList constraints, final int varId)
  {
    return mVarFinder.findPrimeVariable(constraints, getVariable(varId));
  }

  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOp;
  private final SimpleInfoEncoder<SimpleEFAVariable> mVariableEncoding;
  private final SimpleEFAVariableFinder mVarFinder;
  private final SimpleEFAVariableCollector mVarCollector;
}
