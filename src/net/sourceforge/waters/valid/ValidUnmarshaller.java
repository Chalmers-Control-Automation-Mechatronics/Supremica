//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   ValidUnmarshaller
//###########################################################################
//# $Id: ValidUnmarshaller.java,v 1.4 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.valid;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;

import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBDocumentImporter;
import net.sourceforge.waters.model.marshaller.JAXBModuleImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;

import net.sourceforge.waters.xsd.module.ModuleType;


public class ValidUnmarshaller
  implements ProxyUnmarshaller<ModuleProxy>
{

  //#########################################################################
  //# Constructor
  public ValidUnmarshaller(final ModuleProxyFactory factory,
                           final OperatorTable optable)
    throws JAXBException
  {
    final JAXBContext context =
      JAXBContext.newInstance("net.sourceforge.waters.xsd.module");
    mUnmarshaller = context.createUnmarshaller();
    mUnmarshaller.setValidating(false);
    mImporter = new JAXBModuleImporter(factory, optable);
  }


  //#########################################################################
  //# Access Methods
  public ModuleProxy unmarshal(File filename)
    throws WatersUnmarshalException, IOException
  {
    try {
      final String name = filename.toString();
      if (name.endsWith(".vprj")) {
        final int len = name.length();
        final String newname = name.substring(0, len - 5) + "_main.vmod";
        filename = new File(newname);
      }
      final ValidTransformer transformer = new ValidTransformer(filename);
      final Source source = transformer.getSource();
      transformer.start();
      final ModuleType module = (ModuleType) mUnmarshaller.unmarshal(source);
      final ModuleProxy modproxy = mImporter.importDocument(module, null);
      return modproxy;
    } catch (final JAXBException exception) {
      throw new WatersUnmarshalException(exception);
    } catch (final TransformerConfigurationException exception) {
      throw new WatersUnmarshalException(exception);
    }
  }

  public Class<ModuleProxy> getDocumentClass()
  {
    return ModuleProxy.class;
  }

  public String getDefaultExtension()
  {
    return EXT_VMOD;
  }

  public Collection<String> getSupportedExtensions()
  {
    return EXTENSIONS;
  }


  //#########################################################################
  //# Data Members
  private final Unmarshaller mUnmarshaller;
  private final JAXBDocumentImporter<ModuleProxy,ModuleType> mImporter;

  private static final Collection<String> EXTENSIONS;
  private static final String EXT_VMOD = ".vmod";
  private static final String EXT_VPRJ = ".vprj";

  static {
    final Collection<String> exts = new LinkedList<String>();
    exts.add(EXT_VMOD);
    exts.add(EXT_VPRJ);
    EXTENSIONS = Collections.unmodifiableCollection(exts);
  }

}
