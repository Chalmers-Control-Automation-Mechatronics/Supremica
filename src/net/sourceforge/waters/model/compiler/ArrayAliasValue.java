//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.compiler
//# CLASS:   ArrayAliasValue
//###########################################################################
//# $Id: ArrayAliasValue.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.expr.ArrayValue;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.Value;



class ArrayAliasValue implements ArrayValue
{

  //#######################################################################
  //# Constructor
  ArrayAliasValue(final String name)
  {
    mName = name;
    mMap = new HashMap();
  }

  ArrayAliasValue(final ArrayAliasValue parent, final Value index)
  {
    mName = parent.mName + "[" + index + "]";
    mMap = new HashMap();
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    return mName;
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.expr.ArrayValue
  public Value find(final Value index, final SimpleExpressionProxy indexexpr)
    throws UndefinedIdentifierException
  {
    final Value result = get(index);
    if (result == null) {
      throw new UndefinedIdentifierException(mName);
    }
    return result;
  }


  //#######################################################################
  //# Alias Creation
  Value get(final Value index)
  {
    return (Value) mMap.get(index);
  }

  void set(final Value index, final Value value)
  {
    mMap.put(index, value);
  }


  //#######################################################################
  //# Data Members
  private final String mName;
  private final Map mMap;

}
