//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.subject.base;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;


public class ArrayListSubjectTest extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(ArrayListSubjectTest.class);
  }

  public static void main(final String args[])
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testAssign0()
  {
    final String[] array = {"A", "G", "C", "A", "T"};
    final ArrayListSubject<SimpleIdentifierSubject> list =
      createListSubject(array);
    final UndoInfo undo = list.createUndoInfo(list, null);
    assertNull("Got non-null assignment for unchanged lists!", undo);
  }

  public void testAssign1()
  {
    final String[] array1 = new String[0];
    final String[] array2 = {"G", "A", "C"};
    assign(array1, array2);
  }

  public void testAssign2()
  {
    final String[] array1 = {"A", "G", "C", "A", "T"};
    final String[] array2 = new String[0];
    assign(array1, array2);
  }

  public void testAssign3()
  {
    final String[] array1 = {"A", "G", "C", "A", "T"};
    final String[] array2 = {"G", "A", "C"};
    assign(array1, array2);
  }

  public void testAssign4()
  {
    final String[] array1 = {"G", "A", "C"};
    final String[] array2 = {"A", "G", "C", "A", "T"};
    assign(array1, array2);
  }

  public void testAssign5()
  {
    final String[] array1 = {"A", "G", "C", "A", "T"};
    final String[] array2 = {"A", "G", "X", "X", "T"};
    assign(array1, array2);
  }

  public void testAssign6()
  {
    final String[] array1 = {"A", "G", "C", "A", "T"};
    final String[] array2 = {"A", "G", "X", "Y", "Z", "T"};
    assign(array1, array2);
  }

  public void testAssign7()
  {
    final String[] array1 = {"X", "M", "J", "Y", "A", "U", "Z"};
    final String[] array2 = {"M", "Z", "J", "A", "W", "X", "U"};
    assign(array1, array2);
  }

  public void testAssign8()
  {
    final String[] array1 = {"M", "Z", "J", "A", "W", "X", "U"};
    final String[] array2 = {"X", "M", "J", "Y", "A", "U", "Z"};
    assign(array1, array2);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void assign(final String[] oldArray, final String[] newArray)
  {
    final ArrayListSubject<SimpleIdentifierSubject> oldList =
      createListSubject(oldArray);
    final List<SimpleIdentifierSubject> oldCopy =
      new ArrayList<SimpleIdentifierSubject>(oldList);
    final ArrayListSubject<SimpleIdentifierSubject> newList =
      createListSubject(newArray);
    final UndoInfo undo = oldList.createUndoInfo(newList, null);
    undo.redo(oldList);
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true, true);
    assertProxyListEquals(eq, "Unexpected list contents after assignment!",
                          newList, oldList);
    final List<SimpleIdentifierSubject> newCopy =
      new ArrayList<SimpleIdentifierSubject>(oldList);
    undo.undo(oldList);
    assertEquals("Unexpected list contents after undo!", oldCopy, oldList);
    undo.redo(oldList);
    assertEquals("Unexpected list contents after redo!", newCopy, oldList);
  }

  private ArrayListSubject<SimpleIdentifierSubject> createListSubject
    (final String[] array)
  {
    final ArrayListSubject<SimpleIdentifierSubject> list =
      new ArrayListSubject<SimpleIdentifierSubject>(array.length);
    for (final String name : array) {
      final SimpleIdentifierSubject ident = new SimpleIdentifierSubject(name);
      list.add(ident);
    }
    return list;
  }

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    super.tearDown();
  }


  //#########################################################################
  //# Data Members

}
