//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   ProxyMarshaller
//###########################################################################
//# $Id: ProxyMarshaller.java,v 1.3 2005-07-08 01:05:34 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.File;
import java.util.Collection;


/**
 * <P>A helper class to read and write XML files.</P>
 *
 * <P>The ProxyMarshaller is a simple wrapper that provides an easy way
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

public interface ProxyMarshaller<D extends DocumentProxy>
{

  //#########################################################################
  //# Access Methods
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
  public D unmarshal(final File filename)
    throws Exception;

  /**
   * Write a document to a file.
   * @param  filename The name of the file to be written.
   * @param  docproxy The document to be written.
   * @throws JAXBException to indicate a failure while writing the
   *                  XML structures.
   * @throws IOException to indicate that the output file could not be
   *                  opened.
   */
  public void marshal(final D docproxy, final File filename)
    throws Exception;


  //#########################################################################
  //# Provided by Subclasses
  /**
   * Get a default extension for the XML files handled by this wrapper.
   */
  public String getDefaultExtension();

  public Collection<String> getSupportedExtensions();

  public Collection<Class> getMarshalledClasses();

}
