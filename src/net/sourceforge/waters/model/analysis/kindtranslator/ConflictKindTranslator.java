//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.analysis.kindtranslator;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>A kind translator used for conflict checking.
 * This translator remaps all plants and specifications as plants,
 * and all controllable and uncontrollable events as the same kind.
 * There are two instances of this kind translator that return either
 * controllable or uncontrollable events.
 * The use of kind translators is optional in conflict checking,
 * but the unification of types may make things easier for some
 * implementations.</P>
 *
 * @author Robi Malik
 */

public class ConflictKindTranslator
  implements KindTranslator
{

  //#########################################################################
  //# Singleton Pattern
  /**
   * Returns a kind translator that remaps all plants and specifications as
   * plants, and all controllable and uncontrollable events as the same
   * controllable.
   */
  public static ConflictKindTranslator getInstanceControllable()
  {
    return SingletonHolderControllable.theInstance;
  }

  /**
   * Returns a kind translator that remaps all plants and specifications as
   * plants, and all controllable and uncontrollable events as the same
   * uncontrollable.
   */
  public static ConflictKindTranslator getInstanceUncontrollable()
  {
    return SingletonHolderUncontrollable.theInstance;
  }

  private static class SingletonHolderControllable {
    private static final ConflictKindTranslator theInstance =
      new ConflictKindTranslator(EventKind.CONTROLLABLE);
  }

  private static class SingletonHolderUncontrollable {
    private static final ConflictKindTranslator theInstance =
      new ConflictKindTranslator(EventKind.UNCONTROLLABLE);
  }

  /**
   * Creates a conflict kind translator.
   * This method is available to facilitate subclassing.
   * To obtain a standard conflict kind translator, the singleton methods
   * {@link #getInstanceControllable()} and {@link
   * #getInstanceUncontrollable()} should be used instead.
   * @param  kind   The event kind to be returned for all non-proposition
   *                events.
   */
  public ConflictKindTranslator(final EventKind kind)
  {
    mEventKind = kind;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator
  /**
   * Returns the component kind of the given automaton in a conflict
   * check.
   * @return {@link ComponentKind#PLANT}, if the given automaton is
   *         a plant, spec, or supervisor; <CODE>null</CODE> otherwise.
   */
  @Override
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    final ComponentKind kind = aut.getKind();
    switch (kind) {
    case PLANT:
    case SPEC:
    case SUPERVISOR:
      return ComponentKind.PLANT;
    default:
      return null;
    }
  }

  /**
   * Returns the event kind of the given event in a conflict check.
   * @return Either {@link EventKind#CONTROLLABLE} or
   *         {@link EventKind#UNCONTROLLABLE}, depending on which
   *         version of the conflict kind translator is used;
   *         or {@link EventKind#PROPOSITION}.
   */
  @Override
  public EventKind getEventKind(final EventProxy event)
  {
    final EventKind kind = event.getKind();
    switch (kind) {
    case CONTROLLABLE:
    case UNCONTROLLABLE:
      return mEventKind;
    default:
      return kind;
    }
  }


  //#########################################################################
  //# Data Members
  private final EventKind mEventKind;

}
