//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFAModuleContext
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.
  UndefinedIdentifierException;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * The binding context used by the EFA compiler.
 *
 * In addition to bindings of enumeration atom, the EFA context includes a
 * symbol table contains with all variables mantained by the EFA compiler
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

class EFAModuleContext
  extends ModuleBindingContext
  implements VariableContext
{

  //#########################################################################
  //# Constructors
  EFAModuleContext(final ModuleProxy module)
  {
    super(module);
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
    final int size = 2 * module.getComponentList().size();
    mMap =
      new ProxyAccessorHashMap<SimpleExpressionProxy,EFAVariable>(eq, size);
    mVariableNameSet = new VariableNameSet();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
    final EFAVariable var = getVariable(varname);
    if (var == null) {
      return null;
    } else {
      return var.getRange();
    }
  }

  public Set<SimpleExpressionProxy> getVariableNames()
  {
    return mVariableNameSet;
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

  void createVariables(final ComponentProxy comp,
                       final CompiledRange range,
                       final ModuleProxyFactory factory,
                       final CompilerOperatorTable optable)
  {
    final EFAVariable curvar =
      new EFAVariable(false, comp, range, factory, optable);
    final SimpleExpressionProxy curvarname = curvar.getVariableName();
    mMap.putByProxy(curvarname, curvar);
    final EFAVariable nextvar =
      new EFAVariable(true, comp, range, factory, optable);
    final SimpleExpressionProxy nextvarname = nextvar.getVariableName();
    mMap.putByProxy(nextvarname, nextvar);
  }


  //#########################################################################
  //# Inner Class VariableNameSet
  private class VariableNameSet
    extends AbstractSet<SimpleExpressionProxy>
  {

    //#######################################################################
    //# Interface java.util.Set
    public int size()
    {
      return mMap.size();
    }

    public Iterator<SimpleExpressionProxy> iterator()
    {
      return new VariableNameIterator();
    }

  }


  //#########################################################################
  //# Inner Class VariableNameIterator
  private class VariableNameIterator
    implements Iterator<SimpleExpressionProxy>
  {

    //#######################################################################
    //# Constructor
    VariableNameIterator()
    {
      mMaster = mMap.keySet().iterator();
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mMaster.hasNext();
    }

    public SimpleExpressionProxy next()
    {
      final ProxyAccessor<SimpleExpressionProxy> accessor = mMaster.next();
      return accessor.getProxy();
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        ("Can't remove variables through variable name iteration!");
    }

    //#######################################################################
    //# Data Members
    private final Iterator<ProxyAccessor<SimpleExpressionProxy>> mMaster;

  }


  //#########################################################################
  //# Data Members
  private final ProxyAccessorMap<SimpleExpressionProxy,EFAVariable> mMap;
  private final VariableNameSet mVariableNameSet;

}
