//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   IndexOutOfRangeException
//###########################################################################
//# $Id: IndexOutOfRangeException.java,v 1.1 2008-06-16 07:09:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.CompiledRange;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.expr.EvalException;


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
   * Constructs a new exception indicating that the given object is not
   * an array.
   * @param  ident    The object that was incorrectly indexed.
   */
  public IndexOutOfRangeException(final Object ident)
  {
    mValue = ident;
    mRange = null;
  }

  /**
   * Constructs a new exception indicating that the given expression is not
   * well-typed.
   * @param  value    The value to which that subterm has been evaluated.
   * @param  range    The type that was expected.
   */
  public IndexOutOfRangeException(final SimpleExpressionProxy value,
                                  final CompiledRange range)
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
                                  final SimpleExpressionProxy value,
                                  final CompiledRange range)
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
    if (mValue == null) {
      return null;
    } else if (mRange == null) {
      return "Identifier " + mValue + " cannot be indexed as array!";
    } else {
      return "Value " + mValue + " is not in range " + mRange + "!";
    }
  }


  //#########################################################################
  //# Data Members
  private final Object mValue;
  private final CompiledRange mRange;
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;
 
}
