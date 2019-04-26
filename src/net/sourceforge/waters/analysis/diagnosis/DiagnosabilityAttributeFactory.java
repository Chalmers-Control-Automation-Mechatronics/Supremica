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

package net.sourceforge.waters.analysis.diagnosis;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.AttributeFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * The attribute factory for diagnosability.
 * The diagnosability factory specifies the attribute with name
 * {@link #FAULT_KEY} for events, which declares events as faults and
 * also specifies a fault class through its value.
 *
 * @author Robi Malik
 */

public class DiagnosabilityAttributeFactory implements AttributeFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static DiagnosabilityAttributeFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private DiagnosabilityAttributeFactory()
  {
  }

  private static class SingletonHolder {
    private static final DiagnosabilityAttributeFactory INSTANCE =
      new DiagnosabilityAttributeFactory();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.AttributeFactory
  @Override
  public Collection<String> getApplicableKeys
    (final Class<? extends Proxy> clazz)
  {
    if (clazz.isAssignableFrom(EventDeclProxy.class)) {
      return ATTRIBUTES_FOR_EVENT;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public List<String> getApplicableValues(final String attrib)
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# String Constants
  /**
   * The attribute key used to define an event as a fault and provide its
   * fault class.
   */
  public static final String FAULT_KEY = "FAULT";


   //#########################################################################
  //# Attribute List Constants
  private static final Collection<String> ATTRIBUTES_FOR_EVENT =
    Collections.singletonList(FAULT_KEY);

}
