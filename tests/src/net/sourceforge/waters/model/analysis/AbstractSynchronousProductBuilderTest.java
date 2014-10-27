//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractSynchronousProductBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;


public abstract class AbstractSynchronousProductBuilderTest
  extends AbstractAutomatonBuilderTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractSynchronousProductBuilderTest()
  {
  }

  public AbstractSynchronousProductBuilderTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAutomatonBuilderTest
  @Override
  protected String getExpectedName(final String desname,
                                   final List<ParameterBindingProxy> bindings)
  {
    try {
      final String name;
      final String ext;
      final int dotpos = desname.indexOf('.');
      if (dotpos >= 0) {
        name = desname.substring(0, dotpos);
        ext = desname.substring(dotpos);
      } else {
        name = desname;
        ext = "";
      }
      final StringWriter writer = new StringWriter();
      writer.write(name);
      if (bindings != null) {
        for (final ParameterBindingProxy binding : bindings) {
          writer.write('-');
          ModuleProxyPrinter.printProxy(writer, binding.getExpression());
        }
      }
      writer.write("-sync");
      writer.write(ext);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Test Cases --- handcrafted
  public void testReentrant()
    throws Exception
  {
    testSmallFactory2();
    testTransferline__1();
    testTransferline__1();
    testSmallFactory2u();
  }

  public void testStateOverflowException()
    throws Exception
  {
    try {
      final AutomatonBuilder builder = getAutomatonBuilder();
      builder.setNodeLimit(2);
      testTransferline__1();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      assertEquals("Unexpected overflow kind!",
                   OverflowKind.STATE, exception.getOverflowKind());
    }
  }

  public void testTransitionOverflowException()
    throws Exception
  {
    try {
      final AutomatonBuilder builder = getAutomatonBuilder();
      builder.setTransitionLimit(3);
      testTransferline__1();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      assertEquals("Unexpected overflow kind!",
                   OverflowKind.TRANSITION, exception.getOverflowKind());
    }
  }


  //#########################################################################
  //# Test Cases
  public void testSmallFactory2() throws Exception
  {
    runAutomatonBuilder("handwritten", "small_factory_2.wmod");
  }

  public void testSmallFactory2u() throws Exception
  {
    runAutomatonBuilder("handwritten", "small_factory_2u.wmod");
  }


  //#########################################################################
  //# Test Cases -- Parameterised
  public void testTransferline__1() throws Exception
  {
    checkTransferline(1);
  }

  public void checkTransferline(final int n) throws Exception
  {
    final List<ParameterBindingProxy> bindings =
      new LinkedList<ParameterBindingProxy>();
    final ParameterBindingProxy binding = createBinding("N", n);
    bindings.add(binding);
    runAutomatonBuilder(bindings, "handwritten", "transferline.wmod");
  }

}
