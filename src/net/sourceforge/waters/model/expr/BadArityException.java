//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.model.expr
//# CLASS:   BadArityException
//###########################################################################
//# $Id: BadArityException.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;


public class BadArityException extends EvalException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public BadArityException()
  {
  }

  /**
   * Constructs a new exception indicating that a non-array identifier was
   * used together with array indexes.
   * @param  name        The name of the offending identifier.
   */
  public BadArityException(final String name)
  {
    super("Name '" + name + "' is not to be used as array!");
  }

  /**
   * Constructs a new exception indicating that an identifier was used with
   * the wrong number of array indexes. 
   * @param  name        The name of the offending identifier.
   * @param  numindexes  The number of indexes that was specified.
   * @param  expected    The expected number of indexes.
   */
  public BadArityException(final String name,
			   final int numindexes,
			   final int expected)
  {
    super("Bad number of array indexes to '" + name +
	  "' - expected " + expected + ", got " + numindexes + "!");
  }

}