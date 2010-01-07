//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   NonExistentMarkingException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An exception indicating that a product DES does not contain a given event.
 * This exception typically is thrown by a conflict checker
 * ({@link ConflictChecker}) to indicate that a specified marking proposition
 * was not found in the model.
 *
 * @author Robi Malik
 */

public class EventNotFoundException extends AnalysisException
{

  //#########################################################################
  //# Constructors
  /**
   * Constructs a new exception indicating that the given product DES does not
   * contain the given event.
   * @param des     The product DES that causes the problem.
   * @param name    The name of the missing event.
   */
  public EventNotFoundException(final ProductDESProxy des,
                                final String name)
  {
    this(des, name, null);
  }

  /**
   * Constructs a new exception indicating that the given product DES does not
   * contain the given event.
   * @param des     The product DES that causes the problem.
   * @param name    The name of the missing event.
   * @param kind    The expected event kind. This is used to produce a more
   *                specific message for propositions.
   */
  public EventNotFoundException(final ProductDESProxy des,
                                final String name,
                                final EventKind kind)
  {
    mProductDES = des;
    mEventName = name;
    mEventKind = kind;
  }


  //#########################################################################
  //# Overrides for java.lang.Throwable
  public String getMessage()
  {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("The model '");
    buffer.append(mProductDES.getName());
    buffer.append("' does not contain any ");
    if (mEventKind == EventKind.PROPOSITION) {
      buffer.append("proposition");
    } else {
      buffer.append("event");
    }
    buffer.append(" named '");
    buffer.append(mEventName);
    buffer.append("'!");
    return buffer.toString();
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxy mProductDES;
  private final String mEventName;
  private final EventKind mEventKind;


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
