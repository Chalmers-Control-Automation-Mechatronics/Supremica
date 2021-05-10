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

package net.sourceforge.waters.analysis.efa.unified;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.instance.InstantiationException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;



public class UnifiedEFACompilerTest
  extends AbstractAnalysisTest
{
  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(UnifiedEFACompilerTest.class);
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
    final UnifiedEFASystem des = compile(module);
    assertTrue("Unexpected variable!", des.getVariables().isEmpty());
    assertTrue("Unexpected event!", des.getEvents().isEmpty());
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
    final ModuleProxy module = loadModule("tests", "efsm", "efsm1");
    compileAndTest(module);
  }

  public void testEFSMCompiler2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm2");
    compileAndTest(module);
  }

  public void testEFSMCompiler3()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm3");
    compileAndTest(module);
  }

  public void testEFSMCompiler4()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm4");
    compileAndTest(module);
  }

  public void testEFSMCompiler5()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm5");
    compileAndTest(module);
  }

  public void testEFSMCompiler6()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm6");
    compileAndTest(module);
  }

  public void testEFSMCompiler7()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm7");
    compileAndTest(module);
  }

  public void testEFSMCompiler8()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm8");
    compileAndTest(module);
  }

  public void testEFSMCompiler9()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm9");
    compileAndTest(module);
  }

  public void testEFSMCompiler10()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm10");
    compileAndTest(module);
  }

  public void testEFSMCompiler11()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm11");
    compileAndTest(module);
  }

  public void testEFSMCompiler12()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm12");
    compileAndTest(module);
  }

  public void testEFSMCompiler13()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm13");
    compileAndTest(module);
  }

  public void testEFSMCompiler14()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm14");
    compileAndTest(module);
  }

  public void testEFSMCompiler15()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm15");
    compileAndTest(module);
  }

  public void testEFSMCompiler16()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm16");
    compileAndTest(module);
  }

  public void testEFSMCompiler17()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm17");
    compileAndTest(module);
  }

  public void testEFSMCompiler18()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm18");
    compileAndTest(module);
  }

  public void testEFSMCompiler19()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm19");
    compileAndTest(module);
  }

  public void testEFSMCompiler20()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm20");
    compileAndTest(module);
  }

  public void testEFSMCompiler21()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "efsm21");
    compileAndTest(module);
  }


  //#########################################################################
  //# Customisation
  void configure(final UnifiedEFACompiler compiler)
  {
    // TODO Configure compiler here
    compiler.setSourceInfoEnabled(true);
    /*
    final Collection<String> empty = Collections.emptyList();
    compiler.setEnabledPropertyNames(empty);
    */
  }

  String getTestSuffix()
  {
    return "unified";
  }


  //#########################################################################
  //# Utilities
  private UnifiedEFASystem compile(final ModuleProxy module)
    throws EvalException, AnalysisException
  {
    return compile(module, null);
  }

  private UnifiedEFASystem compile(final ModuleProxy module,
                                   final List<ParameterBindingProxy> bindings)
    throws EvalException, AnalysisException
  {
    final UnifiedEFACompiler compiler =
      new UnifiedEFACompiler(mDocumentManager, module);
    configure(compiler);
    return compiler.compile(bindings);
  }

  private void compileAndTest(final ModuleProxy module)
    throws IOException, WatersException
  {
    compileAndTest(module, null);
  }

  private void compileAndTest(final ModuleProxy module,
                              final List<ParameterBindingProxy> bindings)
    throws IOException, WatersException
  {
    final UnifiedEFASystem system = compile(module, bindings);
    final String name = system.getName();
    final StringBuilder buffer = new StringBuilder(name);
    if (bindings != null) {
      for (final ParameterBindingProxy binding : bindings) {
        buffer.append('-');
        buffer.append(binding.getExpression().toString());
      }
    }
    buffer.append('-');
    buffer.append(getTestSuffix());
    system.setName(buffer.toString());
    final ModuleProxy outputModule = mImporter.importModule(system);
    final String ext = mModuleMarshaller.getDefaultExtension();
    buffer.append(ext);
    final String suffixedname = buffer.toString();
    final File outfilename = new File(getOutputDirectory(), suffixedname);
    mModuleMarshaller.marshal(outputModule, outfilename);
    final File infilename = module.getFileLocation();
    final File indirname = infilename.getParentFile();
    final File cmpfilename = new File(indirname, suffixedname);
    compare(outputModule, cmpfilename);
  }

  private void compare(final ModuleProxy module, final File expectedFile)
    throws WatersUnmarshalException, IOException
  {
    final URI uri = expectedFile.toURI();
    final ModuleProxy expectedModule = mModuleMarshaller.unmarshal(uri);
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true, false);
    assertProxyEquals(eq, "Unexpected module in output", module, expectedModule);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    mModuleFactory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new SAXModuleMarshaller(mModuleFactory, optable);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mImporter = new UnifiedEFASystemImporter(mModuleFactory, optable);
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mModuleFactory = null;
    mModuleMarshaller = null;
    mDocumentManager = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private ModuleProxyFactory mModuleFactory;
  private SAXModuleMarshaller mModuleMarshaller;
  private DocumentManager mDocumentManager;

  private UnifiedEFASystemImporter mImporter;

}
