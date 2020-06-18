//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


public class KindTranslatorTest
  extends AbstractWatersTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(KindTranslatorTest.class);
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Constructors
  public KindTranslatorTest()
  {
  }

  public KindTranslatorTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Test Cases
  public void testSerialize()
    throws ClassNotFoundException, IOException
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final EventProxy event =
      factory.createEventProxy("e", EventKind.CONTROLLABLE, true);
    final KindTranslator trans0 = IdenticalKindTranslator.getInstance();
    assertEquals(trans0.getEventKind(event), EventKind.CONTROLLABLE);
    final File filename =
      new File(getOutputDirectory(), "KindTranslatorTest.ser");
    final FileOutputStream fos =  new FileOutputStream(filename);
    final ObjectOutputStream out = new ObjectOutputStream(fos);
    out.writeObject(trans0);
    out.close();
    final FileInputStream fis =  new FileInputStream(filename);
    final ObjectInputStream in = new ObjectInputStream(fis);
    final KindTranslator trans1 = (KindTranslator) in.readObject();
    in.close();
    assertFalse(trans0 == trans1);
    assertEquals(trans1.getEventKind(event), EventKind.CONTROLLABLE);
  }

}
