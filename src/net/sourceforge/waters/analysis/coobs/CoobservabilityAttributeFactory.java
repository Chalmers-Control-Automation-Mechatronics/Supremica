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

package net.sourceforge.waters.analysis.coobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.AttributeFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * The attribute factory for coobservability.
 * The coobservability factory specifies the attributes with name
 * {@link #CONTROLLABITY_KEY} and {@link #OBSERVABITY_KEY} for events,
 * which are used to define the name of a site or controller (an arbitrary
 * string) that can control or disable the event with the attribute.
 * To support events controlled or observed by more than one site, any
 * attribute that starts with these keys can be used to define additional
 * controlling or observing sites for an event.
 *
 * @author Robi Malik
 */

public class CoobservabilityAttributeFactory implements AttributeFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static CoobservabilityAttributeFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private CoobservabilityAttributeFactory()
  {
  }

  private static class SingletonHolder {
    private static final CoobservabilityAttributeFactory INSTANCE =
      new CoobservabilityAttributeFactory();
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
   * The attribute key prefix used to define a site or controller that can
   * disable an event.
   */
  public static final String CONTROLLABITY_KEY = "COOBS:controller";

  /**
   * The attribute key prefix used to define a site or controller that can
   * observe an event.
   */
  public static final String OBSERVABITY_KEY = "COOBS:observer";


  //#########################################################################
  //# Attribute List Constants
  private static final Collection<String> ATTRIBUTES_FOR_EVENT;

  static {
    ATTRIBUTES_FOR_EVENT = new ArrayList<>(2);
    ATTRIBUTES_FOR_EVENT.add(CONTROLLABITY_KEY);
    ATTRIBUTES_FOR_EVENT.add(OBSERVABITY_KEY);
  }

}
