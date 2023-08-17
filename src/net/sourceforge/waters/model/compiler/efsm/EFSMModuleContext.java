//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.model.compiler.efsm;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * <P>The binding context used by the EFSM compiler.</P>
 *
 * <P>In addition to bindings of enumeration atoms, the EFSM module context
 * includes a symbol table with all variables maintained by the EFSM compiler
 * and their range. These are the variables that occur in guards and
 * represent the current state of EFSM components such as variables ({@link
 * net.sourceforge.waters.model.module.VariableComponentProxy
 * VariableComponentProxy}) or automata {@link
 * net.sourceforge.waters.model.module.SimpleComponentProxy
 * SimpleComponentProxy}). The symbol table maps expressions ({@link
 * SimpleExpressionProxy}) representing variable names to variable objects
 * ({@link EFSMComponent}) containing the the computed range of its state
 * space.</P>
 *
 * @see EFSMCompiler
 * @author Robi Malik
 */

public class EFSMModuleContext
  extends ModuleBindingContext
  implements VariableContext
{

  //#########################################################################
  //# Constructors
  public EFSMModuleContext(final ModuleProxy module,
                           final CompilerOperatorTable optable)
  {
    this(module, null, null, optable);
  }

  public EFSMModuleContext(final ModuleProxy module,
                           final IdentifierProxy prefix,
                           final SourceInfo info,
                           final CompilerOperatorTable optable)
  {
    super(module, prefix, info, null);
    mNextOperator = optable.getNextOperator();
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    final int size = module.getComponentList().size();
    mMap = new ProxyAccessorHashMap<>(eq, size);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
  @Override
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
    final EFSMComponent var = getEFSMComponent(varname);
    if (var == null) {
      return null;
    } else {
      return var.getRange();
    }
  }

  @Override
  public int getNumberOfVariables()
  {
    return mMap.size();
  }


  //#########################################################################
  //# Simple Access
  EFSMComponent getEFSMComponent(final SimpleExpressionProxy varname)
  {
    return mVariableLookupVisitor.getEFSMComponent(varname);
  }

  EFSMComponent getMentionedEFSMComponent(final SimpleExpressionProxy expr)
  {
    return mVariableSearchVisitor.getEFSMComponent(expr);
  }

  EFSMComponent findEFSMComponent(final SimpleExpressionProxy varname)
    throws UndefinedIdentifierException
  {
    final EFSMComponent var = getEFSMComponent(varname);
    if (var == null) {
      throw new UndefinedIdentifierException(varname, "variable");
    } else {
      return var;
    }
  }

  void insertEFSMComponent(final EFSMComponent comp)
    throws DuplicateIdentifierException
  {
    final IdentifierProxy ident = comp.getIdentifier();
    if (getBoundExpression(ident) != null) {
      throw new DuplicateIdentifierException(ident);
    }
    final ProxyAccessor<IdentifierProxy> accessor = mMap.createAccessor(ident);
    if (mMap.containsKey(accessor)) {
      throw new DuplicateIdentifierException(ident);
    }
    mMap.put(accessor, comp);
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.compiler.context.ModuleBindingContext
  @Override
  public void insertBinding(final SimpleExpressionProxy ident,
                            final SimpleExpressionProxy value)
    throws DuplicateIdentifierException
  {
    if (!(ident instanceof IdentifierProxy)) {
      super.insertBinding(ident, value);
    } else if (mMap.containsProxyKey((IdentifierProxy) ident)) {
      throw new DuplicateIdentifierException(ident);
    } else {
      super.insertBinding(ident, value);
    }
  }

  @Override
  public void insertEnumAtom(final SimpleIdentifierProxy ident)
    throws DuplicateIdentifierException, TypeMismatchException
  {
    if (mMap.containsProxyKey(ident)) {
      throw new DuplicateIdentifierException(ident);
    } else {
      super.insertEnumAtom(ident);
    }
  }


  //#########################################################################
  //# Inner Class VariableLookupVisitor
  private class VariableLookupVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private EFSMComponent getEFSMComponent(final SimpleExpressionProxy varname)
    {
      try {
        return (EFSMComponent) varname.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public EFSMComponent visitIdentifierProxy(final IdentifierProxy ident)
    {
      return mMap.getByProxy(ident);
    }

    @Override
    public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return null;
    }

    @Override
    public EFSMComponent visitUnaryExpressionProxy
      (final UnaryExpressionProxy expr)
    {
      if (expr.getOperator() == mNextOperator) {
        final SimpleExpressionProxy subTerm = expr.getSubTerm();
        if (subTerm instanceof IdentifierProxy) {
          final IdentifierProxy ident = (IdentifierProxy) subTerm;
          return visitIdentifierProxy(ident);
        }
      }
      return null;
    }
  }


  //#########################################################################
  //# Inner Class VariableSearchVisitor
  private class VariableSearchVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private EFSMComponent getEFSMComponent(final SimpleExpressionProxy expr)
    {
      try {
        return (EFSMComponent) expr.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitBinaryExpressionProxy(final BinaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy left = expr.getLeft();
      final Object result = left.acceptVisitor(this);
      if (result != null) {
        return result;
      }
      final SimpleExpressionProxy right = expr.getRight();
      return right.acceptVisitor(this);
    }

    @Override
    public Object visitFunctionCallExpressionProxy
      (final FunctionCallExpressionProxy expr)
      throws VisitorException
    {
      for (final SimpleExpressionProxy arg : expr.getArguments()) {
        final Object result = arg.acceptVisitor(this);
        if (result != null) {
          return result;
        }
      }
      return null;
    }

    @Override
    public EFSMComponent visitIdentifierProxy(final IdentifierProxy ident)
    {
      return mMap.getByProxy(ident);
    }

    @Override
    public Object visitSimpleExpressionProxy(final SimpleExpressionProxy expr)
    {
      return null;
    }

    @Override
    public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
      throws VisitorException
    {
      final SimpleExpressionProxy subTerm = expr.getSubTerm();
      return subTerm.acceptVisitor(this);
    }
  }


  //#########################################################################
  //# Data Members
  private final UnaryOperator mNextOperator;
  private final VariableLookupVisitor mVariableLookupVisitor =
    new VariableLookupVisitor();
  private final VariableSearchVisitor mVariableSearchVisitor =
    new VariableSearchVisitor();

  private final ProxyAccessorMap<IdentifierProxy,EFSMComponent> mMap;

}
