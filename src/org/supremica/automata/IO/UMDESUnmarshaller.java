//# -*- indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   SupremicaUnmarshaller
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.IO;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

import org.xml.sax.SAXException;


public class UMDESUnmarshaller implements ProxyUnmarshaller<ModuleProxy>
{

  //#########################################################################
  //# Constructor
  public UMDESUnmarshaller(final ModuleProxyFactory modfactory)
    throws SAXException
  {
    builder = new ProjectBuildFromFSM();
    mImporter = new ProductDESImporter(modfactory);
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.model.marshaller.ProxyUnmarshaller<ModuleProxy>
  @Override
  public ModuleProxy unmarshal(final URI uri)
    throws WatersUnmarshalException, IOException
  {
    final URL url = uri.toURL();
    final ProductDESProxy des;
    try {
      des = builder.build(url);
      return mImporter.importModule(des);
    } catch (final Exception ex) {
      throw new WatersUnmarshalException(ex);
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
    return ".fsm";
  }

  @Override
  public Collection<String> getSupportedExtensions()
  {
    return Collections.singletonList(getDefaultExtension());
  }

  @Override
  public Collection<FileFilter> getSupportedFileFilters()
  {
    final FileFilter filter =
      new StandardExtensionFileFilter("UMDES files [*.fsm]",
                                      getDefaultExtension());
    return Collections.singletonList(filter);
  }


  //#########################################################################
  //# Data Members
  private final ProjectBuildFromFSM builder;
  private final ProductDESImporter mImporter;
}
