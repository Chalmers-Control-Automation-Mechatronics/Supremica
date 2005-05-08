//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   JAXBMarshaller
//###########################################################################
//# $Id: JAXBMarshaller.java,v 1.1 2005-05-08 00:27:15 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.sourceforge.waters.xsd.base.NamedType;


/**
 * <P>A helper class to read and write XML files.</P>
 *
 * <P>The JAXBMarshaller is a simple wrapper that provides an easy way
 * to convert between XML files and their corresponding object structures.
 * The object structures obtained are called <I>documents</I> and all
 * implement the {@link DocumentProxy} interface.</P> 
 * 
 * <P>This abstract base class implements the core of the wrapper
 * functionality, but is not intended to be used directly. Instead, use one
 * of the subclasses depending on the type of document you want to
 * handle.</P>
 *
 * @author Robi Malik
 */

public abstract class JAXBMarshaller implements ProxyMarshaller
{

  //#########################################################################
  //# Constructors
  public JAXBMarshaller(final String packname)
    throws JAXBException
  {
    final JAXBContext context = JAXBContext.newInstance(packname);
    mUnmarshaller = context.createUnmarshaller();
    mUnmarshaller.setValidating(true);
    mMarshaller = context.createMarshaller();
    mMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.ProxyMarshaller
  /**
   * Load a document from a file.
   * @param  filename The name of the file to be loaded.
   * @return The loaded document.
   * @throws JAXBException to indicate that reading the XML file
   *                  has failed for some reason.
   * @throws ModelException to indicate that the loaded XML could
   *                  not be converted to proper classes due to
   *                  serious semantic inconsistencies.
   */
  public DocumentProxy unmarshal(final File filename)
    throws JAXBException, ModelException
  {
    final NamedType doc = (NamedType) mUnmarshaller.unmarshal(filename);
    final DocumentProxy docproxy = createProxy(doc, filename);
    return docproxy;
  }

  /**
   * Write a document to a file.
   * @param  filename The name of the file to be written.
   * @param  docproxy The document to be written.
   * @throws JAXBException to indicate a failure while writing the
   *                  XML structures.
   * @throws IOException to indicate that the output file could not be
   *                  opened.
   */
  public void marshal(final DocumentProxy docproxy,
                      final File filename)
     throws JAXBException, IOException
  {
    final NamedType doc = createElement(docproxy);
    final FileOutputStream stream = new FileOutputStream(filename);
    final PrintWriter writer = new PrintWriter(stream);
    try {
      mMarshaller.marshal(doc, writer);
    } finally {
      writer.close();
    }
  }

  public Collection getSupportedExtensions()
  {
    final String ext = getDefaultExtension();
    return Collections.singletonList(ext);
  }

  public Collection getMarshalledClasses()
  {
    final Class clazz = getOutputClass();
    return Collections.singletonList(clazz);
  }


  //#########################################################################
  //# Provided by Subclasses
  public abstract DocumentProxy createProxy(NamedType doc, File location)
    throws ModelException;
  public abstract NamedType createElement(DocumentProxy docproxy)
    throws JAXBException;

  /**
   * Get the class of the documents handled by this wrapper.
   */
  public abstract Class getOutputClass();


  //#########################################################################
  //# Data Members
  private final Marshaller mMarshaller;
  private final Unmarshaller mUnmarshaller;

}
