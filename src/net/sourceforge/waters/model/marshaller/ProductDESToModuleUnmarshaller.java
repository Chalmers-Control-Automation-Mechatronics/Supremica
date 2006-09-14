//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   ProductDESToModuleUnmarshaller
//###########################################################################
//# $Id: ProductDESToModuleUnmarshaller.java,v 1.1 2006-09-14 11:31:12 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.xml.sax.SAXException;


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

  public ProductDESToModuleUnmarshaller(final ModuleProxyFactory modfactory,
                                        final DocumentManager manager)
    throws JAXBException, SAXException
  {
    this(modfactory);
    setDocumentManager(manager);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
  public ModuleProxy unmarshal(final URI uri)
    throws WatersUnmarshalException, IOException
  {
    final ProductDESProxy des = mUnmarshaller.unmarshal(uri);
    return mImporter.importModule(des);
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

  public DocumentManager getDocumentManager()
  {
    return mImporter.getDocumentManager();
  }

  public void setDocumentManager(DocumentManager manager)
  {
    mImporter.setDocumentManager(manager);
  }


  //#########################################################################
  //# Data Members
  private final ProxyUnmarshaller<ProductDESProxy> mUnmarshaller;
  private final ProductDESImporter mImporter;

}
