//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   DuplicateIdentifierException
//###########################################################################
//# $Id: DuplicateIdentifierException.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;

import net.sourceforge.waters.model.base.Proxy;


public class DuplicateIdentifierException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public DuplicateIdentifierException()
  {
  }

  /**
   * Constructs a new exception indicating that the given name is already
   * defined.
   */
  public DuplicateIdentifierException(final String name)
  {
    this(name, "Name");
  }

  /**
   * Constructs a new exception indicating that the given name is already
   * defined, with the specified originating expression.
   */
  public DuplicateIdentifierException(final String name,
				      final Proxy location)
  {
    this(name, "Name", location);
  }

  /**
   * Constructs a new exception indicating that the given name is already
   * defined.
   */
  public DuplicateIdentifierException(final String name,
				      final String typename)
  {
    this(name, typename, null);
  }

  /**
   * Constructs a new exception indicating that the given name is already
   * defined, with the specified originating expression.
   */
  public DuplicateIdentifierException(final String name,
				      final String typename,
				      final Proxy location)
  {
    super(typename + " '" + name + "' is already in use!", location);
  }

}