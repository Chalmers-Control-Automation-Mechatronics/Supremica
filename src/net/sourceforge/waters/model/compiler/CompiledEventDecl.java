//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   CompiledEventDecl
//###########################################################################
//# $Id: CompiledEventDecl.java,v 1.3 2005-02-28 19:16:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A compiler-internal representation of an event declaration.</P>
 *
 * <P>This class supports the mapping from a module's event declarations
 * and event expressions to the simple events in a product DES.</P>
 *
 * @author Robi Malik
 */

class CompiledEventDecl
{

  //#########################################################################
  //# Constructors
  CompiledEventDecl(final String name,
		    final EventDeclProxy decl,
		    final List ranges,
		    final ModuleCompiler environment)
  {
    mName = name;
    mKind = decl.getKind();
    mIsObservable = decl.isObservable();
    mRanges = Collections.unmodifiableList(ranges);
    mGeometry = decl.getColorGeometry();
    mIndexValueMap = new HashMap();
    mEnvironment = environment;
  }


  //#########################################################################
  //# Simple Access
  String getName()
  {
    return mName;
  }

  EventKind getKind()
  {
    return mKind;
  }

  List getRanges()
  {
    return mRanges;
  }

  RangeValue getRange(int index)
  {
    return (RangeValue) mRanges.get(index);
  }

  ColorGeometryProxy getColorGeometry()
  {
    return mGeometry;
  }


  //#########################################################################
  //# Value Creation
  EventValue getValue()
  {
    final List empty = Collections.EMPTY_LIST;
    return getValue(empty);
  }

  EventValue getValue(final List indexes)
  {
    EventValue result = (EventValue) mIndexValueMap.get(indexes);
    if (result == null) {
      if (indexes.size() == mRanges.size()) {
	result = new SingleEventValue(this, indexes);
      } else {
	result = new ArrayEventValue(this, indexes);
      }
      mIndexValueMap.put(indexes, result);
    }
    return result;
  }


  //#########################################################################
  //# Event Creation
  EventProxy getEvent(final List indexes)
  {
    final String name = getIndexedName(indexes);
    final EventProxy template = new EventProxy(name, mKind, mIsObservable);
    return mEnvironment.getEvent(template);
  }


  //#########################################################################
  //# Auxiliary Methods
  String getIndexedName(final List indexes)
  {
    final StringBuffer buffer = new StringBuffer(mName);
    final Iterator iter = indexes.iterator();
    while (iter.hasNext()) {
      final Value index = (Value) iter.next();
      buffer.append('[');
      buffer.append(index);
      buffer.append(']');
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final EventKind mKind;
  private final boolean mIsObservable;
  private final List mRanges;
  private final ColorGeometryProxy mGeometry;
  private final Map mIndexValueMap;
  private final ModuleCompiler mEnvironment;

}
