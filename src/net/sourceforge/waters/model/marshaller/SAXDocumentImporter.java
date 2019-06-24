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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.xsd.SchemaBase;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public abstract class SAXDocumentImporter<D extends DocumentProxy>
  extends DefaultHandler
{

  //#########################################################################
  //# Constructors
  SAXDocumentImporter(final int estimatedSize)
  {
    mHandlerMap = new HashMap<>(estimatedSize);
  }


  //#########################################################################
  //# Hooks
  abstract Class<D> getDocumentClass();


  //#########################################################################
  //# Configuration
  public DocumentManager getDocumentManager()
  {
    return mDocumentManager;
  }

  public void setDocumentManager(final DocumentManager manager)
  {
    mDocumentManager = manager;
  }

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

  void setURI(final URI uri)
  {
    mURI = uri;
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
    builder.append(ProxyTools.getShortClassName(found));
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
      throws SAXException
    {
      pushHandler(localName, atts);
    }

    public void characters(final char[] ch,
                           final int start,
                           final int length)
      throws SAXException
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
  class ListHandler<T> extends AbstractContentHandler<List<T>>
  {
    //#######################################################################
    //# Constructor
    ListHandler(final AbstractContentHandler<?> parent,
                final Class<T> clazz)
    {
      super(parent);
      mList = new ArrayList<>();
      mElementClass = clazz;
    }

    //#######################################################################
    //# Overrides for AbstractContentHandler<List<T>>
    @Override
    void endSubElement(final AbstractContentHandler<?> subHandler)
      throws SAXParseException
    {
      final Object item = subHandler.getResult();
      if (item != null) {
        try {
          final T cast = mElementClass.cast(item);
          mList.add(cast);
        } catch (final ClassCastException exception) {
          // ignore
        }
      }
    }

    @Override
    List<T> getResult()
    {
      return mList;
    }

    //#######################################################################
    //# Data Members
    private final List<T> mList;
    private final Class<T> mElementClass;
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
      throws SAXException
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
    {
      map.put(mName, mValue);
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
      throws SAXException
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
  private final Map<String,SAXHandlerCreator<?>> mHandlerMap;

  private DocumentManager mDocumentManager;
  private boolean mImportingGeometry = true;
  private URI mURI = null;
  private Locator mLocator;

  private AbstractContentHandler<?> mCurrentHandler;
  private AbstractContentHandler<?> mLastHandler;

}
