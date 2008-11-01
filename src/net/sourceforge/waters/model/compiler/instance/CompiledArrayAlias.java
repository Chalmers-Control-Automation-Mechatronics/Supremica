//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   CompiledArrayAlias
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.compiler.context.
  DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.
  UndefinedIdentifierException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.IndexedIdentifierElement;
import net.sourceforge.waters.plain.module.SimpleIdentifierElement;


class CompiledArrayAlias implements CompiledEvent
{

  //#########################################################################
  //# Constructor
  CompiledArrayAlias(final String name)
  {
    mParentInfo = new RootParentInfo(name);
    mMap = new HashMap<SimpleExpressionProxy,CompiledEvent>();
  }

  CompiledArrayAlias(final CompiledArrayAlias parent,
                     final SimpleExpressionProxy index)
  {
    mParentInfo = new IndexedParentInfo(parent, index);
    mMap = new HashMap<SimpleExpressionProxy,CompiledEvent>();
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    return getIdentifier().toString();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.CompiledEvent
  public int getKindMask()
  {
    int mask = 0;
    for (final CompiledEvent event : mMap.values()) {
      mask |= event.getKindMask();
    }
    return mask;
  }

  public boolean isObservable()
  {
    boolean observable = true;
    for (final CompiledEvent event : mMap.values()) {
      observable &= event.isObservable();
    }
    return observable;
  }

  public List<CompiledRange> getIndexRanges()
  {
    return Collections.emptyList();
  }

  public CompiledEvent find(SimpleExpressionProxy index)
    throws UndefinedIdentifierException
  {
    final CompiledEvent result = get(index);
    if (result == null) {
      final ParentInfo info = new IndexedParentInfo(this, index);
      final String name = info.getName();
      throw new UndefinedIdentifierException(name);
    }
    return result;
  }

  public SourceInfo getSourceInfo()
  {
    return null;
  }

  public Iterator<CompiledEvent> getChildrenIterator()
  {
    return mMap.values().iterator();
  }


  //#########################################################################
  //# Specific Access
  IdentifierProxy getIdentifier()
  {
    return mParentInfo.getIdentifier();
  }

  CompiledEvent get(final SimpleExpressionProxy index)
  {
    return mMap.get(index);
  }

  void set(final SimpleExpressionProxy index, final CompiledEvent value)
    throws DuplicateIdentifierException
  {
    if (!mMap.containsKey(index)) {
      mMap.put(index, value);
    } else {
      final ParentInfo info = new IndexedParentInfo(this, index);
      final String name = info.getName();
      throw new DuplicateIdentifierException(name);
    }
  }

  void set(final List<SimpleExpressionProxy> indexes,
           final CompiledEvent value)
    throws DuplicateIdentifierException
  {
    if (indexes.isEmpty()) {
      throw new IllegalArgumentException
        ("Index list for new array alias must not be empty!");
    } else {
      final Iterator<SimpleExpressionProxy> iter = indexes.iterator();
      set(iter, value);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private ParentInfo getParentInfo()
  {
    return mParentInfo;
  }

  private void set(final Iterator<SimpleExpressionProxy> iter,
                   final CompiledEvent value)
    throws DuplicateIdentifierException
  {
    final SimpleExpressionProxy index = iter.next();
    if (iter.hasNext()) {
      final CompiledArrayAlias alias = new CompiledArrayAlias(this, index);
      alias.set(iter, value);
      set(index, alias);
    } else {
      set(index, value);
    }      
  }


  //#########################################################################
  //# Local Class ParentInfo
  private abstract class ParentInfo
  {

    //#######################################################################
    //# Naming
    String getName()
    {
      final IdentifierProxy ident = getIdentifier();
      return ident.toString();
    }

    IdentifierProxy getIdentifier()
    {
      final List<SimpleExpressionProxy> empty =
        new LinkedList<SimpleExpressionProxy>();
      return getIdentifier(empty);
    }

    abstract IdentifierProxy getIdentifier
      (final List<SimpleExpressionProxy> indexes);

  }


  //#########################################################################
  //# Local Class RootParentInfo
  private class RootParentInfo extends ParentInfo {

    //#######################################################################
    //# Constructor
    private RootParentInfo(final String name)
    {
      mName = name;
    }

    //#######################################################################
    //# Naming
    IdentifierProxy getIdentifier()
    {
      return new SimpleIdentifierElement(mName);
    }

    IdentifierProxy getIdentifier(final List<SimpleExpressionProxy> indexes)
    {
      if (indexes.isEmpty()) {
        return new SimpleIdentifierElement(mName);
      } else {
        return new IndexedIdentifierElement(mName, indexes);
      }
    }

    //#######################################################################
    //# Data Members
    private final String mName;
  }


  //#########################################################################
  //# Local Class IndexedParentInfo
  private class IndexedParentInfo extends ParentInfo {

    //#######################################################################
    //# Constructor
    private IndexedParentInfo(final CompiledArrayAlias parent,
                              final SimpleExpressionProxy index)
    {
      mParent = parent;
      mIndex = index;
    }

    //#######################################################################
    //# Naming
    IdentifierProxy getIdentifier(final List<SimpleExpressionProxy> indexes)
    {
      final ParentInfo info = mParent.getParentInfo();
      indexes.add(0, mIndex);
      return info.getIdentifier(indexes);
    }

    //#######################################################################
    //# Data Members
    private final CompiledArrayAlias mParent;
    private final SimpleExpressionProxy mIndex;

  }


  //#########################################################################
  //# Data Members
  private final ParentInfo mParentInfo;
  private final Map<SimpleExpressionProxy,CompiledEvent> mMap;

}
