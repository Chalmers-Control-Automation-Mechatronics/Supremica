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

package net.sourceforge.waters.model.marshaller;

import net.sourceforge.waters.xsd.SchemaModule;


/**
 * <P>Enumeration of possible list types in the XML schema.</P>
 *
 * <P>Depending on the context where a list appears, the schema
 * distinguishes between <CODE>EventList</CODE>, <CODE>ComponentList</CODE>,
 * and <CODE>EventAliasList</CODE>. Depend on the list type, different
 * element types are expected in the XML.</P>
 *
 * @author Robi Malik
 */

enum ListType
{
  ALIASES(SchemaModule.ELEMENT_EventAliasList,
          SchemaModule.ELEMENT_ConditionalEventAlias,
          SchemaModule.ELEMENT_ForeachEventAlias),
  EVENTS(SchemaModule.ELEMENT_EventList,
         SchemaModule.ELEMENT_ConditionalEvent,
         SchemaModule.ELEMENT_ForeachEvent),
  COMPONENTS(SchemaModule.ELEMENT_ComponentList,
             SchemaModule.ELEMENT_ConditionalComponent,
             SchemaModule.ELEMENT_ForeachComponent);

  //#######################################################################
  //# Constructor
  private ListType(final String listName,
                   final String conditionalName,
                   final String foreachName)
  {
    mListName = listName;
    mConditionalName = conditionalName;
    mForeachName = foreachName;
  }

  //#######################################################################
  //# Simple Access
  String getListName()
  {
    return mListName;
  }

  String getConditionalName()
  {
    return mConditionalName;
  }

  String getForeachName()
  {
    return mForeachName;
  }

  //#######################################################################
  //# Data Members
  private final String mListName;
  private final String mConditionalName;
  private final String mForeachName;

}
