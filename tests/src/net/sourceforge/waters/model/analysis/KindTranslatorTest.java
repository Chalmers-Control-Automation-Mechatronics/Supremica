//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   KindTranslatorTest
//###########################################################################
//# $Id$
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
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.EventKind;


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
    ensureParentDirectoryExists(filename);
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
