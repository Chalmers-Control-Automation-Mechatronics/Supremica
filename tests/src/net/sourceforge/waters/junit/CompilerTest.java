//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.junit
//# CLASS:   JAXBTestCase
//###########################################################################
//# $Id: CompilerTest.java,v 1.4 2005-03-03 02:33:40 robi Exp $
//###########################################################################


package net.sourceforge.waters.junit;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentManager;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.InstantiationException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.NondeterminismException;
import net.sourceforge.waters.model.des.ProductDESMarshaller;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.DuplicateIdentifierException;
import net.sourceforge.waters.model.expr.SimpleIdentifierProxy;
import net.sourceforge.waters.model.expr.UndefinedIdentifierException;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;


public class CompilerTest
  extends WatersTestCase
{

  //#########################################################################
  //# Test Cases
  public void testCompile_empty_1()
    throws JAXBException, WatersException, IOException
  {
    final String name = "empty";
    final ModuleProxy module = new ModuleProxy(name);
    final ModuleCompiler compiler =
      new ModuleCompiler(module, mDocumentManager);
    final ProductDESProxy des = compiler.compile();
    assertTrue("Unexpected name!", des.getName().equals(name));
    assertTrue("Unexpected location!", des.getLocation() == null);
    assertTrue("Unexpected event!", des.getEvents().isEmpty());
    assertTrue("Unexpected automata!", des.getAutomata().isEmpty());
  }

  public void testCompile_empty_2()
    throws JAXBException, WatersException, IOException
  {
    final String modname = "almost_empty";
    final String instname = "instance";
    try {
      final ModuleProxy module = new ModuleProxy(modname);
      final ModuleCompiler compiler =
        new ModuleCompiler(module, mDocumentManager);
      final SimpleIdentifierProxy ident = new SimpleIdentifierProxy(instname);
      final InstanceProxy instance = new InstanceProxy(ident, instname);
      module.getComponentList().add(instance);
      final ProductDESProxy des = compiler.compile();
      fail("Expected InstantiationException not caught!");
    } catch (final InstantiationException exception) {
      final String culprit = "'" + instname + "'";
      final String msg = exception.getMessage();
      assertTrue("InstantiationException <" + msg +
                 "> does not mention culprit " + culprit + "!",
                 msg.indexOf(culprit) >= 0);
    }
  }

  public void testCompile_buffer_sf1()
    throws JAXBException, WatersException, IOException
  {
    compile("buffer_sf1");
  }

  public void testCompile_colours()
    throws JAXBException, WatersException, IOException
  {
    compile("colours");
  }

  public void testCompile_machine()
    throws JAXBException, WatersException, IOException
  {
    compile("machine");
  }

  public void testCompile_nodegroup1()
    throws JAXBException, WatersException, IOException
  {
    compile("nodegroup1");
  }

  public void testCompile_nodegroup2()
    throws JAXBException, WatersException, IOException
  {
    compile("nodegroup2");
  }

  public void testCompile_nodegroup3()
    throws JAXBException, WatersException, IOException
  {
    try {
      compile("nodegroup3");
      fail("Expected NondeterminismException not caught!");
    } catch (final NondeterminismException exception) {
      final String msg = exception.getMessage();
      assertTrue("NondeterminismException <" + msg +
                 "> does not mention culprit 'q0'!",
                 msg.indexOf("'q0'") >= 0);
      assertTrue("NondeterminismException <" + msg +
                 "> does not mention culprit 'e'!",
                 msg.indexOf("'e'") >= 0);
    }
  }

  public void testCompile_nodegroup4()
    throws JAXBException, WatersException, IOException
  {
    compile("nodegroup4");
  }

  public void testCompile_small_factory_2()
    throws JAXBException, WatersException, IOException
  {
    compile("small_factory_2");
  }

  public void testCompile_small_factory_n()
    throws JAXBException, WatersException, IOException
  {
    compile("small_factory_n");
  }

  public void testCompile_tictactoe()
    throws JAXBException, WatersException, IOException
  {
    compile("tictactoe");
  }


  public void testCompile_error1_small()
    throws JAXBException, WatersException, IOException
  {
    try {
      compile("error1_small");
      fail("Expected DuplicateIdentifierException not caught!");
    } catch (final DuplicateIdentifierException exception) {
      final String msg = exception.getMessage();
      assertTrue("DuplicateIdentifierException <" + msg +
                 "> does not mention culprit 'mach'!",
                 msg.indexOf("'mach'") >= 0);
    }
  }

  public void testCompile_error2_small()
    throws JAXBException, WatersException, IOException
  {
    try {
      compile("error2_small");
      fail("Expected UndefinedIdentifierException not caught!");
    } catch (final UndefinedIdentifierException exception) {
      final String msg = exception.getMessage();
      assertTrue("UndefinedIdentifierException <" + msg +
                 "> does not mention culprit 'break'!",
                 msg.indexOf("required parameter 'break'") >= 0);
    }
  }


  //#########################################################################
  //# Utilities
  void compile(final String name)
    throws JAXBException, WatersException, IOException
  {
    final String inextname = name + mModuleMarshaller.getDefaultExtension();
    final File infilename = new File(mInputDirectory, inextname);
    final String outextname = name + mDESMarshaller.getDefaultExtension();
    final File outfilename = new File(mOutputDirectory, outextname);
    compile(infilename, outfilename);
    final File compfilename = new File(mInputDirectory, outextname);
    compare(outfilename, compfilename);
  }

  void compile(final File infilename, final File outfilename)
    throws JAXBException, WatersException, IOException
  {
    final ModuleProxy module =
      (ModuleProxy) mModuleMarshaller.unmarshal(infilename);
    final ModuleCompiler compiler =
      new ModuleCompiler(module, mDocumentManager);
    final ProductDESProxy des = compiler.compile();
    mDESMarshaller.marshal(des, outfilename);
  }

  void compare(final File filename1, final File filename2)
    throws JAXBException, ModelException, IOException
  {
    final DocumentProxy proxy1 = mDESMarshaller.unmarshal(filename1);
    final DocumentProxy proxy2 = mDESMarshaller.unmarshal(filename2);
    assertTrue("Unexpected result!", proxy1.equals(proxy2));
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException
  { 
    mInputDirectory = new File(getInputRoot(), "handwritten");
    mOutputDirectory = new File(getOutputRoot(), "compiler");
    mModuleMarshaller = new ModuleMarshaller();
    mDESMarshaller = new ProductDESMarshaller();
    mDocumentManager = new DocumentManager();
    mDocumentManager.register(mModuleMarshaller);
    mDocumentManager.register(mDESMarshaller);
  }

  protected void tearDown()
  {
    mInputDirectory = null;
    mOutputDirectory = null;
    mModuleMarshaller = null;
    mDESMarshaller = null;
    mDocumentManager = null;
  }


  //#########################################################################
  //# Data Members
  private File mInputDirectory;
  private File mOutputDirectory;
  private ModuleMarshaller mModuleMarshaller;
  private ProductDESMarshaller mDESMarshaller;
  private DocumentManager mDocumentManager;

}
