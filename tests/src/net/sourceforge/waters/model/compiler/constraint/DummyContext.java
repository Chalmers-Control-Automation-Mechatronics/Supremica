//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   DummyContext
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.constraint;

import junit.framework.TestCase;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class DummyContext implements VariableContext
{

  //#########################################################################
  //# Constructor
  DummyContext()
  {
    mAtoms = new ProxyAccessorHashMapByContents<IdentifierProxy>();
    mRangeMap =
      new HashMap<ProxyAccessor<SimpleExpressionProxy>,CompiledRange>();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.context.BindingContext
  public SimpleExpressionProxy getBoundExpression
    (final SimpleExpressionProxy ident)
  {
    return null;
  }

  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    return mAtoms.containsProxy(ident);
  }

  public ModuleBindingContext getModuleBindingContext()
  {
    return null;
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
    return mRangeMap.get(accessor);
  }

  public Collection<SimpleExpressionProxy> getVariableNames()
  {
    return new VariableNameSet(mRangeMap);
  }


  //#########################################################################
  //# Assignments
  void addAtom(final IdentifierProxy ident)
  {
    mAtoms.addProxy(ident);
  }

  void addVariable(final SimpleExpressionProxy varname,
                   final CompiledRange range)
  {
    final ProxyAccessor<SimpleExpressionProxy> accessor =
      new ProxyAccessorByContents<SimpleExpressionProxy>(varname);
    mRangeMap.put(accessor, range);
  }


  //#########################################################################
  //# Inner Class VariableNameSet
  private class VariableNameSet
    extends AbstractSet<SimpleExpressionProxy>
  {

    //#######################################################################
    //# Constructor
    VariableNameSet
      (final Map<ProxyAccessor<SimpleExpressionProxy>,CompiledRange> map)
    {
      mMap = map;
    }

    //#######################################################################
    //# Interface java.util.Set
    public int size()
    {
      return mMap.size();
    }

    public Iterator<SimpleExpressionProxy> iterator()
    {
      return new VariableNameIterator(mMap.keySet().iterator());
    }

    //#######################################################################
    //# Data Members
    private final Map<ProxyAccessor<SimpleExpressionProxy>,CompiledRange> mMap;

  }


  //#########################################################################
  //# Inner Class VariableNameIterator
  private class VariableNameIterator
    implements Iterator<SimpleExpressionProxy>
  {

    //#######################################################################
    //# Constructor
    VariableNameIterator
      (final Iterator<ProxyAccessor<SimpleExpressionProxy>> master)
    {
      mMaster = master;
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
  private final ProxyAccessorMap<IdentifierProxy> mAtoms;
  private final Map<ProxyAccessor<SimpleExpressionProxy>,CompiledRange>
    mRangeMap;

}
