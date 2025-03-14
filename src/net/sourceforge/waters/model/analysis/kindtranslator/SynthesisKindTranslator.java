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

package net.sourceforge.waters.model.analysis.kindtranslator;

import java.io.Serializable;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>A kind translator for synthesis.</P>
 *
 * <P>There are two versions of synthesis kind translation, depending on
 * whether controllability is considered or not. If controllability is not
 * considered, all uncontrollable events are redefined to be controllable,
 * otherwise the event types are unchanged. In both cases, the synthesis kind
 * translator suppresses supervisor and property components by mapping their
 * component kind to <CODE>null</CODE>.</P>
 *
 * @author Robi Malik
 */

public final class SynthesisKindTranslator
  implements KindTranslator, Serializable
{

  //#########################################################################
  //# Singleton Implementation
  /**
   * Gets a kind translator for synthesis subject to controllability.
   * @return A kind translator that leaves event kinds unchanged but
   *         suppresses supervisors and properties.
   */
  public static SynthesisKindTranslator getInstanceWithControllability()
  {
    return SingletonHolderWithControllability.theInstance;
  }

  /**
   * Gets a kind translator for synthesis subject ignoring controllability.
   * @return A kind translator that redefines all uncontrollable events to be
   *         controllable, and suppresses supervisors and properties.
   */
  public static SynthesisKindTranslator getInstanceWithoutControllability()
  {
    return SingletonHolderWithoutControllability.theInstance;
  }

  private static class SingletonHolderWithControllability {
    private static final SynthesisKindTranslator theInstance =
      new SynthesisKindTranslator(EventKind.UNCONTROLLABLE);
  }

  private static class SingletonHolderWithoutControllability {
    private static final SynthesisKindTranslator theInstance =
      new SynthesisKindTranslator(EventKind.CONTROLLABLE);
  }

  private SynthesisKindTranslator(final EventKind kind)
  {
    mUncontrollableEventKind = kind;
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator
  @Override
  public EventKind getEventKind(final EventProxy event)
  {
    final EventKind kind = event.getKind();
    switch (kind) {
    case UNCONTROLLABLE:
      return mUncontrollableEventKind;
    default:
      return kind;
    }
  }

  @Override
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    final ComponentKind kind = aut.getKind();
    switch (kind) {
    case PROPERTY:
    case SUPERVISOR:
      return null;
    default:
      return kind;
    }
  }


  //#########################################################################
  //# Data Members
  private final EventKind mUncontrollableEventKind;


  //#########################################################################
  //# Singleton Implementation
  private static final long serialVersionUID = 4982215984233224218L;

}
