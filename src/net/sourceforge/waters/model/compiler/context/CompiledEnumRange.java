//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   CompiledEnumRange
//###########################################################################
//# $Id: CompiledEnumRange.java,v 1.1 2008-06-16 07:09:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


class CompiledEnumRange implements CompiledRange
{

  //#########################################################################
  //# Constructors
  CompiledEnumRange(final List<? extends SimpleIdentifierProxy> atoms)
  {
    mAtoms = Collections.unmodifiableList(atoms);
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    final StringBuffer result = new StringBuffer("{");
    final Iterator<SimpleIdentifierProxy> iter = mAtoms.iterator();
    while (iter.hasNext()) {
      final SimpleIdentifierProxy atom = iter.next();
      result.append(atom.getName());
      if (iter.hasNext()) {
        result.append(", ");
      } 
    }
    result.append('}');
    return result.toString();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.expr.RangeValue
  public int size()
  {
    return mAtoms.size();
  }

  public int indexOf(final SimpleExpressionProxy value)
  {
    if (value instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy atom = (SimpleIdentifierProxy) value;
      return indexOf(atom);
    } else {
      return -1;
    }
  }

  public boolean contains(final SimpleExpressionProxy value)
  {
    if (value instanceof SimpleIdentifierProxy) {
      final SimpleIdentifierProxy atom = (SimpleIdentifierProxy) value;
      return contains(atom);
    } else {
      return false;
    }
  }

  public List<SimpleIdentifierProxy> getValues()
  {
    return mAtoms;
  }


  //#########################################################################
  //# More Specific Access
  boolean contains(final SimpleIdentifierProxy value)
  {
    return indexOf(value) >= 0;
  }

  int indexOf(final SimpleIdentifierProxy value)
  {
    int i = 0;
    for (final SimpleIdentifierProxy atom : mAtoms) {
      if (atom.equalsByContents(value)) {
        return i;
      }
      i++;
    }
    return -1;
  }


  //#########################################################################
  //# Data Members
  private final List<SimpleIdentifierProxy> mAtoms;

}
