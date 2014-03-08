//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   Expression class is undefined on line 10, column 16 in Templates/Classes/Class.java.
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import java.util.Collection;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAVariableContext;
import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.VariableComponentProxy;

/**
 * An implementation of the {@link AbstractEFAVariableContext}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAVariableContext
 extends AbstractEFAVariableContext<SimpleEFATransitionLabel, SimpleEFAVariable>
{

  public SimpleEFAVariableContext(final ModuleProxy module,
                                  final CompilerOperatorTable op,
                                  final ModuleProxyFactory factory)
  {
    super(module, op);
    mFactory = factory;
    mOp = op;
  }

  public SimpleEFAVariable createVariables(final VariableComponentProxy comp,
                                           final CompiledRange range)
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
    return var;
  }

  public Collection<SimpleEFAVariable> getVariables()
  {
    return mGlobalVariableMap.values();
  }

  public SimpleEFAVariable getVariable(final IdentifierProxy proxy)
  {
    return mGlobalVariableMap.getByProxy(proxy);
  }

  private final ModuleProxyFactory mFactory;
  private final CompilerOperatorTable mOp;
}
