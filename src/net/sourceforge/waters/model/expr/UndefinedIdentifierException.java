//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   UndefinedIdentifierException
//###########################################################################
//# $Id: UndefinedIdentifierException.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.base.Proxy;


public class UndefinedIdentifierException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public UndefinedIdentifierException()
  {
  }

  /**
   * Constructs a new exception indicating that the given name is not defined.
   */
  public UndefinedIdentifierException(final String name)
  {
    this(name, null);
  }

  /**
   * Constructs a new exception indicating that the given name is not defined,
   * with the specified originating expression.
   */
  public UndefinedIdentifierException(final String name, final Proxy location)
  {
    this(name, "identifier", location);
  }

  /**
   * Constructs a new exception indicating that the given name is not defined,
   * with the specified originating expression.
   */
  public UndefinedIdentifierException(final String name,
				      final String typename,
				      final Proxy location)
  {
    super("Undeclared " + typename + " '" + name + "'!", location);
  }

}