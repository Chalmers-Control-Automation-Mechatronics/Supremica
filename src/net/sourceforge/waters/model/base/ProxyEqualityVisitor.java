//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   EqualityDiagnoser
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * @author Robi Malik
 */

public abstract class ProxyEqualityVisitor
  implements ProxyVisitor
{

  //#########################################################################
  //# Constructors
  public ProxyEqualityVisitor()
  {
    this(false);
  }

  public ProxyEqualityVisitor(final boolean diag)
  {
    this(diag, false);
  }

  public ProxyEqualityVisitor(final boolean diag, final boolean geo)
  {
    mIsProvidingDiagnostics = diag;
    mIsRespectingGeometry = geo;
    mDiagnosticPath = new LinkedList<Proxy>();
  }


  //#########################################################################
  //# Configuration
  public boolean isRespectingGeometry()
  {
    return mIsRespectingGeometry;
  }

  public boolean isProvidingDiagnostics()
  {
    return mIsProvidingDiagnostics;
  }


  //#########################################################################
  //# Provided by Subclasses
  public abstract ProxyHashCodeVisitor getHashCodeVisitor();


  //#########################################################################
  //# Invocation
  public boolean equals(final Proxy proxy, final Proxy expected)
  {
    try {
      mDiagnostics = null;
      mDiagnosticPath.clear();
      return compareProxies(proxy, expected);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }

  public String getDiagnostics()
  {
    final StringBuffer buffer = new StringBuffer();
    appendDiagnostics(buffer);
    return buffer.toString();
  }

  public void appendDiagnostics(final StringBuffer buffer)
  {
    if (mDiagnostics != null) {
      appendDiagnosticPath(buffer);
      mDiagnostics.append(buffer);
    } else {
      throw new IllegalStateException("Diagnostics not available!");
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.base.ProxyVisitor
  public Boolean visitProxy(final Proxy proxy)
  {
    if (proxy.getProxyInterface() == mSecondProxy.getProxyInterface()) {
      return true;
    } else {
      return reportItemMismatch(proxy, mSecondProxy);
    }
  }

  public Boolean visitGeometryProxy(final GeometryProxy proxy)
  {
    return visitProxy(proxy);
  }

  public Boolean visitNamedProxy(final NamedProxy proxy)
    throws VisitorException
  {
    if (visitProxy(proxy)) {
      final NamedProxy other = (NamedProxy) mSecondProxy;
      final String name1 = proxy.getName();
      final String name2 = other.getName();
      if (!name1.equals(name2)) {
        return reportNameMismatch(name2);
      }
      return true;
    } else {
      return false;
    }
  }

  public Boolean visitDocumentProxy(final DocumentProxy proxy)
    throws VisitorException
  {
    if (visitNamedProxy(proxy)) {
      final DocumentProxy other = (DocumentProxy) mSecondProxy;
      final String comment1 = proxy.getComment();
      final String comment2 = other.getComment();
      if (!compareObjects(comment1, comment2)) {
        return reportAttributeMismatch("comment", comment1, comment2);
      }
      return true;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Auxiliary Comparator Methods
  protected Proxy getSecondProxy()
  {
    return mSecondProxy;
  }

  protected boolean compareObjects(final Object object, final Object expected)
  {
    if (object == null) {
      return expected == null;
    } else {
      return object.equals(expected);
    }
  }

  protected boolean compareProxies(final Proxy proxy, final Proxy expected)
    throws VisitorException
  {
    if (proxy == null) {
      return expected == null;
    } else if (mIsProvidingDiagnostics && isDiagnosticNode(proxy)) {
      mDiagnosticPath.addLast(proxy);
      mSecondProxy = expected;
      final boolean result = (Boolean) proxy.acceptVisitor(this);
      if (result) {
        mDiagnosticPath.removeLast();
      }
      return result;
    } else {
      mSecondProxy = expected;
      return (Boolean) proxy.acceptVisitor(this);
    }
  }

  protected boolean compareGeometries(final GeometryProxy geo,
                                      final GeometryProxy expected)
    throws VisitorException
  {
    if (mIsRespectingGeometry) {
      return compareProxies(geo, expected);
    } else {
      return true;
    }
  }

  /**
   * Checks whether two collections have the same contents. This method
   * compares two collections of proxies, and checks whether they have elements
   * considered as equal by this visitor.
   */
  protected boolean compareCollections
    (final Collection<? extends Proxy> coll1,
     final Collection<? extends Proxy> coll2)
  {
    final ProxyAccessorCollection<Proxy> map1 =
      new ProxyAccessorHashCollection2<Proxy>(this, coll1);
    final ProxyAccessorCollection<Proxy> map2 =
      new ProxyAccessorHashCollection2<Proxy>(this, coll2);
    for (final Map.Entry<ProxyAccessor<Proxy>,Integer> entry :
         map1.entrySet()) {
      final ProxyAccessor<Proxy> accessor = entry.getKey();
      final Integer count2 = map2.get(accessor);
      if (count2 == null) {
        final Proxy proxy = accessor.getProxy();
        return reportSuperfluousItem(proxy);
      } else if (entry.getValue() != count2.intValue()) {
        final Proxy proxy = accessor.getProxy();
        return reportCountMismatch(proxy, entry.getValue(), count2);
      }
    }
    for (final ProxyAccessor<Proxy> accessor : map2.keySet()) {
      if (!map1.containsKey(accessor)) {
        final Proxy proxy = accessor.getProxy();
        return reportMissingItem(proxy);
      }
    }
    return true;
  }

  /**
   * Checks whether two sets have the same contents. This method compares
   * two sets of proxies, and checks whether they have elements considered
   * as equal by this visitor. This method can compare sets or collections,
   * duplicates are not considered significant in either case.
   */
  protected boolean compareSets
    (final Collection<? extends Proxy> set1,
     final Collection<? extends Proxy> set2)
  {
    // Can't rely on set size as sets my have distinct elements that
    // are equal under content-based equality.
    /*
    final ProxyAccessorMap<Proxy> map1 =
      new ProxyAccessorHashMapByContents<Proxy>(set1);
    final ProxyAccessorMap<Proxy> map2 =
      new ProxyAccessorHashMapByContents<Proxy>(set2);
    return map1.equalsByAccessorEquality(map2);
    */
    return true;
  }

  /**
   * Checks whether two lists have the same contents. This method compares
   * two lists of proxies, and checks whether they have equal elements
   * appearing in the same order. The equality used is defined by this
   * visitor.
   */
  protected boolean compareLists
    (final List<? extends Proxy> list1, final List<? extends Proxy> list2)
    throws VisitorException
  {
    final Iterator<? extends Proxy> iter1 = list1.iterator();
    final Iterator<? extends Proxy> iter2 = list2.iterator();
    while (iter1.hasNext() && iter2.hasNext()) {
      final Proxy proxy1 = iter1.next();
      final Proxy proxy2 = iter2.next();
      if (!compareProxies(proxy1, proxy2)) {
        return false;
      }
    }
    if (iter1.hasNext()) {
      return reportSuperfluousItem(iter1.next());
    } else if (iter2.hasNext()) {
      return reportMissingItem(iter2.next());
    } else {
      return true;
    }
  }


  //#########################################################################
  //# Diagnostics
  protected boolean isDiagnosticNode(final Proxy proxy)
  {
    return proxy instanceof NamedProxy;
  }

  protected boolean reportNameMismatch(final String expected)
  {
    if (mIsProvidingDiagnostics && mDiagnostics == null) {
      mDiagnostics = new NameMismatchDiagnostics(expected);
    }
    return false;
  }

  protected boolean reportAttributeMismatch(final String attrib,
                                            final Object value,
                                            final Object expected)
  {
    if (mIsProvidingDiagnostics && mDiagnostics == null) {
      mDiagnostics = new AttributeMismatchDiagnostics(attrib, value, expected);
    }
    return false;
  }

  protected boolean reportItemMismatch(final Proxy item,
                                       final Proxy expected)
  {
    if (mIsProvidingDiagnostics && mDiagnostics == null) {
      if (mDiagnosticPath.peekLast() == item) {
        mDiagnosticPath.removeLast();
      }
      mDiagnostics = new ItemMismatchDiagnostics(item, expected);
    }
    return false;
  }

  protected boolean reportMissingItem(final Proxy item)
  {
    return false;
  }

  protected boolean reportSuperfluousItem(final Proxy item)
  {
    return false;
  }

  protected boolean reportCountMismatch(final Proxy item,
                                        final int count,
                                        final int expected)
  {
    return false;
  }

  protected void appendDiagnosticPath(final StringBuffer buffer)
  {
    boolean first = true;
    for (final Proxy proxy : mDiagnosticPath) {
      if (first) {
        buffer.append("In ");
        first = false;
      }
      appendProxyDescription(buffer, proxy);
      buffer.append(", ");
    }
  }

  protected void appendProxyDescription(final StringBuffer buffer,
                                        final Proxy proxy)
  {
    appendProxyClassName(buffer, proxy);
    if (proxy instanceof NamedProxy) {
      buffer.append(" '");
      final NamedProxy named = (NamedProxy) proxy;
      final String name = named.getName();
      buffer.append(name);
      buffer.append('\'');
    }
  }

  protected void appendProxyClassName(final StringBuffer buffer,
                                      final Proxy proxy)
  {
    final String clsname = ProxyTools.getShortProxyInterfaceName(proxy);
    buffer.append(clsname);
  }

  protected void appendValue(final StringBuffer buffer, final Object value)
  {
    if (value == null) {
      buffer.append("(null)");
    } else if (value instanceof String) {
      final String text = (String) value;
      buffer.append('\'');
      buffer.append(text);
      buffer.append('\'');
    } else {
      final String text = value.toString();
      buffer.append(text);
    }
  }

  //#########################################################################
  //# Inner Class Diagnostics
  private abstract class Diagnostics {

    //#######################################################################
    //# Message Generation
    abstract void append(final StringBuffer buffer);

  }


  //#########################################################################
  //# Inner Class NameMismatchDiagnostics
  private class NameMismatchDiagnostics extends Diagnostics {

    //#######################################################################
    //# Constructor
    private NameMismatchDiagnostics(final String expected)
    {
      mExpected = expected;
    }

    //#######################################################################
    //# Message Generation
    void append(final StringBuffer buffer)
    {
      buffer.append("the name is not '");
      buffer.append(mExpected);
      buffer.append("' as expected!");
    }

    //#######################################################################
    //# Data Members
    private final String mExpected;

  }


  //#########################################################################
  //# Inner Class AttrbuteMismatchDiagnostics
  private class AttributeMismatchDiagnostics extends Diagnostics {

    //#######################################################################
    //# Constructor
    private AttributeMismatchDiagnostics
      (final String attrib, final Object value, final Object expected)
    {
      mAttrib = attrib;
      mValue = value;
      mExpected = expected;
    }

    //#######################################################################
    //# Message Generation
    void append(final StringBuffer buffer)
    {
      buffer.append("the attribute ");
      buffer.append(mAttrib);
      buffer.append(" has value ");
      appendValue(buffer, mValue);
      buffer.append(", but should be ");
      appendValue(buffer, mExpected);
      buffer.append('!');
    }

    //#######################################################################
    //# Data Members
    private final String mAttrib;
    private final Object mValue;
    private final Object mExpected;

  }


  //#########################################################################
  //# Inner Class ItemMismatchDiagnostics
  private class ItemMismatchDiagnostics extends Diagnostics {

    //#######################################################################
    //# Constructor
    private ItemMismatchDiagnostics(final Proxy item, final Proxy expected)
    {
      mItem = item;
      mExpected = expected;
    }

    //#######################################################################
    //# Message Generation
    void append(final StringBuffer buffer)
    {
      buffer.append("the item ");
      appendProxyDescription(buffer, mItem);
      buffer.append(" does not match the expected ");
      appendProxyDescription(buffer, mExpected);
      buffer.append('!');
    }

    //#######################################################################
    //# Data Members
    private final Proxy mItem;
    private final Proxy mExpected;

  }


  //#########################################################################
  //# Data Members
  private final boolean mIsRespectingGeometry;
  private final boolean mIsProvidingDiagnostics;
  private final Deque<Proxy> mDiagnosticPath;

  private Proxy mSecondProxy;
  private Diagnostics mDiagnostics;

}
