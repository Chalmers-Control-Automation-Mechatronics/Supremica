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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedArrayList;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.xsd.SchemaBase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


public abstract class SAXDocumentImporter<D extends DocumentProxy>
  extends DefaultHandler
{

  //#########################################################################
  //# Constructors
  SAXDocumentImporter(final String schemaName,
                      final int estimatedSize)
    throws SAXException, ParserConfigurationException
  {
    final SchemaFactory schemaFactory =
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final URL url = SAXDocumentImporter.class.getResource(schemaName);
    final Schema schema = schemaFactory.newSchema(url);
    final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    parserFactory.setSchema(schema);
    parserFactory.setNamespaceAware(true);
    mParser = parserFactory.newSAXParser();
    mHandlerMap = new HashMap<>(estimatedSize);
  }


  //#########################################################################
  //# Invocation
  public D parse(final URI uri)
    throws SAXException, IOException
  {
    InputStream stream = null;
    final URL url = uri.toURL();
    try {
      setURI(uri);
      stream = url.openStream();
      final InputSource source = new InputSource(stream);
      return parse(source);
    } finally {
      setURI(null);
      if (stream != null) {
        stream.close();
      }
    }
  }

  public D parse(final InputSource source)
    throws SAXException, IOException
  {
    try {
      final XMLReader reader = mParser.getXMLReader();
      reader.setContentHandler(this);
      reader.setErrorHandler(this);
      reader.parse(source);
      return getParsedDocument();
    } finally {
      reset();
    }
  }

  public void setURI(final URI uri)
  {
    mURI = uri;
  }


  //#########################################################################
  //# Hooks
  abstract Class<D> getDocumentClass();

  void reset()
  {
  }


  //#########################################################################
  //# Configuration
  boolean isImportingGeometry()
  {
    return mImportingGeometry;
  }

  void setImportingGeometry(final boolean importing)
  {
    mImportingGeometry = importing;
  }

  void registerHandler(final String name, final SAXHandlerCreator<?> creator)
  {
    mHandlerMap.put(name, creator);
  }

  URI getURI()
  {
    return mURI;
  }


  //#########################################################################
  //# Interface org.xml.sax.ContentHandler
  @Override
  public void startElement(final String uri,
                           final String localName,
                           final String qName,
                           final Attributes atts)
    throws SAXException
  {
    if (mCurrentHandler == null) {
      pushHandler(localName, atts);
    } else {
      mCurrentHandler.startSubElement(localName, atts);
    }
  }

  @Override
  public void endElement(final String uri,
                         final String localName,
                         final String qName)
    throws SAXException
  {
    mLastHandler = popHandler();
    final AbstractContentHandler<?> parent = mLastHandler.getParent();
    if (parent != null) {
      parent.endSubElement(mLastHandler);
    }
  }

  @Override
  public void characters(final char[] ch,
                         final int start,
                         final int length)
    throws SAXException
  {
    if (mCurrentHandler != null) {
      mCurrentHandler.characters(ch, start, length);
    }
  }

  @Override
  public void setDocumentLocator(final Locator locator)
  {
    mLocator = locator;
  }


  //#########################################################################
  //# Interface org.xml.sax.ErrorHandler
  @Override
  public void warning(final SAXParseException exception)
  {
    final Logger logger = LogManager.getLogger();
    logger.warn(exception.getMessage());
  }

  @Override
  public void error(final SAXParseException exception)
    throws SAXParseException
  {
    final Logger logger = LogManager.getLogger();
    logger.error(exception.getMessage());
    throw exception;
  }

  @Override
  public void fatalError(final SAXParseException exception)
    throws SAXParseException
  {
    final Logger logger = LogManager.getLogger();
    logger.error(exception.getMessage());
    throw exception;
  }


  //#########################################################################
  //# Parsing Support
  AbstractContentHandler<?> pushHandler(final String localName,
                                        final Attributes atts)
    throws SAXParseException
  {
    final SAXHandlerCreator<?> creator = mHandlerMap.get(localName);
    if (creator != null) {
      mCurrentHandler = creator.createHandler(mCurrentHandler);
      mCurrentHandler.setAttributes(atts);
    } else {
      mCurrentHandler = new IgnoringContentHandler(mCurrentHandler);
    }
    return mCurrentHandler;
  }

  void pushHandler(final AbstractContentHandler<?> handler)
  {
    mCurrentHandler = handler;
  }

  void pushHandler(final AbstractContentHandler<?> handler,
                   final Attributes atts)
    throws SAXParseException
  {
    mCurrentHandler = handler;
    handler.setAttributes(atts);
  }

  AbstractContentHandler<?> popHandler()
  {
    final AbstractContentHandler<?> popped = mCurrentHandler;
    mCurrentHandler = mCurrentHandler.getParent();
    return popped;
  }

  D getParsedDocument()
    throws SAXParseException
  {
    if (mLastHandler == null) {
      return null;
    } else {
      mLocator = null;
      final Class<D> clazz = getDocumentClass();
      final Object result = mLastHandler.getResult();
      try {
        return clazz.cast(result);
      } catch (final ClassCastException exception) {
        throw createTypeMismatchException(result, clazz, exception);
      }
    }
  }


  //#########################################################################
  //# Error Handling
  SAXParseException createTypeMismatchException
    (final Object found,
     final Class<?> expected,
     final ClassCastException cause)
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("Unexpected XML content - found a ");
    builder.append(ProxyTools.getShortProxyInterfaceName(found));
    builder.append(" where a ");
    builder.append(ProxyTools.getShortClassName(expected));
    builder.append(" is expected.");
    return createSAXParseException(builder.toString(), cause);
  }

  SAXParseException createSAXParseException(final String msg)
  {
    return new SAXParseException(msg, mLocator);
  }

  SAXParseException createSAXParseException(final String msg,
                                            final Exception cause)
  {
    return new SAXParseException(msg, mLocator, cause);
  }


  //#########################################################################
  //# Inner Class AbstractContentHandler
  abstract class AbstractContentHandler<T>
  {
    //#######################################################################
    //# Constructor
    AbstractContentHandler(final AbstractContentHandler<?> parent)
    {
      mParent = parent;
    }

    //#######################################################################
    //# Simple Access
    AbstractContentHandler<?> getParent()
    {
      return mParent;
    }

    <H extends AbstractContentHandler<?>>
    H getAncestor(final Class<H> clazz)
    {
      if (clazz.isAssignableFrom(getClass())) {
        return clazz.cast(this);
      } else if (mParent == null) {
        return null;
      } else {
        return mParent.getAncestor(clazz);
      }
    }

    //#######################################################################
    //# Hooks
    void setAttributes(final Attributes atts)
      throws SAXParseException
    {
      final int len = atts.getLength();
      for (int i = 0; i < len; i++) {
        final String attName = atts.getLocalName(i);
        final String value = atts.getValue(i);
        setAttribute(attName, value);
      }
    }

    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
    }

    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      pushHandler(localName, atts);
    }

    public void characters(final char[] ch,
                           final int start,
                           final int length)
      throws SAXParseException
    {
    }

    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
    }

    abstract T getResult() throws SAXParseException;

    void reset()
    {
    }

    //#######################################################################
    //# Data Members
    private final AbstractContentHandler<?> mParent;
  }


  //#########################################################################
  //# Inner Class IgnoringContentHandler
  class IgnoringContentHandler extends AbstractContentHandler<Object>
  {
    //#######################################################################
    //# Constructor
    IgnoringContentHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    void setAttributes(final Attributes atts)
    {
    }

    @Override
    Object getResult()
    {
      return null;
    }
  }


  //#########################################################################
  //# Inner Class TextContentHandler
  class TextContentHandler extends AbstractContentHandler<String>
  {
    //#######################################################################
    //# Constructor
    TextContentHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
      mBuilder = new StringBuilder();
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<String>
    @Override
    public void characters(final char[] ch,
                           final int start,
                           final int length)
    {
      final int end = start + length;
      for (int i = start; i < end; i++) {
        mBuilder.append(ch[i]);
      }
    }

    @Override
    String getResult()
    {
      return mBuilder.toString();
    }

    //#######################################################################
    //# Data Members
    private final StringBuilder mBuilder;
  }


  //#########################################################################
  //# Inner Class ListHandler
  abstract class ListHandler<T> extends AbstractContentHandler<List<T>>
  {
    //#######################################################################
    //# Constructor
    ListHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }
  }


  //#########################################################################
  //# Inner Class SimpleListHandler
  abstract class SimpleListHandler<T> extends ListHandler<T>
  {
    //#######################################################################
    //# Constructor
    SimpleListHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
      mList = new ArrayList<>();
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<List<T>>
    @Override
    List<T> getResult()
    {
      return mList;
    }

    //#######################################################################
    //# Parsing Support
    void add(final T item)
    {
      if (item != null) {
        mList.add(item);
      }
    }

    //#######################################################################
    //# Data Members
    private final List<T> mList;
  }


  //#########################################################################
  //# Inner Class GenericListHandler
  class GenericListHandler<T> extends SimpleListHandler<T>
  {
    //#######################################################################
    //# Constructor
    GenericListHandler(final AbstractContentHandler<?> parent,
                       final Class<T> clazz)
    {
      super(parent);
      mItemClass = clazz;
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      final Object object = subHandler.getResult();
      try {
        final T item = mItemClass.cast(object);
        add(item);
      } catch (final ClassCastException exception) {
        // ignore
      }
    }

    //#######################################################################
    //# Data Members
    private final Class<T> mItemClass;
  }


  //#########################################################################
  //# Inner Class StaticListHandler
  class StaticListHandler<T> extends SimpleListHandler<T>
  {
    //#######################################################################
    //# Constructor
    StaticListHandler(final AbstractContentHandler<?> parent,
                      final String itemKey,
                      final SAXHandlerCreator<T> itemHandlerCreator)
    {
      super(parent);
      mItemKey = itemKey;
      mItemHandler = itemHandlerCreator.createHandler(this);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<List<T>>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(mItemKey)) {
        pushHandler(mItemHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      if (subHandler == mItemHandler) {
        final T item = mItemHandler.getResult();
        mItemHandler.reset();
        add(item);
      }
    }

    //#######################################################################
    //# Data Members
    private final String mItemKey;
    private final AbstractContentHandler<T> mItemHandler;
  }


  //#########################################################################
  //# Inner Class UniqueListHandler
  abstract class UniqueListHandler<P extends NamedProxy>
    extends ListHandler<P>
  {
    //#######################################################################
    //# Constructor
    UniqueListHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
      mList = new IndexedArrayList<>();
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<List<P>>
    @Override
    List<P> getResult()
    {
      return mList;
    }

    //#######################################################################
    //# Parsing Support
    void insert(final P item)
    {
      if (item != null) {
        mList.insert(item);
      }
    }

    P get(final String name)
    {
      return mList.get(name);
    }

    //#######################################################################
    //# Data Members
    private final IndexedList<P> mList;
  }


  //#########################################################################
  //# Inner Class GenericUniqueListHandler
  class GenericUniqueListHandler<P extends NamedProxy>
    extends UniqueListHandler<P>
  {
    //#######################################################################
    //# Constructor
    GenericUniqueListHandler(final AbstractContentHandler<?> parent,
                             final Class<P> clazz)
    {
      super(parent);
      mItemClass = clazz;
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      final Object object = subHandler.getResult();
      P item = null;
      try {
        item = mItemClass.cast(object);
        insert(item);
      } catch (final ClassCastException exception) {
        return;
      } catch (final DuplicateNameException exception) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Invalid XML content - found two ");
        builder.append(ProxyTools.getShortClassName(mItemClass));
        builder.append(" entries both named '");
        builder.append(item.getName());
        builder.append("'.");
        throw createSAXParseException(builder.toString());
      }
    }

    //#######################################################################
    //# Data Members
    private final Class<P> mItemClass;
  }


  //#########################################################################
  //# Inner Class StaticUniqueListHandler
  class StaticUniqueListHandler<P extends NamedProxy>
    extends UniqueListHandler<P>
  {
    //#######################################################################
    //# Constructor
    StaticUniqueListHandler(final AbstractContentHandler<?> parent,
                            final String itemKey,
                            final SAXHandlerCreator<P> itemHandlerCreator)
    {
      super(parent);
      mItemKey = itemKey;
      mItemHandler = itemHandlerCreator.createHandler(this);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<List<T>>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(mItemKey)) {
        pushHandler(mItemHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      final P item;
      if (subHandler == mItemHandler) {
        item = mItemHandler.getResult();
        mItemHandler.reset();
        try {
          insert(item);
        } catch (final DuplicateNameException exception) {
          final StringBuilder builder = new StringBuilder();
          builder.append("Invalid XML content - found two ");
          builder.append(ProxyTools.getShortProxyInterfaceName(item));
          builder.append(" entries both named '");
          builder.append(item.getName());
          builder.append("'.");
          throw createSAXParseException(builder.toString());
        }
      }
    }

    //#######################################################################
    //# Data Members
    private final String mItemKey;
    private final AbstractContentHandler<P> mItemHandler;
  }


  //#########################################################################
  //# Inner Class SingletonHandler
  class SingletonHandler<T> extends AbstractContentHandler<T>
  {
    //#######################################################################
    //# Constructor
    SingletonHandler(final AbstractContentHandler<?> parent,
                     final Class<T> clazz)
    {
      super(parent);
      mElementClass = clazz;
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<List<T>>
    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      if (mResult == null) {
        final Object item = subHandler.getResult();
        if (item != null) {
          try {
            mResult = mElementClass.cast(item);
          } catch (final ClassCastException exception) {
            // ignore
          }
        }
      }
    }

    @Override
    T getResult()
    {
      return mResult;
    }

    //#######################################################################
    //# Data Members
    private final Class<T> mElementClass;
    private T mResult = null;
  }


  class AttributeMapHandler extends AbstractContentHandler<Map<String,String>>
  {
    //#######################################################################
    //# Constructor
    AttributeMapHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
      mMap = new HashMap<>();
      mAttributeHandler = new AttributeHandler(this);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<Map<String,String>>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaBase.ELEMENT_Attribute)) {
        mAttributeHandler.reset();
        pushHandler(mAttributeHandler, atts);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      if (subHandler == mAttributeHandler) {
        mAttributeHandler.put(mMap);
      }
    }

    @Override
    Map<String,String> getResult()
    {
      return mMap;
    }

    //#######################################################################
    //# Parsing Support
    void put(final String name, final String value)
    {
      mMap.put(name, value);
    }

    //#######################################################################
    //# Data Members
    private final Map<String,String> mMap;
    private final AttributeHandler mAttributeHandler;
  }


  class AttributeHandler extends AbstractContentHandler<Object>
  {
    //#######################################################################
    //# Constructor
    AttributeHandler(final AttributeMapHandler parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    void setAttribute(final String localName, final String value)
    {
      if (localName.equals(SchemaBase.ATTRIB_Name)) {
        mName = value;
      } else if (localName.equals(SchemaBase.ATTRIB_Value)) {
        mValue = value;
      }
    }

    @Override
    Object getResult()
    {
      return null;
    }

    @Override
    void reset()
    {
      mName = mValue = "";
    }

    //#######################################################################
    //# Parsing Support
    void put(final Map<String,String> map)
      throws SAXParseException
    {
      final String existing = map.put(mName, mValue);
      if (existing != null) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Invalid XML content - " +
                       "multiple values assigned to attribute '");
        builder.append(mName);
        builder.append("'.");
        throw createSAXParseException(builder.toString());
      }
    }

    //#######################################################################
    //# Data Members
    private String mName;
    private String mValue;
  }


  //#########################################################################
  //# Inner Class NamedProxyHandler
  abstract class NamedProxyHandler<P extends NamedProxy>
    extends AbstractContentHandler<P>
  {
    //#######################################################################
    //# Constructor
    NamedProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    void setAttribute(final String localName, final String value)
      throws SAXParseException
    {
      if (localName.equals(SchemaBase.ATTRIB_Name)) {
        mName = value;
      }
    }

    //#######################################################################
    //# Parsing Support
    String getName()
    {
      return mName;
    }

    //#######################################################################
    //# Data Members
    private String mName = null;
  }


  //#########################################################################
  //# Inner Class DocumentProxyHandler
  abstract class DocumentProxyHandler<P extends DocumentProxy>
    extends NamedProxyHandler<P>
  {
    //#######################################################################
    //# Constructor
    DocumentProxyHandler(final AbstractContentHandler<?> parent)
    {
      super(parent);
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<?>
    @Override
    public void startSubElement(final String localName,
                                final Attributes atts)
      throws SAXParseException
    {
      if (localName.equals(SchemaBase.ELEMENT_Comment)) {
        mCommentHandler = new TextContentHandler(this);
        pushHandler(mCommentHandler);
      } else {
        super.startSubElement(localName, atts);
      }
    }

    //#######################################################################
    //# Parsing Support
    String getComment()
    {
      return mCommentHandler == null ? null : mCommentHandler.getResult();
    }

    //#######################################################################
    //# Data Members
    private TextContentHandler mCommentHandler;
  }


  //#########################################################################
  //# Inner Class SAXHandlerCreator
  abstract class SAXHandlerCreator<T>
  {
    abstract AbstractContentHandler<T> createHandler
      (final AbstractContentHandler<?> parent);
  }


  //#########################################################################
  //# Data Members
  private final SAXParser mParser;
  private final Map<String,SAXHandlerCreator<?>> mHandlerMap;

  private boolean mImportingGeometry = true;
  private URI mURI = null;
  private Locator mLocator;

  private AbstractContentHandler<?> mCurrentHandler;
  private AbstractContentHandler<?> mLastHandler;

}
