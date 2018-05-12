//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
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
 * handle and register it with a {@link DocumentManager}.</P>
 *
 * @see DocumentManager
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
      (Marshaller.JAXB_ENCODING, Charset.defaultCharset().toString());
    mJAXBMarshaller.setProperty
      (Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
  }


  //#########################################################################
  //# Simple Access
  JAXBDocumentExporter<D,T> getExporter()
  {
    return mExporter;
  }

  JAXBDocumentImporter<D,T> getImporter()
  {
    return mImporter;
  }


  //#########################################################################
  //# Configuration
  /**
   * Returns whether geometry information is provided when unmarshalling.
   * @see #setImportingGeometry(boolean)
   */
  boolean isImportingGeometry()
  {
    return mImporter.isImportingGeometry();
  }

  /**
   * Specifies whether geometry information is provided when unmarshalling.
   * If set to true <CODE>true</CODE> (the default), then all geometry
   * information is included in the loaded object model, otherwise all
   * geometry objects are set to <CODE>null</CODE> regardless of the file
   * contents. This option can be useful to speed up command line tools
   * that do not require geometry information: it saves some memory, and
   * the AWT library does not need to be loaded.
   * @param  importgeo  <CODE>true</CODE> if geometry information is provided,
   *                    <CODE>false</CODE> otherwise.
   */
  void setImportingGeometry(final boolean importgeo)
  {
    mImporter.setImportingGeometry(importgeo);
  }


  //#########################################################################
  //# Interfaces net.sourceforge.waters.model.marshaller.ProxyMarshaller
  //# and net.sourceforge.waters.model.marshaller.ProxyUnmarshaller
  /**
   * Load a document from a file.
   * @param  uri      A URI specifying the location of the document
   *                  to be retrieved.
   * @return The loaded document.
   * @throws WatersUnmarshalException to indicate that reading the XML file
   *                  has failed for some reason.
   */
  @Override
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
   * @throws WatersMarshalException to indicate a failure while writing the
   *                  XML structures.
   * @throws IOException to indicate that the output file could not be
   *                  opened.
   */
  @Override
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

  @Override
  public Collection<String> getSupportedExtensions()
  {
    final String ext = getDefaultExtension();
    return Collections.singletonList(ext);
  }

  @Override
  public FileFilter getDefaultFileFilter()
  {
    final String ext = getDefaultExtension();
    final String description = getDescription();
    return StandardExtensionFileFilter.getFilter(description, ext);
  }

  @Override
  public Collection<FileFilter> getSupportedFileFilters()
  {
    final FileFilter filter = getDefaultFileFilter();
    return Collections.singletonList(filter);
  }

  @Override
  public DocumentManager getDocumentManager()
  {
    return mImporter.getDocumentManager();
  }

  @Override
  public void setDocumentManager(final DocumentManager manager)
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
