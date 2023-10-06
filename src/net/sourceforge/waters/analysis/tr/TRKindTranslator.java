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

package net.sourceforge.waters.analysis.tr;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>A kind translator extension to support the replacement of {@link
 * AutomatonProxy} objects to {@link TRAutomatonProxy} objects.</P>
 *
 * <P>Some kind translators compare object references to determine component
 * kind, and this may fail when a model analyser works with {@link
 * TRAutomatonProxy} objects instead of the original {@link AutomatonProxy}
 * objects. Such algorithms should replace their kind translator with an
 * instance of this class and record automata replacements by calling
 * {@link #add(AutomatonProxy, TRAutomatonProxy) add()}.</P>
 *
 * @author Robi Malik
 */

public class TRKindTranslator implements KindTranslator
{

  /**
   * Creates a new TRKindTranslator.
   * @param  parent  The parent kind translator that determines the component
   *                 kind of automata before replacement.
   */
  public TRKindTranslator(final KindTranslator parent)
  {
    mParent = parent;
    mExtensionMap = null;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Returns the parent kind translator that determines the component kind
   * of automata before replacement.
   */
  public KindTranslator getParent()
  {
    return mParent;
  }

  /**
   * Records a replacement of an {@link AutomatonProxy} object with a {@link
   * TRAutomatonProxy} object. Calling this method ensures that the {@link
   * TRAutomatonProxy} is given the same component kind as the {@link
   * AutomatonProxy}.
   * @param  aut     The {@link AutomatonProxy} being replaced.
   * @param  tr      The {@link TRAutomatonProxy} replacing the automaton.
   */
  public void add(final AutomatonProxy aut, final TRAutomatonProxy tr)
  {
    final ComponentKind kind = mParent.getComponentKind(aut);
    if (mParent.getComponentKind(tr) != kind) {
      if (mExtensionMap == null) {
        mExtensionMap = new HashMap<>();
      }
      mExtensionMap.put(tr, kind);
    }
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator
  @Override
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    if (mExtensionMap == null) {
      return mParent.getComponentKind(aut);
    }
    final ComponentKind kind = mExtensionMap.get(aut);
    if (kind == null) {
      return mParent.getComponentKind(aut);
    } else {
      return kind;
    }
  }

  @Override
  public EventKind getEventKind(final EventProxy event)
  {
    return mParent.getEventKind(event);
  }


  //#########################################################################
  //# Data Members
  private final KindTranslator mParent;
  private Map<TRAutomatonProxy,ComponentKind> mExtensionMap;

}
