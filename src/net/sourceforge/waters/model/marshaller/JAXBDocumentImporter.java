//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.waters.model.base.DocumentProxy;

import net.sourceforge.waters.xsd.base.Attribute;
import net.sourceforge.waters.xsd.base.AttributeMap;
import net.sourceforge.waters.xsd.base.NamedType;


public abstract class JAXBDocumentImporter
  <D extends DocumentProxy, T extends NamedType>
  extends JAXBImporter
{

  //#########################################################################
  //# Invocation
  public abstract D importDocument(final T element, final URI uri)
    throws WatersUnmarshalException;


  //#########################################################################
  //# Entity Resolving
  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  public void setDocumentManager(final DocumentManager manager)
  {
    mDocumentManager = manager;
  }


  //#########################################################################
  //# Importing Elements
  Map<String,String> importAttributeMap(final AttributeMap element)
  {
    if (element == null) {
      return null;
    } else {
      final List<Attribute> list = element.getList();
      final TreeMap<String,String> map = new TreeMap<String,String>();
      for (final Attribute attrib : list) {
        final String name = attrib.getName();
        final String value = attrib.getValue();
        map.put(name, value);
      }
      return map;
    }
  }


  //#########################################################################
  //# Data Members
  private DocumentManager mDocumentManager;

}
