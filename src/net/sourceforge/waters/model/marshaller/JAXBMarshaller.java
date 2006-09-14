//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   JAXBMarshaller
//###########################################################################
//# $Id: JAXBMarshaller.java,v 1.5 2006-09-14 21:10:21 flordal Exp $
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import javax.swing.filechooser.FileFilter;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ModelException;

import net.sourceforge.waters.xsd.base.NamedType;
import org.supremica.gui.StandardExtensionFileFilter;

import org.xml.sax.SAXException;


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

public abstract class JAXBMarshaller
  <D extends DocumentProxy, T extends NamedType>
  implements ProxyMarshaller<D>, ProxyUnmarshaller<D>
{

  //#########################################################################
  //# Constructors
  public JAXBMarshaller(final JAXBDocumentExporter<D,T> exporter,
                        final JAXBDocumentImporter<D,T> importer,
                        final String packname,
                        final String schemaname)
    throws JAXBException, SAXException
  {
    mExporter = exporter;
    mImporter = importer;
    final SchemaFactory factory =
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final URL url = JAXBMarshaller.class.getResource(schemaname);
    final Schema schema = factory.newSchema(url);
    final JAXBContext context = JAXBContext.newInstance(packname);
    mJAXBUnmarshaller = context.createUnmarshaller();
    mJAXBUnmarshaller.setSchema(schema);
    mJAXBMarshaller = context.createMarshaller();
    mJAXBMarshaller.setProperty
      (Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
  }


  //#########################################################################
  //# Interfaces net.sourceforge.waters.model.marshaller.ProxyMarshaller
  //# and net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
  /**
   * Load a document from a file.
   * @param  uri      A URI specifiying the location of the document
   *                  to be retrieved.
   * @return The loaded document.
   * @throws WatersUnmarshalException to indicate that reading the XML file
   *                  has failed for some reason.
   */
  public D unmarshal(final URI uri)
    throws WatersUnmarshalException, IOException
  {
    try {
      final URL url = uri.toURL();
      final Object unmarshalled = mJAXBUnmarshaller.unmarshal(url);
      final Class<T> clazz = getElementClass();
      final T doc = clazz.cast(unmarshalled);
      final D docproxy = mImporter.importDocument(doc, uri);
      return docproxy;
    } catch (final JAXBException exception) {
      throw new WatersUnmarshalException(uri, exception);
    } catch (final ModelException exception) {
      throw new WatersUnmarshalException(uri, exception);
    }
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
  public void marshal(final D docproxy, final File filename)
    throws WatersMarshalException, IOException
  {
    PrintWriter writer = null;
    try {
      final T doc = mExporter.export(docproxy);
      final FileOutputStream stream = new FileOutputStream(filename);
      writer = new PrintWriter(stream);
      mJAXBMarshaller.marshal(doc, writer);
    } catch (final JAXBException exception) {
      throw new WatersMarshalException(filename, exception);
    } catch (final ModelException exception) {
      throw new WatersMarshalException(filename, exception);
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  public Collection<String> getSupportedExtensions()
  {
    final String ext = getDefaultExtension();
    return Collections.singletonList(ext);
  }

  public Collection<FileFilter> getSupportedFileFilters()
  {
      final String ext = getDefaultExtension();
      final String description = getDescription();
      FileFilter filter = new StandardExtensionFileFilter(ext, description);
      return Collections.singletonList(filter);
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
  //# Specific Access
  public void copyXMLFile(final File infilename, final File outfilename)
    throws FileNotFoundException, JAXBException
  {
    PrintWriter writer = null;
    try {
      final Object doc = mJAXBUnmarshaller.unmarshal(infilename);
      final FileOutputStream stream = new FileOutputStream(outfilename);
      writer = new PrintWriter(stream);
      mJAXBMarshaller.marshal(doc, writer);
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }


  //#########################################################################
  //# Provided by Subclasses
  public abstract Class<T> getElementClass();
  public abstract String getDescription();

  //#########################################################################
  //# Data Members
  private final JAXBDocumentExporter<D,T> mExporter;
  private final JAXBDocumentImporter<D,T> mImporter;
  private final Marshaller mJAXBMarshaller;
  private final Unmarshaller mJAXBUnmarshaller;

}
