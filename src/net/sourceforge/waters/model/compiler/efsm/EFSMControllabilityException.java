//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.model.compiler.efsm;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.efa.EFAVariable;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * An exception to report controllability issues when compiling EFSMs.
 *
 * @author Roger Su
 */

public class EFSMControllabilityException extends EvalException
{

  //#########################################################################
  //# Constructors
  /**
   * <P>Constructs an exception with the error message in the form of the
   * examples below.</P>
   *
   * <P><STRONG>Examples:</STRONG></P>
   * <UL>
   * <LI>"Specification '____' attempts to modify the variable '____' on the
   * uncontrollable event '____."</LI>
   * <LI>"Supervisor '____' attempts to modify the variable '____' on the
   * uncontrollable event '____'."</LI>
   * <LI>"Property '____' attempts to modify the variable '____' on the
   * event '____'."</LI></UL>
   *
   * @param component The component of interest
   * @param variable  The variable that is changed
   * @param event     The event that attempts to change the variable
   * @param location  The location of this exception
   */
  public EFSMControllabilityException(final SimpleComponentProxy component,
                                      final EFAVariable variable,
                                      final IdentifierProxy event,
                                      final Proxy location)
  {
    super(component.getKind().toString() + " '" + component.getName() +
          "' attempts to modify the variable '" +
          variable.getVariableName().toString() + "' on the " +
          isUncontrollable(component.getKind()) + "event '" +
          event.toString() + "'!",
          location);
  }


  //#########################################################################
  //# Auxiliary Method
  private static String isUncontrollable(final ComponentKind kind)
  {
    if (kind == ComponentKind.PROPERTY) {
      return "";
    } else {
      return "uncontrollable ";
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 5983937125200118084L;

}
