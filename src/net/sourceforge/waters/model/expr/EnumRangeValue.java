//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   EnumRangeValue
//###########################################################################
//# $Id: EnumRangeValue.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class EnumRangeValue implements RangeValue
{

  //#########################################################################
  //# Constructors
  public EnumRangeValue(final List atoms)
  {
    mAtoms = Collections.unmodifiableList(atoms);
  }


  //#########################################################################
  //# Getters
  public List getAtoms()
  {
    return mAtoms;
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    final StringBuffer result = new StringBuffer("{");
    final Iterator iter = mAtoms.iterator();
    while (iter.hasNext()) {
      final AtomValue atom = (AtomValue) iter.next();
      result.append(atom.getName());
      if (iter.hasNext()) {
	result.append(", ");
      }	
    }
    result.append('}');
    return result.toString();
  }

  public int hashCode()
  {
    return mAtoms.hashCode();
  }

  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final EnumRangeValue range = (EnumRangeValue) partner;
      return mAtoms.equals(range.mAtoms);
    } else {
      return false;
    }    
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.RangeValue
  public int size()
  {
    return mAtoms.size();
  }

  public int indexOf(final Value value)
  {
    if (value instanceof AtomValue) {
      return mAtoms.indexOf(value);
    } else {
      return -1;
    }
  }

  public boolean contains(Value value)
  {
    if (value instanceof AtomValue) {
      return mAtoms.contains(value);
    } else {
      return false;
    }
  }

  public Iterator iterator()
  {
    return mAtoms.iterator();
  }


  //#########################################################################
  //# Data Members
  private final List mAtoms;

}