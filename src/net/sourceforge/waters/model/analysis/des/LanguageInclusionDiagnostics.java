//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.io.Serializable;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * <P>A diagnostics generator used for language inclusion checking.</P>
 *
 * @author Robi Malik
 */

public class LanguageInclusionDiagnostics
  implements SafetyDiagnostics, Serializable
{

  //#########################################################################
  //# Singleton Pattern
  public static LanguageInclusionDiagnostics getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final LanguageInclusionDiagnostics theInstance =
      new LanguageInclusionDiagnostics();
  }

  private LanguageInclusionDiagnostics()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.SafetyDiagnostics
  public String getTraceName(final ProductDESProxy des)
  {
    final String desname = des.getName();
    return desname + "-unsafe";
  }

  public String getTraceComment(final ProductDESProxy des,
                                final EventProxy event,
                                final AutomatonProxy aut,
                                final StateProxy state)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("The model '");
    buffer.append(des.getName());
    buffer.append("' does not satisfy the language inclusion property '");
    buffer.append(aut.getName());
    buffer.append('\'');
    if (event == null) {
      buffer.append(", because this property has no initial state ");
      buffer.append("and therefore represents the empty language.");
    } else {
      buffer.append(": event ");
      buffer.append(event.getName());
      buffer.append(" may occur in state ");
      buffer.append(state.getName());
      buffer.append(", but the property disallows it.");
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
