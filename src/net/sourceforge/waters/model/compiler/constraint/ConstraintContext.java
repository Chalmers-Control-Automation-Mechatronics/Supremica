//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   ConstraintContext
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class ConstraintContext implements VariableContext
{

  //#########################################################################
  //# Constructor
  ConstraintContext(final VariableContext parent,
                    final ConstraintPropagator propagator)
  {
    final int size = parent.getVariableNames().size();
    mParent = parent;
    mPropagator = propagator;
    mRestrictions =
      new HashMap<ProxyAccessor<SimpleExpressionProxy>,CompiledRange>(size);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
  public SimpleExpressionProxy getBoundExpression
    (final SimpleExpressionProxy ident)
  {
    final CompiledRange range = getVariableRange(ident);
    if (range != null) {
      switch (range.size()) {
      case 0:
        mPropagator.setFalse();
        break;
      case 1:
        final List<? extends SimpleExpressionProxy> values = range.getValues();
        final SimpleExpressionProxy value = values.iterator().next();
        return value;
      default:
        break;
      }
    }
    return mParent.getBoundExpression(ident);    
  }

  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    return mParent.isEnumAtom(ident);
  }

  public ModuleBindingContext getModuleBindingContext()
  {
    return mParent.getModuleBindingContext();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.VariableContext
  public CompiledRange getVariableRange(final SimpleExpressionProxy varname)
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
    return getVariableRange(accessor);
  }

  public CompiledRange getVariableRange
    (final ProxyAccessor<SimpleExpressionProxy> accessor)
  {
    final CompiledRange range = mRestrictions.get(accessor);
    if (range != null) {
      return range;
    } else {
      return mParent.getVariableRange(accessor);
    }
  }

  public Collection<SimpleExpressionProxy> getVariableNames()
  {
    return mParent.getVariableNames();
  }


  //#########################################################################
  //# Range Modifications
  void merge(final ConstraintContext context)
  {
    for (final SimpleExpressionProxy varname : context.getVariableNames()) {
      final ProxyAccessor<SimpleExpressionProxy> accessor =
        new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
      final CompiledRange ours = getVariableRange(accessor);
      assert ours != null :
        "Attempting to merge undefined range for " + varname + "!";
      final CompiledRange theirs = context.getVariableRange(accessor);
      if (ours != theirs) {
        final CompiledRange intersection = ours.intersection(theirs);
        if (ours != intersection) {
          mRestrictions.put(accessor, intersection);
        }
      }
    }
  }

  boolean restrictRange(final SimpleExpressionProxy varname,
                        final CompiledRange restriction)
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
    final CompiledRange current = getVariableRange(accessor);
    assert current != null :
      "Attempting to restrict undefined range for " + varname + "!";
    final CompiledRange intersection = current.intersection(restriction);
    if (current != intersection) {
      // System.err.println
      //   ("RESTRICT " + varname + ": " + current + " >> " + intersection);
      mRestrictions.put(accessor, intersection);
      return true;
    } else {
      return false;
    }
  }

  CompiledRange getOriginalRange(final SimpleExpressionProxy varname)
  {
    if (mParent instanceof ConstraintContext) {
      final ConstraintContext parent = (ConstraintContext) mParent;
      return parent.getOriginalRange(varname);
    } else {
      return mParent.getVariableRange(varname);
    }
  }


  //#########################################################################
  //# Data Members
  private final VariableContext mParent;
  private final ConstraintPropagator mPropagator;
  private final Map<ProxyAccessor<SimpleExpressionProxy>,CompiledRange>
    mRestrictions;

}