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

package net.sourceforge.waters.model.marshaller;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

import org.xml.sax.SAXException;


/**
 * <P>A helper class to read and write Waters Module (<CODE>.wmod</CODE>)
 * files.</P>
 *
 * <P>The most convenient way to use a SAXModuleMarshaller is to create an
 * instance and register it with a {@link DocumentManager} as a marshaller
 * and/or unmarshaller. The {@link DocumentManager} can automatically
 * recognise files by their extension and use the appropriate marshaller to
 * load or save their contents.</P>
 *
 * @see DocumentManager
 * @author Robi Malik
 */

public class SAXModuleMarshaller
  extends SAXMarshaller<ModuleProxy>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new module marshaller.
   * @param  factory   The factory to be used to create the objects when
   *                   loading a module from an XML file.
   * @param  optable   The operator table to be used to create the operators
   *                   when loading expressions in a module from an XML file.
   */
  public SAXModuleMarshaller(final ModuleProxyFactory factory,
                             final OperatorTable optable)
    throws SAXException, ParserConfigurationException
  {
    this(factory, optable, true);
  }

  /**
   * Creates a new module marshaller.
   * @param  factory   The factory to be used to create the objects when
   *                   loading a module from an XML file.
   * @param  optable   The operator table to be used to create the operators
   *                   when loading expressions in a module from an XML file.
   * @param  importGeo A flag, indicating whether geometry information should
   *                   be included when reading files. If <CODE>true</CODE>
   *                   (the default), then all geometry information is included
   *                   in the loaded object model, otherwise all geometry is
   *                   set to <CODE>null</CODE> regardless of the file
   *                   contents.
   * @throws ParserConfigurationException
   */
  public SAXModuleMarshaller(final ModuleProxyFactory factory,
                             final OperatorTable optable,
                             final boolean importGeo)
    throws SAXException, ParserConfigurationException
  {
    super("waters-module.xsd",
          new SAXModuleImporter(factory, optable));
    setImportingGeometry(importGeo);
  }


  //#########################################################################
  //# Interfaces
  //# net.sourceforge.waters.model.marshaller.ProxyMarshaller<ModuleProxy>
  //# net.sourceforge.waters.model.marshaller.ProxyUnmarshaller<ModuleProxy>
  @Override
  public String getDefaultExtension()
  {
    return ".wmod";
  }

  @Override
  public Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.marshaller.SAXMarshaller<ModuleProxy>
  @Override
  public String getDescription()
  {
      return "Waters Module files [*.wmod]";
  }

}
