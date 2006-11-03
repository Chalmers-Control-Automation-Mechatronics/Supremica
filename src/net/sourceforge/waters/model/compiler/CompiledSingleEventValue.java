//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledSingleEventValue
//###########################################################################
//# $Id: CompiledSingleEventValue.java,v 1.3 2006-11-03 15:01:57 torda Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.expr.IndexValue;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.xsd.base.EventKind;


class CompiledSingleEventValue implements EventValue
{

  //#######################################################################
  //# Constructor
  CompiledSingleEventValue(final CompiledEventDecl decl,
                           final List<? extends IndexValue> indexes)
  {
    mDecl = decl;
    mIndexes = Collections.unmodifiableList(indexes);
    mEvent = null;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    return mDecl.getIndexedName(mIndexes);
  }

  public int hashCode()
  {
    return 5 * mDecl.getName().hashCode() + mIndexes.hashCode();
  }

  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final CompiledSingleEventValue value =
        (CompiledSingleEventValue) partner;
      return mDecl == value.mDecl && mIndexes.equals(value.mIndexes);
    } else {
      return false;
    }    
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.EventValue
  public int getKindMask()
  {
    final EventKind kind = getKind();
    return EventKindMask.getMask(kind);
  }

  public boolean isObservable()
  {
    return mDecl.isObservable();
  }

  public Iterator<CompiledSingleEventValue> getEventIterator()
  {
    return Collections.singletonList(this).iterator();
  }

  public List<RangeValue> getIndexRanges()
  {
    return Collections.emptyList();
  }


  //#######################################################################
  //# Specific Access
  EventKind getKind()
  {
    return mDecl.getKind();
  }

  String getName()
  {
    return toString();
  }

  EventProxy getEventProxy()
  {
    return mEvent;
  }

  void setEventProxy(final EventProxy event)
  {
    mEvent = event;
  }


  //#######################################################################
  //# Data Members
  private final CompiledEventDecl mDecl;
  private final List<IndexValue> mIndexes;
  private EventProxy mEvent;

}
