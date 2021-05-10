//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.hisc;

import java.util.Map;

import net.sourceforge.waters.model.analysis.kindtranslator.ConflictKindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * <P>A kind translator used for HISC-CP conflict checking.
 * This translator remaps all plants, specifications, and supervisors as
 * plants and suppresses properties, and in addition all interface automata
 * are remapped as plants regardless of their type. All controllable and
 * uncontrollable events are remapped as uncontrollable.</P>
 *
 * @author Robi Malik
 */

public class HISCConflictKindTranslator
  extends ConflictKindTranslator
{

  //#########################################################################
  //# Singleton Pattern
  public static HISCConflictKindTranslator getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final HISCConflictKindTranslator theInstance =
      new HISCConflictKindTranslator();
  }

  private HISCConflictKindTranslator()
  {
    super(EventKind.UNCONTROLLABLE);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator
  /**
   * Returns the component kind of the given automaton in a conflict
   * check.
   * @return {@link ComponentKind#PLANT}, if the given automaton is
   *         a plant, spec, supervisor, or an interface.
   */
  @Override
  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    final Map<String,String> attribs = aut.getAttributes();
    if (HISCAttributeFactory.isInterface(attribs)) {
      return ComponentKind.PLANT;
    } else {
      return super.getComponentKind(aut);
    }
  }

}
