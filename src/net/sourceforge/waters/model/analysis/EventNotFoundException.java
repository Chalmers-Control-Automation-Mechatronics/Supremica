//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   EventNotFoundException
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
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
   * @param container    The product DES or automaton that causes the problem.
   * @param name         The name of the missing event.
   */
  public EventNotFoundException(final NamedProxy container,
                                final String name)
  {
    this(container, name, null, false);
  }

  /**
   * Constructs a new exception indicating that the given product DES does not
   * contain the given event.
   * @param container    The product DES or automaton that causes the problem.
   * @param name         The name of the missing event.
   * @param kind         The expected event kind.
   * @param includeKind  A flag, indicating whether the exception message
   *                     should include detailed event kind information.
   *                     If false, it will only distinguish between events
   *                     and propositions.
   */
  public EventNotFoundException(final NamedProxy container,
                                final String name,
                                final EventKind kind,
                                final boolean includeKind)
  {
    mContainer = container;
    mEventName = name;
    mEventKind = kind;
    mIncludeKind = includeKind;
  }


  //#########################################################################
  //# Overrides for java.lang.Throwable
  public String getMessage()
  {
    final StringBuffer buffer = new StringBuffer();
    buffer.append("The ");
    buffer.append(getContainerClassName());
    buffer.append(" '");
    buffer.append(mContainer.getName());
    buffer.append("' does not contain any ");
    buffer.append(getEventKindName());
    buffer.append(" named '");
    buffer.append(mEventName);
    buffer.append("'!");
    return buffer.toString();
  }


  //#########################################################################
  //# Auxiliary Methods
  private String getContainerClassName()
  {
    if (mContainer instanceof ProductDESProxy) {
      return "model";
    } else if (mContainer instanceof AutomatonProxy) {
      return "automaton";
    } else {
      return ProxyTools.getShortClassName(mContainer);
    }
  }

  private String getEventKindName()
  {
    switch (mEventKind) {
    case CONTROLLABLE:
    case UNCONTROLLABLE:
      if (mIncludeKind) {
        return mEventKind.toString().toLowerCase() + " event";
      } else {
        return "event";
      }
    case PROPOSITION:
      return "proposition";
    default:
      return mEventKind.toString();
    }
  }


  //#########################################################################
  //# Data Members
  private final NamedProxy mContainer;
  private final String mEventName;
  private final EventKind mEventKind;
  private final boolean mIncludeKind;


  //#########################################################################
  //# Static Class Variables
  public static final long serialVersionUID = 1;

}
