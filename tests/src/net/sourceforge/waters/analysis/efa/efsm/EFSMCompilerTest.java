//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.efa.efsm;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.instance.InstantiationException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;



public class EFSMCompilerTest
  extends AbstractWatersTest
{
  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(EFSMCompilerTest.class);
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Handcrafting Test Cases
  public void testCompile_empty_1()
    throws EvalException, AnalysisException
  {
    final String name = "empty";
    final ModuleProxy module = mModuleFactory.createModuleProxy
      (name, null, null, null, null, null, null);
    final EFSMSystem des = compile(module);
    assertTrue("Unexpected variable!", des.getVariables().isEmpty());
    assertTrue("Unexpected transition relation",
               des.getTransitionRelations().isEmpty());
  }

  public void testCompile_empty_2()
    throws EvalException, AnalysisException
  {
    final String modname = "almost_empty";
    final String instname = "instance";
    try {
      final SimpleIdentifierProxy ident =
        mModuleFactory.createSimpleIdentifierProxy(instname);
      final InstanceProxy instance = mModuleFactory.createInstanceProxy
        (ident, instname, null);
      final ModuleProxy module = mModuleFactory.createModuleProxy
        (modname, null, null, null, null, null,
         Collections.singletonList(instance));
      compile(module);
      fail("Expected InstantiationException not caught!");
    } catch (final InstantiationException exception) {
      final String culprit = "'" + instname + "'";
      final String msg = exception.getMessage();
      assertTrue("InstantiationException <" + msg +
                 "> does not mention culprit " + culprit + "!",
                 msg.indexOf(culprit) >= 0);
    }
  }


  //#########################################################################
  //# Successful Test Cases using EFA
  public void testEFSMCompiler1()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm1");
  }

  public void testEFSMCompiler2()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm2");
  }

  public void testEFSMCompiler3()
    throws IOException, WatersException
  {
    compileError("tests", "efsm", "efsm3",null,SharedEventException.class, "'a'");
  }

  public void testEFSMCompiler4()
    throws IOException, WatersException
  {
    compileError("tests", "efsm", "efsm4",null,SharedEventException.class, "'a'");
  }

  public void testEFSMCompiler5()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm5");
  }

  public void testEFSMCompiler6()
    throws IOException, WatersException
  {
    compileError("tests", "efsm", "efsm6", null,
                 SharedEventException.class, "'a'");
  }

  public void testEFSMCompiler7()
    throws IOException, WatersException
  {
    compileError("tests", "efsm", "efsm7", null,
                 SharedEventException.class, "'a'");
  }

  public void testEFSMCompiler8()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm8");
  }

  public void testEFSMCompiler9()
    throws IOException, WatersException
  {
    compileError("tests", "efsm", "efsm9", null,
                 SharedEventException.class, "'a'");
  }

  public void testEFSMCompiler10()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm10");
  }

  public void testEFSMCompiler11()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm11");
  }

  public void testEFSMCompiler12()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm12");
  }

  public void testEFSMCompiler13()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm13");
  }

  public void testEFSMCompiler14()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm14");
  }

  public void testEFSMCompiler15()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm15");
  }

  public void testEFSMCompiler16()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm16");
  }

  public void testEFSMCompiler17()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm17");
  }

  public void testEFSMCompiler18()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm18");
  }

  public void testEFSMCompiler19()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm19");
  }

  public void testEFSMCompiler20()
    throws IOException, WatersException
  {
    compileError("tests", "efsm", "efsm20", null,
                 SharedEventException.class, ":accepting");
  }

  public void testEFSMCompiler21()
    throws IOException, WatersException
  {
    compile("tests", "efsm", "efsm21");
  }


  //#########################################################################
  //# Customisation
  void configure(final EFSMCompiler compiler)
  {
    compiler.setSourceInfoEnabled(true);
  }

  String getTestSuffix()
  {
    return "result";
  }


  //#########################################################################
  //# Utilities
  @SuppressWarnings("unused")
  private void compileError(final String dirname,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass)
    throws IOException, WatersException
  {
    final String[] culprits = {};
    compileError(dirname, name, bindings, exclass, culprits);
  }

  @SuppressWarnings("unused")
  private void compileError(final String dirname,
                            final String subdirname,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass)
    throws IOException, WatersException
  {
    final String[] culprits = {};
    compileError(dirname, subdirname, name, bindings, exclass, culprits);
  }

  @SuppressWarnings("unused")
  private void compileError(final String dirname,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String culprit)
    throws IOException, WatersException
  {
    final String[] culprits = {culprit};
    compileError(dirname, name, bindings, exclass, culprits);
  }

  private void compileError(final String dirname,
                            final String subdirname,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String culprit)
    throws IOException, WatersException
  {
    final String[] culprits = {culprit};
    compileError(dirname, subdirname, name, bindings, exclass, culprits);
  }

  @SuppressWarnings("unused")
  private void compileError(final String dirname,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String culprit1,
                            final String culprit2)
    throws IOException, WatersException
  {
    final String[] culprits = {culprit1, culprit2};
    compileError(dirname, name, bindings, exclass, culprits);
  }

  @SuppressWarnings("unused")
  private void compileError(final String dirname,
                            final String subdirname,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String culprit1,
                            final String culprit2)
    throws IOException, WatersException
  {
    final String[] culprits = {culprit1, culprit2};
    compileError(dirname, subdirname, name, bindings, exclass, culprits);
  }

  private void compileError(final String dirname,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String[] culprits)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    compileError(dir, name, bindings, exclass, culprits);
  }

  private void compileError(final String dirname,
                            final String subdirname,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String[] culprits)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    final File subdir = new File(dir, subdirname);
    compileError(subdir, name, bindings, exclass, culprits);
  }

  private void compileError(final File dir,
                            final String name,
                            final List<ParameterBindingProxy> bindings,
                            final Class<? extends WatersException> exclass,
                            final String[] culprits)
    throws IOException, WatersException
  {
    try {
      compile(dir, name, bindings, true);
      fail("Expected " + exclass.getName() + " not caught!");
    } catch (final WatersException exception) {
      if (exception.getClass() == exclass) {
        for (int i = 0; i < culprits.length; i++) {
          final String culprit = culprits[i];
          final String msg = exception.getMessage();
          assertNotNull("Caught " + exclass.getName() +
                        " as expected, but no error message found!", msg);
          assertTrue("Caught " + exclass.getName() +
                     " as expected, but message '" + msg +
                     "' does not mention culprit: " + culprit + "!",
                     msg.indexOf(culprit) >= 0);
        }
        if (exception instanceof EvalException) {
          final EvalException evalException = (EvalException) exception;
          final Proxy location = evalException.getLocation();
          assertNotNull("Caught " + exception.getClass().getName() + " <" +
                        exception.getMessage() + "> provides no location!",
                        location);
        }
      } else {
        throw exception;
      }
    }
  }

  @SuppressWarnings("unused")
  private void compile(final String dirname, final String name)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    compile(dir, name, null, false);
  }

  private void compile(final String dirname,
                       final String subdirname,
                       final String name)
    throws IOException, WatersException
  {
    final File root = getWatersInputRoot();
    final File dir = new File(root, dirname);
    final File subdir = new File(dir, subdirname);
    compile(subdir, name, null, false);
  }

  private void compile(final File dir,
                       final String name,
                       final List<ParameterBindingProxy> bindings,
                       final boolean appendToName)
    throws IOException, WatersException
  {
    final String inextname = name + mModuleMarshaller.getDefaultExtension();
    final File infilename = new File(dir, inextname);
    final StringBuilder buffer = new StringBuilder(name);
    if (bindings != null && appendToName) {
      for (final ParameterBindingProxy binding : bindings) {
        buffer.append('-');
        buffer.append(binding.getExpression().toString());
      }
    }
    final String ext = mModuleMarshaller.getDefaultExtension();
    final int pos = buffer.length();
    buffer.append(ext);
    final String outextname = buffer.toString();
    final File outfilename = new File(mOutputDirectory, outextname);
    final ModuleProxy outputModule = compile(infilename, outfilename, bindings);
    final String suffix = getTestSuffix();
    buffer.setLength(pos);
    buffer.append('-');
    buffer.append(suffix);
    buffer.append(ext);
    final String suffixedname = buffer.toString();
    final File suffixedfilename = new File(dir, suffixedname);
    if (suffixedfilename.exists()) {
      compare(outputModule, suffixedfilename);
    }
  }

  private ModuleProxy compile(final File infilename,
                             final File outfilename,
                             final List<ParameterBindingProxy> bindings)
    throws IOException, WatersException
  {
    final URI uri = infilename.toURI();
    final ModuleProxy inputModule = mModuleMarshaller.unmarshal(uri);
    final EFSMSystem system = compile(inputModule, bindings);
    final String name = system.getName() + "-" + getTestSuffix();
    system.setName(name);
    final ModuleProxy outputModule = mImporter.importModule(system);
    mModuleMarshaller.marshal(outputModule, outfilename);
    return outputModule;
  }


  private EFSMSystem compile(final ModuleProxy module,
                             final List<ParameterBindingProxy> bindings)
    throws EvalException, AnalysisException
  {
    final EFSMCompiler compiler =
      new EFSMCompiler(mDocumentManager, module);
    configure(compiler);
    return compiler.compile(bindings);
  }

  private EFSMSystem compile(final ModuleProxy module)
    throws EvalException, AnalysisException
  {
    return compile(module, null);
  }

  private void compare(final ModuleProxy module, final File expectedFile)
    throws WatersUnmarshalException, IOException
  {
    final URI uri = expectedFile.toURI();
    final ModuleProxy expectedModule = mModuleMarshaller.unmarshal(uri);
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true, false);
    assertProxyEquals(eq, "Unexpected module in output", module, expectedModule);
  }

//  private void compare(final File filename1, final File filename2)
//    throws IOException, WatersUnmarshalException
//  {
//    final URI uri1 = filename1.toURI();
//    final URI uri2 = filename2.toURI();
//    final DocumentProxy proxy1 = mProductDESMarshaller.unmarshal(uri1);
//    final DocumentProxy proxy2 = mProductDESMarshaller.unmarshal(uri2);
//    assertProductDESProxyEquals(proxy1, proxy2);
//  }

  @SuppressWarnings("unused")
  private ParameterBindingProxy createBinding(final String name,
                                              final int value)
  {
    final IntConstantProxy expr = mModuleFactory.createIntConstantProxy(value);
    return mModuleFactory.createParameterBindingProxy(name, expr);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    mOutputDirectory = getOutputDirectory();
    mModuleFactory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(mModuleFactory, optable);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mImporter  = new EFSMSystemImporter(mModuleFactory, optable);
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mOutputDirectory = null;
    mModuleFactory = null;
    mModuleMarshaller = null;
    mDocumentManager = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private File mOutputDirectory;
  private ModuleProxyFactory mModuleFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private DocumentManager mDocumentManager;
  private EFSMSystemImporter mImporter;

}
