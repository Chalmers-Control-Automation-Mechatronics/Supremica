//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   AbstractEqualityVisitor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.printer.ProxyPrinter;


/**
 * @author Robi Malik
 */

public abstract class AbstractEqualityVisitor
  implements ProxyVisitor
{

  //#########################################################################
  //# Constructors
  public AbstractEqualityVisitor()
  {
    this(false);
  }

  public AbstractEqualityVisitor(final boolean diag)
  {
    this(diag, false);
  }

  public AbstractEqualityVisitor(final boolean diag, final boolean geo)
  {
    mIsProvidingDiagnostics = diag;
    mIsRespectingGeometry = geo;
    mDiagnosticPath = new LinkedList<Proxy>();
  }


  //#########################################################################
  //# Simple Access
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
  public abstract AbstractHashCodeVisitor getHashCodeVisitor();

  public abstract AbstractEqualityVisitor getNonReportingEqualityVisitor();


  //#########################################################################
  //# Invocation
  public boolean equals(final Proxy proxy, final Proxy expected)
  {
    mDiagnostics = null;
    mDiagnosticPath.clear();
    try {
      return compareProxies(proxy, expected);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }

  public String getDiagnostics()
  {
    return getDiagnostics(null);
  }

  public String getDiagnostics(final String prefix)
  {
    try {
      final Writer writer = new StringWriter();
      if (prefix != null) {
        writer.write(prefix);
        writer.write('\n');
      }
      writeDiagnostics(writer);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public void writeDiagnostics(final Writer writer)
    throws IOException
  {
    if (mDiagnostics != null) {
      writeDiagnosticPath(writer);
      mDiagnostics.write(writer);
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
      final NamedProxy expected = (NamedProxy) mSecondProxy;
      final String name1 = proxy.getName();
      final String name2 = expected.getName();
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
      final DocumentProxy expected = (DocumentProxy) mSecondProxy;
      final String comment1 = proxy.getComment();
      final String comment2 = expected.getComment();
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
   * considered as equal by this visitor, occurring the same number of times.
   */
  protected boolean compareCollections
    (final Collection<? extends Proxy> coll,
     final Collection<? extends Proxy> expected)
  {
    final AbstractEqualityVisitor eq = getNonReportingEqualityVisitor();
    final ProxyAccessorCollection<Proxy> map =
      new ProxyAccessorHashCollection2<Proxy>(eq, coll);
    final ProxyAccessorCollection<Proxy> emap =
      new ProxyAccessorHashCollection2<Proxy>(eq, expected);
    for (final Map.Entry<ProxyAccessor<Proxy>,Integer> entry :
      map.entrySet()) {
      final ProxyAccessor<Proxy> accessor = entry.getKey();
      final Integer count2 = emap.get(accessor);
      if (count2 == null) {
        final Proxy proxy = accessor.getProxy();
        return reportSuperfluousItem(proxy);
      } else if (entry.getValue() != count2.intValue()) {
        final Proxy proxy = accessor.getProxy();
        return reportCountMismatch(proxy, entry.getValue(), count2);
      }
    }
    if (map.size() == emap.size()) {
      return true;
    }
    for (final ProxyAccessor<Proxy> accessor : emap.keySet()) {
      if (!map.containsKey(accessor)) {
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
    (final Collection<? extends Proxy> set,
     final Collection<? extends Proxy> expected)
  {
    final AbstractEqualityVisitor eq = getNonReportingEqualityVisitor();
    final ProxyAccessorMap<Proxy> map =
      new ProxyAccessorHashMap2<Proxy>(eq, set);
    final ProxyAccessorMap<Proxy> emap =
      new ProxyAccessorHashMap2<Proxy>(eq, expected);
    for (final ProxyAccessor<Proxy> eaccessor : emap.keySet()) {
      if (!map.containsKey(eaccessor)) {
        final Proxy proxy = eaccessor.getProxy();
        return reportMissingItem(proxy);
      }
    }
    if (map.size() == emap.size()) {
      return true;
    }
    for (final ProxyAccessor<Proxy> accessor : map.keySet()) {
      if (!emap.containsKey(accessor)) {
        final Proxy proxy = accessor.getProxy();
        return reportSuperfluousItem(proxy);
      }
    }
    return true;
  }

  /**
   * Checks whether two lists have the same contents. This method compares
   * two lists of proxies, and checks whether they have equal elements
   * appearing in the same order. The equality used is defined by this
   * visitor.
   */
  protected boolean compareLists(final List<? extends Proxy> list,
                                 final List<? extends Proxy> expected)
    throws VisitorException
  {
    final Iterator<? extends Proxy> iter1 = list.iterator();
    final Iterator<? extends Proxy> iter2 = expected.iterator();
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

  /**
   * Checks whether two attribute maps are equal. This method compares
   * two maps that map strings to strings, using standard string equality.
   * Discrepancies are reported as attribute mismatch.
   */
  protected boolean compareAttributeMaps(final Map<String,String> attribs,
                                         final Map<String,String> expected)
  {
    for (final Map.Entry<String,String> entry : expected.entrySet()) {
      final String attrib = entry.getKey();
      final String value2 = entry.getValue();
      final String value1 = attribs.get(attrib);
      if (!value1.equals(value2)) {
        return reportAttributeMismatch(attrib, value1, value2);
      }
    }
    if (attribs.size() == expected.size()) {
      return true;
    }
    for (final Map.Entry<String,String> entry : attribs.entrySet()) {
      final String attrib = entry.getKey();
      if (!expected.containsKey(attrib)) {
        final String value = expected.get(attrib);
        return reportAttributeMismatch(attrib, value, null);
      }
    }
    // Should never get here ...
    return false;
  }

  /**
   * Checks whether two collections of references have the same contents. This
   * method compares two collections of proxies, and checks whether they have
   * elements with the same names, occurring the same number of times.
   */
  protected boolean compareRefCollections
      (final Collection<? extends NamedProxy> coll,
       final Collection<? extends NamedProxy> expected)
  {
    final Map<String,Integer> names = createNameCollection(coll);
    final Map<String,Integer> enames = createNameCollection(expected);
    if (names.equals(enames)) {
      return true;
    }
    for (final NamedProxy proxy : expected) {
      final String name = proxy.getName();
      final Integer count = names.get(name);
      if (count == null) {
        return reportMissingItem(proxy);
      }
      final int ecount = enames.get(name);
      if (count != ecount) {
        reportCountMismatch(proxy, count, ecount);
      }
    }
    for (final NamedProxy proxy : coll) {
      final String name = proxy.getName();
      if (!enames.containsKey(name)) {
        return reportSuperfluousItem(proxy);
      }
    }
    // Should never get here ...
    return false;
  }

  /**
   * Checks whether two sets of references have the same contents. This method
   * compares two sets of named proxies, and checks whether they have elements
   * with equal names. This method can compare sets or collections, duplicates
   * are not considered significant in either case.
   */
  protected boolean compareRefSets
      (final Collection<? extends NamedProxy> set,
       final Collection<? extends NamedProxy> expected)
  {
    final Set<String> names = createNameSet(set);
    for (final NamedProxy proxy : expected) {
      final String name = proxy.getName();
      if (!names.contains(name)) {
        return reportMissingItem(proxy);
      }
    }
    if (set instanceof Set<?> &&
        expected instanceof Set<?> &&
        set.size() == expected.size()) {
      return true;
    }
    final Set<String> enames = createNameSet(expected);
    if (names.size() == enames.size()) {
      return true;
    }
    for (final NamedProxy proxy : set) {
      final String name = proxy.getName();
      if (!enames.contains(name)) {
        return reportSuperfluousItem(proxy);
      }
    }
    // Should never get here ...
    return false;
  }

  /**
   * Checks whether two sets of references have the same contents. This method
   * compares two sets of named proxies, and checks whether they have elements
   * with equal names. This method can compare sets or collections, duplicates
   * are not considered significant in either case.
   */
  protected boolean compareRefMaps
      (final Map<? extends NamedProxy,? extends NamedProxy> map,
       final Map<? extends NamedProxy,? extends NamedProxy> expected)
  {
    final Map<String,String> names = createNameMap(map);
    for (final Map.Entry<? extends NamedProxy,? extends NamedProxy> entry :
         expected.entrySet()) {
      final NamedProxy ekey = entry.getKey();
      final String ekeyname = ekey.getName();
      final String valuename = names.get(ekeyname);
      if (valuename == null) {
        return reportMissingItem(ekey);
      }
      final NamedProxy evalue = entry.getValue();
      final String evaluename = evalue.getName();
      if (!valuename.equals(evaluename)) {
        return reportMapMismatch(ekey, valuename, evaluename);
      }
    }
    if (map.size() == expected.size()) {
      return true;
    }
    final Set<String> enames = createNameSet(expected.keySet());
    for (final NamedProxy key : map.keySet()) {
      final String keyname = key.getName();
      if (!enames.contains(keyname)) {
        return reportSuperfluousItem(key);
      }
    }
    // Should never get here ...
    return false;
  }


  //#########################################################################
  //# Storing Diagnostics
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
    if (mIsProvidingDiagnostics && mDiagnostics == null) {
      mDiagnostics = new MissingItemDiagnostics(item);
    }
    return false;
  }

  protected boolean reportSuperfluousItem(final Proxy item)
  {
    if (mIsProvidingDiagnostics && mDiagnostics == null) {
      mDiagnostics = new SuperfluousItemDiagnostics(item);
    }
    return false;
  }

  protected boolean reportCountMismatch(final Proxy item,
                                        final int count,
                                        final int expected)
  {
    if (mIsProvidingDiagnostics && mDiagnostics == null) {
      mDiagnostics = new CountMismatchDiagnostics(item, count, expected);
    }
    return false;
  }

  protected boolean reportMapMismatch(final NamedProxy key,
                                      final String valuename,
                                      final String expectedname)
  {
    if (mIsProvidingDiagnostics && mDiagnostics == null) {
      mDiagnostics = new MapMismatchDiagnostics(key, valuename, expectedname);
    }
    return false;
  }


  //#########################################################################
  //# Printing Diagnostics
  protected void writeDiagnosticPath(final Writer writer)
    throws IOException
  {
    boolean first = true;
    for (final Proxy proxy : mDiagnosticPath) {
      if (first) {
        writer.write("In ");
        first = false;
      }
      writeProxyDescription(writer, proxy);
      writer.write(", ");
    }
  }

  protected void writeProxyDescription(final Writer writer, final Proxy proxy)
    throws IOException
  {
    writeProxyClassName(writer, proxy);
    if (proxy instanceof NamedProxy) {
      writer.write(" '");
      final NamedProxy named = (NamedProxy) proxy;
      final String name = named.getName();
      writer.write(name);
      writer.write('\'');
    }
  }

  protected void writeProxyClassName(final Writer writer, final Proxy proxy)
    throws IOException
  {
    final String clsname = ProxyTools.getShortProxyInterfaceName(proxy);
    writer.write(clsname);
  }

  protected void writeValue(final Writer writer, final Object value)
    throws IOException
  {
    if (value == null) {
      writer.write("(null)");
    } else if (value instanceof String) {
      final String text = (String) value;
      writer.write('\'');
      writer.write(text);
      writer.write('\'');
    } else {
      final String text = value.toString();
      writer.write(text);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private Map<String,Integer> createNameCollection
      (final Collection<? extends NamedProxy> proxies)
  {
    final int size = proxies.size();
    final Map<String,Integer> map = new HashMap<String,Integer>(size);
    for (final NamedProxy proxy : proxies) {
      final String name = proxy.getName();
      final Integer count = map.get(name);
      final int newcount = count == null ? 1 : count + 1;
      map.put(name, newcount);
    }
    return map;
  }

  private Set<String> createNameSet
      (final Collection<? extends NamedProxy> proxies)
  {
    final int size = proxies.size();
    final Set<String> names = new HashSet<String>(size);
    for (final NamedProxy proxy : proxies) {
      final String name = proxy.getName();
      names.add(name);
    }
    return names;
  }

  private Map<String,String> createNameMap
      (final Map<? extends NamedProxy,? extends NamedProxy> proxies)
  {
    final int size = proxies.size();
    final Map<String,String> map = new HashMap<String,String>(size);
    for (final Map.Entry<? extends NamedProxy,? extends NamedProxy> entry :
         proxies.entrySet()) {
      final String key = entry.getKey().getName();
      final String value = entry.getValue().getName();
      map.put(key, value);
    }
    return map;
  }


  //#########################################################################
  //# Inner Class Diagnostics
  private abstract class Diagnostics {

    //#######################################################################
    //# Message Generation
    abstract void write(final Writer writer) throws IOException;

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
    void write(final Writer writer)
      throws IOException
    {
      writer.write("the name is not '");
      writer.write(mExpected);
      writer.write("' as expected!");
    }

    //#######################################################################
    //# Data Members
    private final String mExpected;

  }


  //#########################################################################
  //# Inner Class AttributeMismatchDiagnostics
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
    void write(final Writer writer)
      throws IOException
    {
      writer.write("the attribute ");
      writer.write(mAttrib);
      writer.write(" has value ");
      writeValue(writer, mValue);
      writer.write(", but should be ");
      writeValue(writer, mExpected);
      writer.write('!');
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
    void write(final Writer writer)
      throws IOException
    {
      writer.write("the ");
      writeProxyDescription(writer, mItem);
      writer.write(" does not match the expected ");
      writeProxyDescription(writer, mExpected);
      writer.write("!\nFOUND:\n");
      ProxyPrinter.printProxy(writer, mItem);
      writer.write("\nEXPECTED:\n");
      ProxyPrinter.printProxy(writer, mExpected);
    }

    //#######################################################################
    //# Data Members
    private final Proxy mItem;
    private final Proxy mExpected;

  }


  //#########################################################################
  //# Inner Class MissingItemDiagnostics
  private class MissingItemDiagnostics extends Diagnostics {

    //#######################################################################
    //# Constructor
    private MissingItemDiagnostics(final Proxy item)
    {
      mItem = item;
    }

    //#######################################################################
    //# Message Generation
    void write(final Writer writer)
      throws IOException
    {
      writer.write("the expected ");
      writeProxyDescription(writer, mItem);
      writer.write(" was not found!\nEXPECTED:\n");
      ProxyPrinter.printProxy(writer, mItem);
    }

    //#######################################################################
    //# Data Members
    private final Proxy mItem;

  }


  //#########################################################################
  //# Inner Class SuperfluousItemDiagnostics
  private class SuperfluousItemDiagnostics extends Diagnostics {

    //#######################################################################
    //# Constructor
    private SuperfluousItemDiagnostics(final Proxy item)
    {
      mItem = item;
    }

    //#######################################################################
    //# Message Generation
    void write(final Writer writer)
      throws IOException
    {
      writer.write("found an unexpected ");
      writeProxyDescription(writer, mItem);
      writer.write("!\nFOUND:\n");
      ProxyPrinter.printProxy(writer, mItem);
    }

    //#######################################################################
    //# Data Members
    private final Proxy mItem;

  }


  //#########################################################################
  //# Inner Class CountMismatchDiagnostics
  private class CountMismatchDiagnostics extends Diagnostics {

    //#######################################################################
    //# Constructor
    private CountMismatchDiagnostics
      (final Proxy item, final int count, final int expected)
    {
      mItem = item;
      mCount = count;
      mExpected = expected;
    }

    //#######################################################################
    //# Message Generation
    void write(final Writer writer)
      throws IOException
    {
      writer.write("the ");
      writeProxyDescription(writer, mItem);
      writer.write(" occurs an unexpected number of times: found ");
      writer.write(mCount);
      writer.write(" occurrences, but expected ");
      writer.write(mExpected);
      writer.write("!\nITEM:\n");
      ProxyPrinter.printProxy(writer, mItem);
    }

    //#######################################################################
    //# Data Members
    private final Proxy mItem;
    private final int mCount;
    private final int mExpected;

  }


  //#########################################################################
  //# Inner Class MapMismatchDiagnostics
  private class MapMismatchDiagnostics extends Diagnostics {

    //#######################################################################
    //# Constructor
    private MapMismatchDiagnostics(final NamedProxy key,
                                   final String valuename,
                                   final String expectedname)
    {
      mKey = key;
      mValueName = valuename;
      mExpectedName = expectedname;
    }

    //#######################################################################
    //# Message Generation
    void write(final Writer writer)
      throws IOException
    {
      writer.write("the map entry for ");
      writeProxyDescription(writer, mKey);
      writer.write(" has the name '");
      writer.write(mValueName);
      writer.write("', but it should be '");
      writer.write(mExpectedName);
      writer.write("'!");
    }

    //#######################################################################
    //# Data Members
    private final NamedProxy mKey;
    private final String mValueName;
    private final String mExpectedName;

  }


  //#########################################################################
  //# Data Members
  private final boolean mIsRespectingGeometry;
  private final boolean mIsProvidingDiagnostics;
  private final Deque<Proxy> mDiagnosticPath;

  private Proxy mSecondProxy;
  private Diagnostics mDiagnostics;

}
