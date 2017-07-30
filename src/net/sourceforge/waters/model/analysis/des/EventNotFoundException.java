//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.model.analysis.AnalysisException;
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
    final StringBuilder buffer = new StringBuilder();
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
    if (mEventKind == null) {
      return "event";
    } else {
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
