//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.valid
//# CLASS:   ValidUnmarshaller
//###########################################################################
//# $Id: ValidUnmarshaller.java,v 1.1 2005-02-17 01:43:36 knut Exp $
//###########################################################################


package net.sourceforge.waters.valid;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.xsd.module.ModuleType;


public class ValidUnmarshaller
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
  public ModuleProxy unmarshal(final File filename)
    throws IOException, JAXBException, ModelException,
	   TransformerConfigurationException
  {
    final ValidTransformer transformer = new ValidTransformer(filename);
    final Source source = transformer.getSource();
    transformer.start();
    final ModuleType module = (ModuleType) mUnmarshaller.unmarshal(source);
    final ModuleProxy modproxy = new ModuleProxy(module, filename);
    return modproxy;
  }


  //#########################################################################
  //# Data Members
  private final Unmarshaller mUnmarshaller;

}