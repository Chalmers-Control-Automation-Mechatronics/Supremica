//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   EqualCollectionTest
//###########################################################################
//# $Id: EqualCollectionTest.java,v 1.2 2006-07-20 02:28:38 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class EqualCollectionTest extends TestCase
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(EqualCollectionTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testEmptyEquals()
  {
    final Set<StringProxy> empty1 = Collections.emptySet();
    assertTrue(EqualCollection.isEqualSetByContents(empty1, empty1));
    final Set<StringProxy> empty2 = new HashSet<StringProxy>();
    assertTrue(EqualCollection.isEqualSetByContents(empty1, empty2));
    assertTrue(EqualCollection.isEqualSetByContents(empty2, empty1));
    final Collection<StringProxy> empty3 = new LinkedList<StringProxy>();
    assertTrue(EqualCollection.isEqualSetByContents(empty1, empty3));
    assertTrue(EqualCollection.isEqualSetByContents(empty3, empty1));
    assertTrue(EqualCollection.isEqualSetByContents(empty2, empty3));
    assertTrue(EqualCollection.isEqualSetByContents(empty3, empty2));
  }

  public void testSingletonEquals()
  {
    final String TEXT = "testSingletonEquals";
    final StringProxy item1 = new StringProxy(TEXT);
    final StringProxy item2 = new StringProxy(TEXT);
    final StringProxy item3 = new StringProxy(TEXT);
    final Set<StringProxy> single1 = Collections.singleton(item1);
    assertTrue(EqualCollection.isEqualSetByContents(single1, single1));
    final Set<StringProxy> single2 = new HashSet<StringProxy>();
    single2.add(item2);
    assertTrue(EqualCollection.isEqualSetByContents(single1, single2));
    assertTrue(EqualCollection.isEqualSetByContents(single2, single1));
    final Collection<StringProxy> single3 = new LinkedList<StringProxy>();
    single3.add(item3);
    assertTrue(EqualCollection.isEqualSetByContents(single1, single3));
    assertTrue(EqualCollection.isEqualSetByContents(single3, single1));
    assertTrue(EqualCollection.isEqualSetByContents(single2, single3));
    assertTrue(EqualCollection.isEqualSetByContents(single3, single2));
  }

  public void testTwoElementsEquals()
  {
    final StringProxy item1 = new StringProxy("didel");
    final StringProxy item2 = new StringProxy("dum");
    final Collection<StringProxy> list1 = new LinkedList<StringProxy>();
    list1.add(item1);
    list1.add(item2);
    final Collection<StringProxy> list2 = new LinkedList<StringProxy>();
    list2.add(item2);
    list2.add(item1);
    assertTrue(EqualCollection.isEqualSetByContents(list1, list1));
    assertTrue(EqualCollection.isEqualSetByContents(list1, list2));
    assertTrue(EqualCollection.isEqualSetByContents(list2, list1));
    assertTrue(EqualCollection.isEqualSetByContents(list2, list2));
  }

  public void testNotEquals()
  {
    final StringProxy item1 = new StringProxy("ei");
    final StringProxy item2 = new StringProxy("didel");
    final StringProxy item3 = new StringProxy("dum");
    final Collection<StringProxy> list1 = new LinkedList<StringProxy>();
    list1.add(item1);
    list1.add(item2);
    final Collection<StringProxy> list2 = new LinkedList<StringProxy>();
    list2.add(item2);
    list2.add(item3);
    final Collection<StringProxy> list3 = new LinkedList<StringProxy>();
    list3.add(item1);
    list3.add(item2);
    list3.add(item3);
    assertFalse(EqualCollection.isEqualSetByContents(list1, list2));
    assertFalse(EqualCollection.isEqualSetByContents(list2, list1));
    assertFalse(EqualCollection.isEqualSetByContents(list1, list3));
    assertFalse(EqualCollection.isEqualSetByContents(list3, list1));
    assertFalse(EqualCollection.isEqualSetByContents(list2, list3));
    assertFalse(EqualCollection.isEqualSetByContents(list3, list2));
  }


  //#########################################################################
  //# Inner Class StringProxy
  private static class StringProxy implements Proxy, Cloneable {

    //#######################################################################
    //# Constructors
    private StringProxy(final String string)
    {
      mString = string;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.Proxy
    public StringProxy clone()
    {
      try {
	return (StringProxy) super.clone();
      } catch (final CloneNotSupportedException exception) {
	throw new WatersRuntimeException(exception);
      }
    }

    public boolean equalsByContents(final Proxy partner)
    {
      if (partner instanceof StringProxy) {
	final StringProxy string = (StringProxy) partner;
	return mString.equals(string.mString);
      } else {
	return false;
      }
    }

    public boolean equalsWithGeometry(final Proxy partner)
    {
      return equalsByContents(partner);
    }

    public int hashCodeByContents()
    {
      return mString.hashCode();
    }

    public int hashCodeWithGeometry()
    {
      return hashCodeByContents();
    }

    public Object acceptVisitor(ProxyVisitor visitor)
      throws VisitorException
    {
      throw new UnsupportedOperationException("Can't visit StringProxy!");
    }

    //#######################################################################
    //# Data Members
    private final String mString;

  }

}