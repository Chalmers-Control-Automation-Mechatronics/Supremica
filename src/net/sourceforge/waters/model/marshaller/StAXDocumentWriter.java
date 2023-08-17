//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.GeometryProxy;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.xsd.SchemaBase;


public class StAXDocumentWriter
  implements ProxyVisitor
{

  //#########################################################################
  //# Configuration
  public boolean isInsertingLineBreaks()
  {
    return mInsertingLineBreaks;
  }

  public void setInsertingLineBreaks(final boolean inserting)
  {
    mInsertingLineBreaks = inserting;
  }


  //#########################################################################
  //# Invocation
  public void marshal(final DocumentProxy doc, final File filename)
    throws WatersMarshalException, XMLStreamException, IOException
  {
    try {
      mNamespacesWritten = false;
      final Charset charset = Charset.defaultCharset();
      final OutputStream stream = new FileOutputStream(filename);
      final Writer writer = new OutputStreamWriter(stream, charset);
      final XMLOutputFactory factory = XMLOutputFactory.newInstance();
      mWriter = factory.createXMLStreamWriter(writer);
      mWriter.writeStartDocument(charset.toString(), XML_VERSION);
      writeNewLine();
      doc.acceptVisitor(this);
      mWriter.writeEndDocument();
    } catch (final VisitorException exception) {
      final Throwable cause = exception.getCause();
      if (cause instanceof XMLStreamException) {
        throw (XMLStreamException) cause;
      } else {
        throw new WatersMarshalException(cause);
      }
    } finally {
      if (mWriter != null) {
        mWriter.flush();
        mWriter.close();
        mWriter = null;
      }
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.ProxyVisitor
  @Override
  public Object visitProxy(final Proxy proxy) throws VisitorException
  {
    writeNewLine();
    return null;
  }

  @Override
  public Object visitGeometryProxy(final GeometryProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  @Override
  public Object visitNamedProxy(final NamedProxy proxy)
    throws VisitorException
  {
    writeAttribute(SchemaBase.ATTRIB_Name, proxy.getName());
    return visitProxy(proxy);
  }

  @Override
  public Object visitDocumentProxy(final DocumentProxy proxy)
    throws VisitorException
  {
    writeNamespace(NAMESPACE_PREFIX, NAMESPACE);
    mNamespacesWritten = true;
    visitNamedProxy(proxy);
    final String comment = proxy.getComment();
    if (comment != null) {
      writeStartElement(NAMESPACE, SchemaBase.ELEMENT_Comment);
      writeCharacters(comment);
      writeEndElement();
    }
    return null;
  }


  //#########################################################################
  //# Formatting

  void writeStartElement(final String namespace,
                         final String localName,
                         final boolean empty)
    throws VisitorException
  {
    if (empty) {
      writeEmptyElement(namespace, localName);
    } else {
      writeStartElement(namespace, localName);
    }
  }

  void writeStartElement(final String namespace,
                         final String localName)
    throws VisitorException
  {
    try {
      assert !mEmptyElement : "Trying to create subelement of empty element!";
      if (mNamespacesWritten) {
        mWriter.writeStartElement(namespace, localName);
      } else {
        mWriter.writeStartElement(localName);
      }
    } catch (final XMLStreamException exception) {
      throw wrap(exception);
    }

  }

  void writeEmptyElement(final String namespace,
                         final String localName)
    throws VisitorException
  {
    try {
      assert !mEmptyElement : "Trying to create subelement of empty element!";
      mWriter.writeEmptyElement(namespace, localName);
      mEmptyElement = true;
    } catch (final XMLStreamException exception) {
      throw wrap(exception);
    }
  }

  void writeDefaultNamespace(final String uri)
    throws VisitorException
  {
    try {
      if (!mNamespacesWritten) {
        mWriter.writeDefaultNamespace(uri);
      }
    } catch (final XMLStreamException exception) {
      throw wrap(exception);
    }
  }

  void writeNamespace(final String prefix, final String uri)
    throws VisitorException
  {
    try {
      if (!mNamespacesWritten) {
        mWriter.writeNamespace(prefix, uri);
      }
    } catch (final XMLStreamException exception) {
      throw wrap(exception);
    }
  }

  void writeAttribute(final String localName, final String value)
    throws VisitorException
  {
    try {
      if (value != null) {
        mWriter.writeAttribute(localName, value);
      }
    } catch (final XMLStreamException exception) {
      throw wrap(exception);
    }
  }

  void writeAttribute(final String localName,
                      final String value,
                      final String defaultValue)
    throws VisitorException
  {
    if (value != null && !value.equals(defaultValue)) {
      writeAttribute(localName, value);
    }
  }

  void writeAttribute(final String localName, final boolean value)
    throws VisitorException
  {
    writeAttribute(localName, Boolean.toString(value));
  }

  void writeAttribute(final String localName,
                      final boolean value,
                      final boolean defaultValue)
    throws VisitorException
  {
    if (value != defaultValue) {
      writeAttribute(localName, value);
    }
  }

  void writeAttribute(final String localName, final int value)
    throws VisitorException
  {
    writeAttribute(localName, Integer.toString(value));
  }

  void writeAttribute(final String localName,
                      final int value,
                      final int defaultValue)
    throws VisitorException
  {
    if (value != defaultValue) {
      writeAttribute(localName, value);
    }
  }

  void writeIntAttribute(final String localName, final double value)
    throws VisitorException
  {
    final int intValue = (int) Math.round(value);
    writeAttribute(localName, intValue);
  }

  void writeAttribute(final String localName, final Object value)
    throws VisitorException
  {
    writeAttribute(localName, value.toString());
  }

  <E extends Enum<E>> void writeAttribute(final String localName,
                                          final E value,
                                          final E defaultValue)
    throws VisitorException
  {
    if (value != defaultValue) {
      writeAttribute(localName, value);
    }
  }

  void writeCharacters(final String text)
    throws VisitorException
  {
    try {
      assert !mEmptyElement : "Trying to write content into empty element!";
      mWriter.writeCharacters(text);
    } catch (final XMLStreamException exception) {
      throw wrap(exception);
    }
  }

  void writeEndElement()
    throws VisitorException
  {
    try {
      if (mEmptyElement) {
        mEmptyElement = false;
      } else {
        mWriter.writeEndElement();
      }
      writeNewLine();
    } catch (final XMLStreamException exception) {
      throw wrap(exception);
    }
  }

  void writeNewLine()
    throws VisitorException
  {
    try {
      if (mInsertingLineBreaks && !mEmptyElement) {
        mWriter.writeCharacters("\n");
      }
    } catch (final XMLStreamException exception) {
      throw wrap(exception);
    }
  }

  void writeOptionalItem(final Proxy proxy)
    throws VisitorException
  {
    if (proxy != null) {
      proxy.acceptVisitor(this);
    }
  }

  void writeOptionalList(final String uri,
                         final String localName,
                         final Collection<? extends Proxy> contents)
    throws VisitorException
  {
    if (contents != null && !contents.isEmpty()) {
      writeStartElement(uri, localName);
      writeNewLine();
      for (final Proxy proxy : contents) {
        proxy.acceptVisitor(this);
      }
      writeEndElement();
    }
  }

  void writeAttributeMap(final Map<String,String> attribs)
    throws VisitorException
  {
    if (attribs != null && !attribs.isEmpty()) {
      writeStartElement(NAMESPACE, SchemaBase.ELEMENT_AttributeMap);
      writeNewLine();
      for (final Map.Entry<String,String> entry : attribs.entrySet()) {
        writeEmptyElement(NAMESPACE, SchemaBase.ELEMENT_Attribute);
        writeAttribute(SchemaBase.ATTRIB_Name, entry.getKey());
        writeAttribute(SchemaBase.ATTRIB_Value, entry.getValue());
        writeEndElement();
      }
      writeEndElement();
    }
  }


  //#########################################################################
  //# Error Handling
  VisitorException wrap(final XMLStreamException exception)
  {
    return new VisitorException(exception);
  }


  //#########################################################################
  //# Data Members
  private boolean mInsertingLineBreaks = true;

  private XMLStreamWriter mWriter;
  private boolean mNamespacesWritten = false;
  private boolean mEmptyElement = false;


  //#########################################################################
  //# Class Constants
  private static final String XML_VERSION = "1.0";
  private static final String NAMESPACE = SchemaBase.NAMESPACE;
  private static final String NAMESPACE_PREFIX = "B";

}
