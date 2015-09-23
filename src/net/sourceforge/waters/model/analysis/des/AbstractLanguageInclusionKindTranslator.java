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

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A kind translator used for language inclusion checking.
 * This translator remaps all events to be uncontrollable,
 * all specs and supervisors are considered as plants, and all properties are
 * considered as specs. Such a remapping makes it possible to
 * implement language inclusion checking using a controllability
 * checker.</P>
 * <P>This is a non-serialisable, abstract default implementation of a
 * language inclusion kind translator, which is subclassed for more specific
 * implementations.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractLanguageInclusionKindTranslator
  implements KindTranslator
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.KindTranslator
  /**
   * Returns the component kind of the given automaton in a language
   * inclusion check.
   * @return {@link ComponentKind#PLANT}, if the given automaton is
   *         a plant, spec, or supervisor, or {@link ComponentKind#SPEC} if
   *         the given automaton is a property.
   */
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    final ComponentKind kind = aut.getKind();
    switch (kind) {
    case PLANT:
    case SPEC:
    case SUPERVISOR:
      return ComponentKind.PLANT;
    case PROPERTY:
      return ComponentKind.SPEC;
    default:
      return kind;
    }
  }

  /**
   * Returns the event kind of the given event in a language
   * inclusion check.
   * @return {@link EventKind#UNCONTROLLABLE}.
   */
  public EventKind getEventKind(final EventProxy event)
  {
    final EventKind kind = event.getKind();
    switch (kind) {
    case CONTROLLABLE:
    case UNCONTROLLABLE:
      return EventKind.UNCONTROLLABLE;
    default:
      return kind;
    }
  }

}
