//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   ArrayEventValue
//###########################################################################
//# $Id: ArrayEventValue.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.expr.ArrayValue;
import net.sourceforge.waters.model.expr.IndexOutOfRangeException;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.Value;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


class ArrayEventValue implements EventValue, ArrayValue
{

  //#######################################################################
  //# Constructor
  ArrayEventValue(final CompiledEventDecl decl, final List indexes)
  {
    mDecl = decl;
    mIndexes = indexes;
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

  public ColorGeometryProxy getColorGeometry()
  {
    return mDecl.getColorGeometry();
  }

  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final ArrayEventValue value = (ArrayEventValue) partner;
      return mDecl == value.mDecl && mIndexes.equals(value.mIndexes);
    } else {
      return false;
    }    
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.compiler.EventValue
  public Iterator getEventIterator()
  {
    return new ArrayEventIterator(this);
  }

  public EventKind getKind()
  {
    return mDecl.getKind();
  }

  public void checkParameterType(final EventDeclProxy template)
    throws EventKindException
  {
    final List ranges = mDecl.getRanges();
    final List tranges = template.getRanges();
    if (getKind() != template.getKind()) {
      throw new EventKindException
	("Can't assign event '" + toString() + "' to parameter '" +
	 template.getName() + ": expected " + template.getKind() + ", got " +
	 getKind() + "!");
    } else if (ranges.size() != tranges.size()) {
      throw new EventKindException
	("Can't assign event '" + toString() + "' to parameter '" +
	 template.getName() + ": expected " + tranges.size() +
	 " array indexes, got " + ranges.size() + "!");
    }
    final Iterator iter = ranges.iterator();
    final Iterator titer = tranges.iterator();
    int pos = mIndexes.size();
    while (iter.hasNext()) {
      final RangeValue range = (RangeValue) iter.next();
      final RangeValue trange = (RangeValue) titer.next();
      if (!range.equals(trange)) {
	throw new EventKindException
	  ("Can't assign event '" + toString() + "' to parameter '" +
	   template.getName() + ": array type mismatch at index position " +
	   pos + " - expected " + trange + ", got " + range + "!");
      }
      pos++;
    }
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.expr.ArrayValue
  public Value find(final Value index, final SimpleExpressionProxy indexexpr)
    throws IndexOutOfRangeException
  {
    final int pos = mIndexes.size();
    final RangeValue range = mDecl.getRange(pos);
    if (!range.contains(index)) {
      throw new IndexOutOfRangeException(indexexpr, index, range);
    }
    final List newindexes = new ArrayList(pos + 1);
    newindexes.addAll(mIndexes);
    newindexes.add(index);
    return mDecl.getValue(newindexes);
  }


  //#########################################################################
  //# Local Class ArrayEventIterator
  private static class ArrayEventIterator implements Iterator
  {

    //#######################################################################
    //# Constructor
    ArrayEventIterator(final ArrayEventValue value)
    {
      final List indexes = value.mIndexes;
      final int pos = indexes.size();
      mDecl = value.mDecl;
      mMoreIndexes = new ArrayList(pos + 1);
      mMoreIndexes.addAll(indexes);
      mRangeIterator = mDecl.getRange(pos).iterator();
      final Object first = mRangeIterator.next();
      mMoreIndexes.add(first);
      final EventValue event = mDecl.getValue(mMoreIndexes);
      mInnerIterator = event.getEventIterator();
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mDecl != null;
    }

    public Object next()
    {
      if (mDecl != null) {
	final Object result = mInnerIterator.next();
	if (!mInnerIterator.hasNext()) {
	  if (!mRangeIterator.hasNext()) {
	    mDecl = null;
	    mMoreIndexes = null;
	    mRangeIterator = null;
	    mInnerIterator = null;
	  } else {
	    final int pos = mMoreIndexes.size() - 1;
	    final Object next = mRangeIterator.next();
	    mMoreIndexes.set(pos, next);
	    final EventValue event = mDecl.getValue(mMoreIndexes);
	    mInnerIterator = event.getEventIterator();
	  }
	}
	return result;
      } else {
	throw new NoSuchElementException
	  ("Out of elements in ArrayEventIterator!");
      }
    }

    public void remove()
    {
      throw new UnsupportedOperationException
	("ArrayEventIterator does not support remove() operation!");
    } 

    //#######################################################################
    //# Data Members
    private CompiledEventDecl mDecl;
    private List mMoreIndexes;
    private Iterator mRangeIterator;
    private Iterator mInnerIterator;

  }


  //#######################################################################
  //# Data Members
  private final CompiledEventDecl mDecl;
  private final List mIndexes;

}
