//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   DuplicateIdentifierException
//###########################################################################
//# $Id: DuplicateIdentifierException.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;


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