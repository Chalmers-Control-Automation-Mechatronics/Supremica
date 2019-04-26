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

package net.sourceforge.waters.plain.des;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.base.AbstractEqualityVisitor;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESEqualityVisitor;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.xsd.base.EventKind;


public class PlainProductDESEqualityTest extends TestCase
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(PlainProductDESEqualityTest.class);
  }

  public static void main(final String args[])
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp() throws Exception
  {
    super.setUp();
    mFactory = ProductDESElementFactory.getInstance();
    mEquality = ProductDESEqualityVisitor.getInstance();
  }


  //#########################################################################
  //# Test Cases
  public void testEmptyEquals()
  {
    final Set<EventProxy> empty1 = Collections.emptySet();
    assertTrue(mEquality.isEqualSet(empty1, empty1));
    final Set<EventProxy> empty2 = new HashSet<EventProxy>();
    assertTrue(mEquality.isEqualSet(empty1, empty2));
    assertTrue(mEquality.isEqualSet(empty2, empty1));
    final Collection<EventProxy> empty3 = new LinkedList<EventProxy>();
    assertTrue(mEquality.isEqualCollection(empty1, empty3));
    assertTrue(mEquality.isEqualCollection(empty3, empty1));
    assertTrue(mEquality.isEqualCollection(empty2, empty3));
    assertTrue(mEquality.isEqualCollection(empty3, empty2));
  }

  public void testSingletonEquals()
  {
    final String TEXT = "testSingletonEquals";
    final EventProxy item1 = createEventProxy(TEXT);
    final EventProxy item2 = createEventProxy(TEXT);
    final EventProxy item3 = createEventProxy(TEXT);
    final Set<EventProxy> single1 = Collections.singleton(item1);
    assertTrue(mEquality.isEqualSet(single1, single1));
    final Set<EventProxy> single2 = new HashSet<EventProxy>();
    single2.add(item2);
    assertTrue(mEquality.isEqualSet(single1, single2));
    assertTrue(mEquality.isEqualSet(single2, single1));
    final Collection<EventProxy> single3 = new LinkedList<EventProxy>();
    single3.add(item3);
    assertTrue(mEquality.isEqualCollection(single1, single3));
    assertTrue(mEquality.isEqualCollection(single3, single1));
    assertTrue(mEquality.isEqualCollection(single2, single3));
    assertTrue(mEquality.isEqualCollection(single3, single2));
  }

  public void testTwoElementsCollectionEquals()
  {
    final EventProxy item1 = createEventProxy("didel");
    final EventProxy item2 = createEventProxy("dum");
    final Collection<EventProxy> list1 = new LinkedList<EventProxy>();
    list1.add(item1);
    list1.add(item2);
    final Collection<EventProxy> list2 = new LinkedList<EventProxy>();
    list2.add(item2);
    list2.add(item1);
    assertTrue(mEquality.isEqualCollection(list1, list1));
    assertTrue(mEquality.isEqualCollection(list1, list2));
    assertTrue(mEquality.isEqualCollection(list2, list1));
    assertTrue(mEquality.isEqualCollection(list2, list2));
  }

  public void testTwoElementsSetEquals()
  {
    final EventProxy item1 = createEventProxy("didel");
    final EventProxy item2 = createEventProxy("dum");
    final Set<EventProxy> set1 = new HashSet<EventProxy>();
    set1.add(item1);
    set1.add(item2);
    final Set<EventProxy> set2 = new HashSet<EventProxy>();
    set2.add(item2);
    set2.add(item1);
    assertTrue(mEquality.isEqualSet(set1, set1));
    assertTrue(mEquality.isEqualSet(set1, set2));
    assertTrue(mEquality.isEqualSet(set2, set1));
    assertTrue(mEquality.isEqualSet(set2, set2));
  }

  public void testDoubleUps()
  {
    final EventProxy item1 = createEventProxy("didel");
    final Collection<EventProxy> list1 = new HashSet<EventProxy>();
    list1.add(item1);
    final Collection<EventProxy> list2 = new LinkedList<EventProxy>();
    list2.add(item1);
    list2.add(item1);
    assertTrue(mEquality.isEqualCollection(list1, list1));
    assertFalse(mEquality.isEqualCollection(list1, list2));
    assertFalse(mEquality.isEqualCollection(list2, list1));
    assertTrue(mEquality.isEqualCollection(list2, list2));
    final EventProxy item2 = createEventProxy("dum");
    list1.add(item2);
    list2.add(item2);
    assertTrue(mEquality.isEqualCollection(list1, list1));
    assertFalse(mEquality.isEqualCollection(list1, list2));
    assertFalse(mEquality.isEqualCollection(list2, list1));
    assertTrue(mEquality.isEqualCollection(list2, list2));
  }

  public void testCollectionWithNullEquals()
  {
    final EventProxy item = createEventProxy("didel");
    final Collection<EventProxy> list1 = new LinkedList<EventProxy>();
    list1.add(item);
    list1.add(null);
    final Collection<EventProxy> list2 = new LinkedList<EventProxy>();
    list2.add(item);
    list2.add(null);
    assertTrue(mEquality.isEqualCollection(list1, list1));
    assertTrue(mEquality.isEqualCollection(list1, list2));
    assertTrue(mEquality.isEqualCollection(list2, list1));
    assertTrue(mEquality.isEqualCollection(list2, list2));
  }

  public void testSetWithNullEquals()
  {
    final EventProxy item = createEventProxy("didel");
    final Set<EventProxy> set1 = new HashSet<EventProxy>();
    set1.add(item);
    set1.add(null);
    final Set<EventProxy> set2 = new HashSet<EventProxy>();
    set2.add(item);
    set2.add(null);
    assertTrue(mEquality.isEqualSet(set1, set1));
    assertTrue(mEquality.isEqualSet(set1, set2));
    assertTrue(mEquality.isEqualSet(set2, set1));
    assertTrue(mEquality.isEqualSet(set2, set2));
  }

  public void testCollectionNotEquals()
  {
    final EventProxy item1 = createEventProxy("ei");
    final EventProxy item2 = createEventProxy("didel");
    final EventProxy item3 = createEventProxy("dum");
    final Collection<EventProxy> list1 = new LinkedList<EventProxy>();
    list1.add(item1);
    list1.add(item2);
    final Collection<EventProxy> list2 = new LinkedList<EventProxy>();
    list2.add(item2);
    list2.add(null);
    final Collection<EventProxy> list3 = new LinkedList<EventProxy>();
    list3.add(item1);
    list3.add(item2);
    list3.add(item3);
    assertFalse(mEquality.isEqualCollection(list1, list2));
    assertFalse(mEquality.isEqualCollection(list2, list1));
    assertFalse(mEquality.isEqualCollection(list1, list3));
    assertFalse(mEquality.isEqualCollection(list3, list1));
    assertFalse(mEquality.isEqualCollection(list2, list3));
    assertFalse(mEquality.isEqualCollection(list3, list2));
  }

  public void testSetNotEquals()
  {
    final EventProxy item1 = createEventProxy("ei");
    final EventProxy item2 = createEventProxy("didel");
    final EventProxy item3 = createEventProxy("dum");
    final Set<EventProxy> set1 = new HashSet<EventProxy>();
    set1.add(item1);
    set1.add(item2);
    final Set<EventProxy> set2 = new HashSet<EventProxy>();
    set2.add(item2);
    set2.add(item3);
    final Set<EventProxy> set3 = new HashSet<EventProxy>();
    set3.add(item1);
    set3.add(item2);
    set3.add(item3);
    assertFalse(mEquality.isEqualSet(set1, set2));
    assertFalse(mEquality.isEqualSet(set2, set1));
    assertFalse(mEquality.isEqualSet(set1, set3));
    assertFalse(mEquality.isEqualSet(set3, set1));
    assertFalse(mEquality.isEqualSet(set2, set3));
    assertFalse(mEquality.isEqualSet(set3, set2));
  }

  public void testCollectionWithNullNotEquals()
  {
    final EventProxy item = createEventProxy("ohhh");
    final Collection<EventProxy> empty = Collections.emptyList();
    final Collection<EventProxy> list1 = Collections.singletonList(item);
    final Collection<EventProxy> list2 = new LinkedList<EventProxy>();
    list2.add(null);
    assertFalse(mEquality.isEqualCollection(empty, list2));
    assertFalse(mEquality.isEqualCollection(list2, empty));
    assertFalse(mEquality.isEqualCollection(list1, list2));
    assertFalse(mEquality.isEqualCollection(list2, list1));
  }

  public void testSetWithNullNotEquals()
  {
    final EventProxy item = createEventProxy("ohhh");
    final Set<EventProxy> empty = Collections.emptySet();
    final Set<EventProxy> set1 = Collections.singleton(item);
    final Set<EventProxy> set2 = new HashSet<EventProxy>();
    set2.add(null);
    assertFalse(mEquality.isEqualSet(empty, set2));
    assertFalse(mEquality.isEqualSet(set2, empty));
    assertFalse(mEquality.isEqualSet(set1, set2));
    assertFalse(mEquality.isEqualSet(set2, set1));
  }


  //#########################################################################
  //# Auxiliary Methods
  private EventProxy createEventProxy(final String name)
  {
    return mFactory.createEventProxy(name, EventKind.CONTROLLABLE);
  }


  //#########################################################################
  //# Data Members
  private ProductDESProxyFactory mFactory;
  private AbstractEqualityVisitor mEquality;

}
