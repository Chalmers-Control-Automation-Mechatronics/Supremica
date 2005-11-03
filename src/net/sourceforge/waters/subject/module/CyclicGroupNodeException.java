//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   CyclicGroupNodeException
//###########################################################################
//# $Id: CyclicGroupNodeException.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.module;

import net.sourceforge.waters.model.base.ModelException;


/**
 * Thrown when a cyclic structure is detected while adding or changing a
 * node group in a graph.
 *
 * @author Robi Malik
 */

public class CyclicGroupNodeException extends ModelException {

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception with <CODE>null</CODE> as its detail message.
   */
  public CyclicGroupNodeException()
  {
  }

  /**
   * Constructs a new exception with a specified detail message.
   * @param  op     A string describing the operation causing this
   *                exception to be thrown, e.g., <CODE>"Adding node 'idle'
   *                to component 'mach1'"</CODE>.
   */
  public CyclicGroupNodeException(final String op)
  {
    mOperation = op;
  }


  //#########################################################################
  //# Message
  /**
   * Gets the detail message string of this exception.
   */
  public String getMessage()
  {
    if (mOperation == null) {
      return null;
    } else {
      return mOperation + " causes cyclic node group structure!";
    }
  }

  /**
   * Modifies the detail message of this exception.
   * @param  op     A string describing the operation causing this
   *                exception to be thrown, e.g., <CODE>"Adding node 'idle'
   *                to component 'mach1'"</CODE>.
   */
  public void putOperation(final String op)
  {
    mOperation = op;
  }


  //#########################################################################
  //# Data Members
  private String mOperation;

}
