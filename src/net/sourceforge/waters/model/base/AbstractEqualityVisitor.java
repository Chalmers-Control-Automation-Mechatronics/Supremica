//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.model.base;

import gnu.trove.strategy.HashingStrategy;

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
 * <P>A visitor to compare {@link Proxy} objects for content-based
 * equality.</P>
 *
 * <P>All {@link Proxy} objects implement default object equality.
 * The AbstractEqualityVisitor provides for content-based equality.
 * It can be configured to respect or not to respect geometry information
 * found in some {@link Proxy} objects.</P>
 *
 * <P>In addition, the AbstractEqualityVisitor can optionally produce
 * detailed diagnostic information when two items are found to be not
 * equal. This is useful for testing and debugging, but it does have
 * an impact on performance.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractEqualityVisitor
  implements ProxyVisitor
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new equality checker.
   * @param  diag      A flag, indicating whether the equality checker should
   *                   provide diagnostic information if items compared are
   *                   found not to be equal. Diagnostic information can be
   *                   retrieved using {@link #getDiagnostics()}.
   *                   This is useful for testing and debugging, but it does
   *                   have an impact on performance.
   * @param  geo       A flag, indicating whether the equality checker should
   *                   consider geometry information. If <CODE>true</CODE>
   *                   objects will be considered equal if their contents and
   *                   geometry are equal, otherwise any geometry information
   *                   will be ignored when checking for equality.
   */
  public AbstractEqualityVisitor(final boolean diag, final boolean geo)
  {
    mIsProvidingDiagnostics = diag;
    mIsRespectingGeometry = geo;
    mDiagnosticPath = new LinkedList<Proxy>();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Returns whether this equality checker respects geometry information.
   */
  public boolean isRespectingGeometry()
  {
    return mIsRespectingGeometry;
  }

  /**
   * Returns whether this equality checker provides diagnostic information.
   */
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
  /**
   * Compares two proxy objects for equality.
   * @param  proxy     The first object to be compared.
   * @param  expected  The second object to be compared. For the purpose
   *                   of diagnostic information, the second argument will
   *                   be referred to as the 'expected' value.
   * @return <CODE>true</CODE> if the two objects were found equal according
   *         to the parameterisation of this equality checker,
   *         <CODE>false</CODE> otherwise.
   */
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

  /**
   * Checks whether two collections have the same contents. This method
   * compares two collections of proxies, and checks whether they have elements
   * considered as equal by this visitor, occurring the same number of times.
   * @param  coll      The first collection to be compared.
   * @param  expected  The second collection to be compared. For the purpose
   *                   of diagnostic information, the second argument will
   *                   be referred to as the 'expected' value.
   * @return <CODE>true</CODE> if the proxies in the two collection were found
   *         equal according to the parameterisation of this equality checker,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isEqualCollection(final Collection<? extends Proxy> coll,
                                   final Collection<? extends Proxy> expected)
  {
    mDiagnostics = null;
    mDiagnosticPath.clear();
    return compareCollections(coll, expected);
  }

  /**
   * Checks whether two sets have the same contents. This method compares
   * two sets of proxies, and checks whether they have elements considered
   * as equal by this visitor. This method can compare sets or collections,
   * duplicates are not considered significant in either case.
   * @param  set       The first set to be compared.
   * @param  expected  The second set to be compared. For the purpose
   *                   of diagnostic information, the second argument will
   *                   be referred to as the 'expected' value.
   * @return <CODE>true</CODE> if the proxies in the two sets were found
   *         equal according to the parameterisation of this equality checker,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isEqualSet(final Collection<? extends Proxy> set,
                            final Collection<? extends Proxy> expected)
  {
    mDiagnostics = null;
    mDiagnosticPath.clear();
    return compareSets(set, expected);
  }

  /**
   * Compares two lists of proxies for equality.
   * @param  list      The first list to be compared.
   * @param  expected  The second list to be compared. For the purpose
   *                   of diagnostic information, the second argument will
   *                   be referred to as the 'expected' value.
   * @return <CODE>true</CODE> if the proxies in the two lists were found
   *         equal according to the parameterisation of this equality checker,
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isEqualList(final List<? extends Proxy> list,
                             final List<? extends Proxy> expected)
  {
    mDiagnostics = null;
    mDiagnosticPath.clear();
    try {
      return compareLists(list, expected);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }

  /**
   * Checks whether the given collection contains an item equal to the
   * given element, using the equality defined by this visitor.
   */
  public boolean contains(final Collection<? extends Proxy> collection,
                          final Proxy element)
  {
    for (final Proxy current : collection) {
      if (equals(element, current)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a hashing strategy for use with GNU Trove,
   * using the equality defined by this visitor.
   * This equality generally is intended for content-based comparison
   * of structures and avoids object identity when comparing references.
   * @see net.sourceforge.waters.model.des.DeterministicTransitionHashingStrategy DeterministicTransitionHashingStrategy
   * @see net.sourceforge.waters.model.des.NonDeterministicTransitionHashingStrategy NonDeterministicTransitionHashingStrategy
   */
  public <P extends Proxy> HashingStrategy<P> getTObjectHashingStrategy()
  {
    return new ProxyHashingStrategy<P>();
  }


  //#########################################################################
  //# Diagnostics
  /**
   * Gets diagnostics information.
   * @return A string explaining why the last equality test performed by
   *         this equality checker produced a <CODE>false</CODE> result.
   * @throws IllegalStateException to indicate that the last comparison gave
   *                   a <CODE>true</CODE> result, or the equality checker is
   *                   not configured to provide diagnostic information.
   */
  public String getDiagnostics()
  {
    return getDiagnostics(null);
  }

  /**
   * Gets diagnostics information.
   * @param  prefix    A string to be prepended to the diagnostic message.
   * @return A string explaining why the last equality test performed by
   *         this equality checker produced a <CODE>false</CODE> result.
   * @throws IllegalStateException to indicate that the last comparison gave
   *                   a <CODE>true</CODE> result, or the equality checker is
   *                   not configured to provide diagnostic information.
   */
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

  /**
   * Writes diagnostics information.
   * This method writes a string explaining why the last equality test
   * performed by this equality checker produced a <CODE>false</CODE> result.
   * The message should explain in detail which attributes of the two items
   * differ and how they differ.
   * @param  writer    A stream for writing output to.
   * @throws IOException to indicate a failure when writing to the output
   *                   stream.
   * @throws IllegalStateException to indicate that the last comparison gave
   *                   a <CODE>true</CODE> result, or the equality checker is
   *                   not configured to provide diagnostic information.
   */
  public void writeDiagnostics(final Writer writer)
    throws IOException
  {
    if (mDiagnostics != null) {
      writeDiagnosticPath(writer);
      mDiagnostics.write(writer);
    } else if (mIsProvidingDiagnostics) {
      throw new IllegalStateException
          ("No previous false comparison found, diagnostics not available!");
    } else {
      throw new IllegalStateException
          ("Not configured to produce diagnostics!");
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.base.ProxyVisitor
  @Override
  public Boolean visitProxy(final Proxy proxy)
  {
    if (proxy.getProxyInterface() == mSecondProxy.getProxyInterface()) {
      return true;
    } else {
      return reportItemMismatch(proxy, mSecondProxy);
    }
  }

  @Override
  public Boolean visitGeometryProxy(final GeometryProxy proxy)
  {
    return visitProxy(proxy);
  }

  @Override
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

  @Override
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

  protected void setSecondProxy(final Proxy expected)
  {
    mSecondProxy = expected;
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
    } else if (expected == null) {
      return false;
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

  protected boolean compareReferences(final NamedProxy proxy,
                                      final NamedProxy expected)
  {
    if (proxy == null) {
      return expected == null;
    } else {
      return proxy.refequals(expected);
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
      new ProxyAccessorHashCollection<Proxy>(eq, coll);
    final ProxyAccessorCollection<Proxy> emap =
      new ProxyAccessorHashCollection<Proxy>(eq, expected);
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
    final ProxyAccessorSet<Proxy> map =
      new ProxyAccessorHashSet<Proxy>(eq, set);
    final ProxyAccessorSet<Proxy> emap =
      new ProxyAccessorHashSet<Proxy>(eq, expected);
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
   * Checks whether two sets of named proxies have the same contents. This
   * method compares two sets of proxies, and checks whether they have elements
   * considered as equal by this visitor. Names are used to match elements of
   * the two sets and produce more detailed diagnostics if differences are
   * found. This method can compare sets or collections, provided they do not
   * contain duplicate names.
   * @throws DuplicateNameException
   *           to indicate that one of the input sets contains more than one
   *           entry with the same name.
   */
  protected <P extends NamedProxy> boolean compareNamedSets
    (final Collection<P> set, final Collection<P> expected)
    throws VisitorException
  {
    //final Proxy container = mDiagnosticPath.peekLast();
    final IndexedSet<P> map = new DiagnosticArraySet<P>(set);
    for (final P eproxy : expected) {
      final String name = eproxy.getName();
      final P proxy = map.get(name);
      if (proxy == null) {
        return reportMissingItem(eproxy);
      } else if (!compareProxies(proxy, eproxy)) {
        return false;
      }
    }
    if (map.size() == expected.size()) {
      return true;
    }
    final IndexedSet<P> emap = new DiagnosticArraySet<P>(expected);
    for (final P proxy : set) {
      final String name = proxy.getName();
      if (!emap.containsName(name)) {
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
      if (!compareObjects(value1, value2)) {
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
  //# Inner Class ProxyHashingStrategy
  private class ProxyHashingStrategy<P extends Proxy>
    implements HashingStrategy<P>
  {

    //#######################################################################
    //# Interface gnu.trove.TObjectHashingStrategy<Proxy>
     @Override
    public int computeHashCode(final P proxy)
    {
      final AbstractHashCodeVisitor visitor = getHashCodeVisitor();
      return visitor.hashCode(proxy);
    }

    @Override
    public boolean equals(final P proxy0, final P proxy1)
    {
      return AbstractEqualityVisitor.this.equals(proxy0, proxy1);
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

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
    @Override
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
    @Override
    void write(final Writer writer)
      throws IOException
    {
      writer.write("the attribute '");
      writer.write(mAttrib);
      writer.write("' has value ");
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
  //# Inner Class DiagnosticArraySet
  private class DiagnosticArraySet<P extends NamedProxy>
    extends IndexedArraySet<P>
  {

    //#######################################################################
    //# Constructor
    private DiagnosticArraySet(final Collection<? extends P> collection)
    {
      super(collection);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.water.model.base.IndexedArraySet
    @Override
    protected void appendContainerName(final StringBuilder buffer)
    {
      final Proxy container = mDiagnosticPath.peekLast();
      ProxyTools.appendContainerName(container, buffer);
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Data Members
  private final boolean mIsRespectingGeometry;
  private final boolean mIsProvidingDiagnostics;
  private final Deque<Proxy> mDiagnosticPath;

  private Proxy mSecondProxy;
  private Diagnostics mDiagnostics;

}








