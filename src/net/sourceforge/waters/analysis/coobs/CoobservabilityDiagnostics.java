//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.coobs;

import java.io.Serializable;
import java.util.List;

import net.sourceforge.waters.model.analysis.des.SafetyDiagnostics;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * <P>A diagnostics generator used for coobservability checking.</P>
 *
 * @author Robi Malik
 */

public class CoobservabilityDiagnostics
  implements SafetyDiagnostics, Serializable
{

  //#########################################################################
  //# Constructor
  public CoobservabilityDiagnostics
    (final List<CoobservabilitySignature.Site> controllers)
  {
    mControllers = controllers;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyDiagnostics
  @Override
  public String getTraceName(final ProductDESProxy des)
  {
    return getDefaultTraceName(des);
  }

  @Override
  public String getTraceComment(final ProductDESProxy des,
                                final EventProxy event,
                                final AutomatonProxy aut,
                                final StateProxy state)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("The model '");
    buffer.append(des.getName());
    buffer.append("' is not coobservable: specification ");
    buffer.append(aut.getName());
    buffer.append(" disables the event ");
    buffer.append(event.getName());
    buffer.append(" in state ");
    buffer.append(state.getName());
    buffer.append(", which is possible according to the plant model while ");
    final int numControllers = mControllers.size();
    if (numControllers == 0) {
      buffer.append("there is no supervisor that can disable this event.");
    } else {
      CoobservabilitySignature.Site site = null;
      if (numControllers == 1) {
        final CoobservabilitySignature.Site first = mControllers.get(0);
        for (final CoobservabilitySignature.Site member :
             first.getSingletonMembers()) {
          if (member.isControlledEvent(event)) {
            if (site == null) {
              site = member;
            } else {
              site = null;
              break;
            }
          }
        }
      }
      if (site != null) {
        buffer.append("the only supervisor that could disable this event, ");
        buffer.append(site.getName());
        buffer.append(", cannot do so unambiguously.");
      } else {
        buffer.append("none of the supervisors that could disable this event " +
                      "can do so unambiguously.");
      }
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Static Access
  public static String getDefaultTraceName(final ProductDESProxy des)
  {
    final String desname = des.getName();
    return desname + "-noncoobs";
  }


  //#########################################################################
  //# Data Members
  private final List<CoobservabilitySignature.Site> mControllers;


  //#########################################################################
  //# Class Constants
  public static String REFERENCE_SITE_NAME = "(reference)";

  private static final long serialVersionUID = -92062777659933733L;

}
