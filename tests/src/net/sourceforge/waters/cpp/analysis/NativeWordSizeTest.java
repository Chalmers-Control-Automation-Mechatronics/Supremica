//###########################################################################
//# PROJECT: Waters C++
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   WordSizeTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;


/**
 * A JUnit test to determine whether the word size has been determined
 * for the C++ compiler. This test merely invokes a compiled C++ function in
 * file <CODE>waters/base/WordSize.cpp</CODE>.
 *
 * @author Robi Malik
 */

public class NativeWordSizeTest extends AbstractWatersTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite = new TestSuite(NativeWordSizeTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testWordSize()
  {
    assertTrue(nativeWordSizeTest());
  }


  //#########################################################################
  //# Native Methods
  private static native boolean nativeWordSizeTest();

  static {
    System.loadLibrary("waters");
  }

}
