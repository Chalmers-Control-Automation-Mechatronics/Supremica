//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   DummyContext
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.constraint;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.ModuleBindingContext;
import net.sourceforge.waters.model.compiler.context.VariableContext;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class DummyContext implements VariableContext
{

  //#########################################################################
  //# Constructor
  DummyContext()
  {
    final ModuleEqualityVisitor eq = ModuleEqualityVisitor.getInstance(false);
    mAtoms = new ProxyAccessorHashSet<IdentifierProxy>(eq);
    mRangeMap =
      new ProxyAccessorHashMap<SimpleExpressionProxy,CompiledRange>(eq);
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
   return mRangeMap.getByProxy(varname);
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
    mRangeMap.putByProxy(varname, range);
  }


  //#########################################################################
  //# Inner Class VariableNameSet
  private class VariableNameSet
    extends AbstractSet<SimpleExpressionProxy>
  {

    //#######################################################################
    //# Constructor
    VariableNameSet
      (final ProxyAccessorMap<SimpleExpressionProxy,CompiledRange> map)
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
    private final ProxyAccessorMap<SimpleExpressionProxy,CompiledRange> mMap;

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
  private final ProxyAccessorSet<IdentifierProxy> mAtoms;
  private final ProxyAccessorMap<SimpleExpressionProxy,CompiledRange>
    mRangeMap;

}
