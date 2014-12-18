//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.expr
//# CLASS:   MultiEvalException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.expr;

import java.util.ArrayList;
import java.util.List;


public class MultiEvalException extends EvalException
{

  //#########################################################################
  //# Constructors
  public MultiEvalException()
  {
    mExceptions = new ArrayList<>();
  }


  //#########################################################################
  //# Overrides for Throwable
  @Override
  public String getMessage()
  {
    final int count = mExceptions.size();
    return count + (count == 1 ? " error" : " errors");
  }


  //#########################################################################
  //# Overrides for EvalException
  @Override
  public EvalException[] getAll()
  {
    return mExceptions.toArray(new EvalException[mExceptions.size()]);
  }


  //#########################################################################
  //# Access
  /**
   * Adds an exception to the exceptions represented by <CODE>this</CODE>.
   * <p>
   * The exception is permitted to be <CODE>this</CODE>, in which case the
   * method has no effect.
   *
   * @param exception The exception to be added.
   */
  public void add(final EvalException exception)
  {
    if (exception != this) {
      mExceptions.add(exception);
    }
  }

  /**
   * Tests whether there is any exception accumulated in
   * the list {@link #mExceptions}.
   *
   * @return <code>true</code> if there is any accumulated exception, or
   *        <code>false</code> otherwise.
   */
  public boolean hasException()
  {
    return !mExceptions.isEmpty();
  }


  //#########################################################################
  //# Data Members
  private final List<EvalException> mExceptions;


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
