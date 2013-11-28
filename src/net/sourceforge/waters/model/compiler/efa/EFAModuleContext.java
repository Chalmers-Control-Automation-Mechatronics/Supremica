//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAModuleContext
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * The binding context used by the EFA compiler.
 *
 * In addition to bindings of enumeration atoms, the EFA module context
 * includes a symbol table with all variables maintained by the EFA compiler
 * and their range. These are the variables that occur in guards and
 * represent the current state of EFA components such as variables ({@link
 * net.sourceforge.waters.model.module.VariableComponentProxy
 * VariableComponentProxy}) or automata {@link
 * net.sourceforge.waters.model.module.SimpleComponentProxy
 * SimpleComponentProxy}). The symbol table maps expressions ({@link
 * SimpleExpressionProxy}) representing variable names to variable objects
 * ({@link EFAVariable}) containing the the computed range of its state
 * space. The table contains entries for the current and the next state of
 * each variable.
 *
 * @see EFACompiler
 * @author Robi Malik
 */

public class EFAModuleContext
  extends ModuleBindingContext
  implements VariableContext
{

  //#########################################################################
  //# Constructors
  public EFAModuleContext(final ModuleProxy module)
  {
    this(module, null, null);
  }

  public EFAModuleContext(final ModuleProxy module,
                          final IdentifierProxy prefix,
                          final SourceInfo info)
  {
    super(module, prefix, info);
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
    final int size = 2 * module.getComponentList().size();
    mMap =
      new ProxyAccessorHashMap<SimpleExpressionProxy,EFAVariable>(eq, size);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
  @Override
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
    final EFAVariable var = getVariable(varname);
    if (var == null) {
      return null;
    } else {
      return var.getRange();
    }
  }

  @Override
  public int getNumberOfVariables()
  {
    return mMap.size() >> 1;
  }


  //#########################################################################
  //# Simple Access
  EFAVariable getVariable(final SimpleExpressionProxy varname)
  {
    return mMap.getByProxy(varname);
  }

  EFAVariable findVariable(final SimpleExpressionProxy varname)
    throws UndefinedIdentifierException
  {
    final EFAVariable var = getVariable(varname);
    if (var == null) {
      throw new UndefinedIdentifierException(varname, "variable");
    } else {
      return var;
    }
  }

  /**
   * Creates current and next-state variables for the given component.
   * @param  comp   Component (variable or automaton) to create variables for.
   * @param  range  Computed range of the variables.
   */
  public void createVariables(final ComponentProxy comp,
                              final CompiledRange range,
                              final ModuleProxyFactory factory,
                              final CompilerOperatorTable optable)
    throws DuplicateIdentifierException
  {
    final IdentifierProxy ident = comp.getIdentifier();
    if (getBoundExpression(ident) != null) {
      throw new DuplicateIdentifierException(ident);
    }
    final EFAVariable var =
      new EFAVariable(false, comp, range, factory, optable);
    final ProxyAccessor<SimpleExpressionProxy> key =
      mMap.createAccessor(ident);
    if (mMap.containsKey(key)) {
      throw new DuplicateIdentifierException(ident);
    }
    mMap.put(key, var);
    final EFAVariable nextvar =
      new EFAVariable(true, comp, range, factory, optable);
    final SimpleExpressionProxy nextident = nextvar.getVariableName();
    mMap.putByProxy(nextident, nextvar);
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.compiler.context.ModuleBindingContext
  @Override
  public void insertBinding(final SimpleExpressionProxy ident,
                            final SimpleExpressionProxy value)
    throws DuplicateIdentifierException
  {
    if (mMap.containsProxyKey(ident)) {
      throw new DuplicateIdentifierException(ident);
    } else {
      super.insertBinding(ident, value);
    }
  }

  @Override
  public void insertEnumAtom(final SimpleIdentifierProxy ident)
    throws DuplicateIdentifierException
  {
    if (mMap.containsProxyKey(ident)) {
      throw new DuplicateIdentifierException(ident);
    } else {
      super.insertEnumAtom(ident);
    }
  }


  //#########################################################################
  //# Data Members
  private final ProxyAccessorMap<SimpleExpressionProxy,EFAVariable> mMap;

}
