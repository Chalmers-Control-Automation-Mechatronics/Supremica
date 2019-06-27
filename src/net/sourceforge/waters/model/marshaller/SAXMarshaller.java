//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import javax.swing.filechooser.FileFilter;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.sourceforge.waters.model.base.DocumentProxy;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * <P>A helper class to read and write XML files.</P>
 *
 * <P>The SAXMarshaller is a wrapper that provides an easy way
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

public abstract class SAXMarshaller<D extends DocumentProxy>
  implements ProxyMarshaller<D>, ProxyUnmarshaller<D>
{

  //#########################################################################
  //# Constructors
  public SAXMarshaller(final String schemaName,
                       final SAXDocumentImporter<D> importer,
                       final StAXDocumentWriter writer)
    throws SAXException, ParserConfigurationException
  {
    mImporter = importer;
    mWriter = writer;
    final SchemaFactory schemaFactory =
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final URL url = SAXMarshaller.class.getResource(schemaName);
    final Schema schema = schemaFactory.newSchema(url);
    final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    parserFactory.setSchema(schema);
    parserFactory.setNamespaceAware(true);
    mParser = parserFactory.newSAXParser();
  }


  //#########################################################################
  //# Configuration
  /**
   * Returns whether geometry information is provided when unmarshalling.
   * @see #setImportingGeometry(boolean)
   */
  public boolean isImportingGeometry()
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
   * @param  importing  <CODE>true</CODE> if geometry information is provided,
   *                    <CODE>false</CODE> otherwise.
   */
  public void setImportingGeometry(final boolean importing)
  {
    mImporter.setImportingGeometry(importing);
  }

  public boolean isInsertingLineBreaks()
  {
    return mWriter.isInsertingLineBreaks();
  }

  public void setInsertingLineBreaks(final boolean inserting)
  {
    mWriter.setInsertingLineBreaks(inserting);
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
    InputStream stream = null;
    final URL url = uri.toURL();
    try {
      stream = url.openStream();
      final InputSource source = new InputSource(stream);
      final XMLReader reader = mParser.getXMLReader();
      mImporter.setURI(uri);
      reader.setContentHandler(mImporter);
      reader.setErrorHandler(mImporter);
      reader.parse(source);
      return mImporter.getParsedDocument();
    } catch (final SAXException exception) {
      throw new WatersUnmarshalException(uri, exception);
    } finally {
      if (stream != null) {
        stream.close();
      }
    }
  }

  /**
   * Write a document to a file.
   * @param  filename The name of the file to be written.
   * @param  doc      The document to be written.
   * @throws WatersMarshalException to indicate a failure while writing the
   *                  XML structures.
   * @throws IOException to indicate that the output file could not be
   *                  opened.
   */
  @Override
  public void marshal(final D doc, final File filename)
    throws WatersMarshalException, IOException
  {
    try {
      mWriter.marshal(doc, filename);
    } catch (final XMLStreamException exception) {
      throw new WatersMarshalException(exception);
    }
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


  //#########################################################################
  //# Specific Access
  public void copyXMLFile(final File infilename, final File outfilename)
    throws FileNotFoundException, JAXBException
  {
    /*
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
    */
  }


  //#########################################################################
  //# Provided by Subclasses
  public abstract String getDescription();


  //#########################################################################
  //# Data Members
  private final SAXParser mParser;
  private final SAXDocumentImporter<D> mImporter;
  private final StAXDocumentWriter mWriter;

}
