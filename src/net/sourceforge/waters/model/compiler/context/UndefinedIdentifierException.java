//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   UndefinedIdentifierException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.context;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


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
   * Constructs a new exception indicating that the given identifier
   * is not defined.
   */
  public UndefinedIdentifierException(final SimpleExpressionProxy ident)
  {
    this(ident.toString(), ident);
  }

  /**
   * Constructs a new exception indicating that the given identifier
   * is not defined.
   */
  public UndefinedIdentifierException(final SimpleExpressionProxy ident,
                                      final String typename)
  {
    this(ident.toString(), typename, ident);
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
  
  
  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}