//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   ProductDESToModuleUnmarshaller
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.xml.sax.SAXException;


/**
 * A utility to load a product DES (<CODE>.wdes</CODE>) file as a module.
 * This marshaller combines a {@link JAXBProductDESMarshaller} with
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
    throws JAXBException, SAXException
  {
    final ProductDESProxyFactory desfactory =
      ProductDESElementFactory.getInstance();
    mUnmarshaller = new JAXBProductDESMarshaller(desfactory);
    mImporter = new ProductDESImporter(modfactory);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
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

  public Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }

  public String getDefaultExtension()
  {
    return mUnmarshaller.getDefaultExtension();
  }

  public Collection<String> getSupportedExtensions()
  {
    return mUnmarshaller.getSupportedExtensions();
  }

  public Collection<FileFilter> getSupportedFileFilters()
  {
      return mUnmarshaller.getSupportedFileFilters();
  }

  public DocumentManager getDocumentManager()
  {
    return mImporter.getDocumentManager();
  }

  public void setDocumentManager(final DocumentManager manager)
  {
    mImporter.setDocumentManager(manager);
  }


  //#########################################################################
  //# Data Members
  private final ProxyUnmarshaller<ProductDESProxy> mUnmarshaller;
  private final ProductDESImporter mImporter;

}
