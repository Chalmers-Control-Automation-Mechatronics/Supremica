//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   IndexOutOfRangeException
//###########################################################################
//# $Id: IndexOutOfRangeException.java,v 1.5 2007-06-08 10:57:34 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.RangeValue;
import net.sourceforge.waters.model.expr.Value;


public class IndexOutOfRangeException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public IndexOutOfRangeException()
  {
    mValue = null;
    mRange = null;
  }

  /**
   * Constructs a new exception indicating that the given constant alias
   * cannot be used as an array.
   * @param  alias    The alias which has wrongly been tried to use as array.
   */
  public IndexOutOfRangeException(final ConstantAliasProxy alias)
  {
    super(alias);
    mValue = null;
    mRange = null;
  }

  /**
   * Constructs a new exception indicating that the given value cannot
   * accept more array indexes.
   * @param  value    The value which has wrongly been tried to use as array.
   */
  public IndexOutOfRangeException(final Value value)
  {
    mValue = value;
    mRange = null;
  }

  /**
   * Constructs a new exception indicating that the given expression is not
   * well-typed.
   * @param  value    The value to which that subterm has been evaluated.
   * @param  range    The type that was expected.
   */
  public IndexOutOfRangeException(final Value value, final RangeValue range)
  {
    mValue = value;
    mRange = range;
  }

  /**
   * Constructs a new exception indicating that the given expression is not
   * well-typed.
   * @param  expr     The subterm that is not well-typed.
   * @param  value    The value to which that subterm has been evaluated.
   * @param  range    The type that was expected.
   */
  public IndexOutOfRangeException(final SimpleExpressionProxy expr,
                                  final Value value,
                                  final RangeValue range)
  {
    super(expr);
    mValue = value;
    mRange = range;
  }


  //#########################################################################
  //# Message
  public String getMessage()
  {
    final Proxy location = getLocation();
    if (location instanceof ConstantAliasProxy) {
      final ConstantAliasProxy alias = (ConstantAliasProxy) location;
      final IdentifierProxy ident = alias.getIdentifier();
      final String name = ident.getName();
      return "Constant alias '" + name + "' cannot be declared as array!";
    } else if (mValue == null) {
      return null;
    } else if (mRange == null) {
      if (location == null) {
        return "Value " + mValue + " cannot be used as array!";
      } else if (location.toString().equals(mValue.toString())) {
        return "Constant " + location + " cannot be used as array!";
      } else {
        return
          "Expression '" + location + "' (evaluated to " + mValue +
          ") cannot be used as array!";
      }
    } else {
      if (location == null) {
        return "Value " + mValue + " is not in range " + mRange + "!";
      } else if (location.toString().equals(mValue.toString())) {
        return "Constant " + location + " is not in range " + mRange + "!";
      } else {
        return
          "Expression '" + location + "' (evaluated to " + mValue +
          ") is not in range " + mRange + "!";
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final Value mValue;
  private final RangeValue mRange;
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;
 
}
