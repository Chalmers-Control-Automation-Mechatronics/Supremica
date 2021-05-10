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

package net.sourceforge.waters.model.marshaller;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.xml.sax.SAXException;


/**
 * A utility to load a product DES (<CODE>.wdes</CODE>) file as a module.
 * This marshaller combines a {@link SAXProductDESMarshaller} with
 * a {@link ProductDESImporter} to load a product DES and directly as a
 * module.
 *
 * @author Robi Malik
 */

public class ProductDESToModuleUnmarshaller
  implements ProxyUnmarshaller<ModuleProxy>
{

  //#########################################################################
  //# Constructor
  public ProductDESToModuleUnmarshaller(final ModuleProxyFactory modfactory)
    throws SAXException, ParserConfigurationException
  {
    final ProductDESProxyFactory desfactory =
      ProductDESElementFactory.getInstance();
    mUnmarshaller = new SAXProductDESMarshaller(desfactory);
    mImporter = new ProductDESImporter(modfactory);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.marshaller.ProxyUnmarshaller<ModuleProxy>
  @Override
  public ModuleProxy unmarshal(final URI uri)
    throws WatersUnmarshalException, IOException
  {
    try {
      final ProductDESProxy des = mUnmarshaller.unmarshal(uri);
      return mImporter.importModule(des);
    } catch (final ParseException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }

  @Override
  public Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }

  @Override
  public String getDefaultExtension()
  {
    return mUnmarshaller.getDefaultExtension();
  }

  @Override
  public Collection<String> getSupportedExtensions()
  {
    return mUnmarshaller.getSupportedExtensions();
  }

  @Override
  public Collection<FileFilter> getSupportedFileFilters()
  {
      return mUnmarshaller.getSupportedFileFilters();
  }


  //#########################################################################
  //# Data Members
  private final ProxyUnmarshaller<ProductDESProxy> mUnmarshaller;
  private final ProductDESImporter mImporter;

}
