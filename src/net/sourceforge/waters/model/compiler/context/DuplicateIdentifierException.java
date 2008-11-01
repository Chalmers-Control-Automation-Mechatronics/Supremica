//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   DuplicateIdentifierException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.IdentifierProxy;


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
   * Constructs a new exception indicating that the given identifier is already
   * defined.
   */
  public DuplicateIdentifierException(final IdentifierProxy ident)
  {
    this(ident, "Name");
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
   * Constructs a new exception indicating that the given identifier is already
   * defined.
   */
  public DuplicateIdentifierException(final IdentifierProxy ident,
				      final String typename)
  {
    this(ident.toString(), typename, ident);
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

  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}