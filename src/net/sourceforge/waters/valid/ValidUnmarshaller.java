//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.valid
//# CLASS:   ValidUnmarshaller
//###########################################################################
//# $Id: ValidUnmarshaller.java,v 1.2 2005-05-08 00:27:15 robi Exp $
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

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.xsd.module.ModuleType;


public class ValidUnmarshaller implements ProxyMarshaller
{

  //#########################################################################
  //# Constructor
  public ValidUnmarshaller()
    throws JAXBException
  {
    final JAXBContext context =
      JAXBContext.newInstance("net.sourceforge.waters.xsd.module");
    mUnmarshaller = context.createUnmarshaller();
    mUnmarshaller.setValidating(false);
  }


  //#########################################################################
  //# Access Methods
  public DocumentProxy unmarshal(File filename)
    throws IOException, JAXBException, ModelException,
           TransformerConfigurationException
  {
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
    final ModuleProxy modproxy = new ModuleProxy(module, filename);
    return modproxy;
  }

  public void marshal(final DocumentProxy docproxy,
                      final File filename)
  {
    throw new UnsupportedOperationException
      ("Marshalling of VALID files is not supported!");
  }

  public String getDefaultExtension()
  {
    return null;
  }

  public Collection getSupportedExtensions()
  {
    return EXTENSIONS;
  }

  public Collection getMarshalledClasses()
  {
    return Collections.EMPTY_LIST;
  }


  //#########################################################################
  //# Data Members
  private final Unmarshaller mUnmarshaller;

  private static final Collection EXTENSIONS = new LinkedList();
  private static final String EXT_VMOD = ".vmod";
  private static final String EXT_VPRJ = ".vprj";

  static {
    EXTENSIONS.add(EXT_VMOD);
    EXTENSIONS.add(EXT_VPRJ);
  }

}
