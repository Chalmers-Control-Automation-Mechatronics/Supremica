//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.external.promela;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.Arrays;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleIdentifierChecker;
import net.sourceforge.waters.model.module.ModuleIntegrityChecker;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


public abstract class AbstractPromelaUnmarshallerTest
  extends AbstractWatersTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractPromelaUnmarshallerTest()
  {
  }

  public AbstractPromelaUnmarshallerTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Successful Test Cases
  /**
   * <P>Tests the import of the Promela file
   * {supremica}<CODE>/examples/waters/promela/p101a.pml</CODE>.</P>
   *
   * <P>After running the test, the imported module is saved in
   * {supremica}<CODE>/logs/results/external/promela/PromelaUnmarshallerTest</CODE>
   * as a <CODE>.wmod</CODE> file that can be loaded into the IDE.
   * After saving, the output is compared to the expected result in
   * {supremica}<CODE>/examples/waters/promela/p101a.wmod</CODE>, and any
   * differences cause a test failure.</P>
   */
  public void testImport_p101a() throws Exception
  {
    testImport("p101a");
  }

  public void testImport_p101b() throws Exception
  {
    testImport("p101b");
  }

  public void testImport_p101c() throws Exception
  {
    testImport("p101c");
  }

  public void testImport_p101d() throws Exception
  {
    testImport("p101d");
  }

  public void testImport_p101e() throws Exception
  {
    testImport("p101e");
  }

  public void testImport_p101f() throws Exception
  {
    testImport("p101f");
  }

  public void testImport_p101g() throws Exception
  {
    testImport("p101g");
  }

  public void testImport_p101h() throws Exception
  {
    testImport("p101h");
  }

  public void testImport_p101i() throws Exception
  {
    testImport("p101i");
  }

  public void testImport_p101j() throws Exception
  {
    testImport("p101j");
  }

  public void testImport_p101k() throws Exception
  {
    testImport("p101k");
  }

  public void testImport_p102a() throws Exception
  {
    testImport("p102a");
  }

  public void testImport_p102b() throws Exception
  {
    testImport("p102b");
  }

  public void testImport_p102c() throws Exception
  {
    testImport("p102c");
  }

  public void testImport_p102d() throws Exception
  {
    testImport("p102d");
  }

  public void testImport_p102e() throws Exception
  {
    testImport("p102e");
  }

  public void testImport_p102f() throws Exception
  {
    testImport("p102f");
  }

  public void testImport_p102h() throws Exception
  {
    testImport("p102h");
  }

  public void testImport_p102i() throws Exception
  {
    testImport("p102i");
  }

  public void testImport_p102j() throws Exception
  {
    testImport("p102j");
  }

  public void testImport_p103a() throws Exception
  {
    testImport("p103a");
  }

  public void testImport_p103b() throws Exception
  {
    testImport("p103b");
  }

  public void testImport_p103c() throws Exception
  {
    testImport("p103c");
  }

  public void testImport_p103d() throws Exception
  {
    testImport("p103d");
  }

  public void testImport_p103e() throws Exception
  {
    testImport("p103e");
  }

  public void testImport_p104a() throws Exception
  {
    testImport("p104a");
  }

  public void testImport_p104b() throws Exception
  {
    testImport("p104b");
  }

  public void testImport_p104c() throws Exception
  {
    testImport("p104c");
  }

  public void testImport_p104d() throws Exception
  {
    testImport("p104d");
  }

  public void testImport_p104e() throws Exception
  {
    testImport("p104e");
  }

  public void testImport_p104f() throws Exception
  {
    testImport("p104f");
  }

  public void testImport_p104g() throws Exception
  {
    testImport("p104g");
  }

  public void testImport_p104h() throws Exception
  {
    testImport("p104h");
  }

  public void testImport_p104i() throws Exception
  {
    testImport("p104i");
  }

  public void testImport_p104j() throws Exception
  {
    testImport("p104j");
  }

  public void testImport_p104k() throws Exception
  {
    testImport("p104k");
  }

  public void testImport_p105a() throws Exception
  {
    testImport("p105a");
  }

  public void testImport_p105b() throws Exception
  {
    testImport("p105b");
  }

  public void testImport_p105c() throws Exception
  {
    testImport("p105c");
  }

  public void testImport_p105d() throws Exception
  {
    testImport("p105d");
  }

  public void testImport_p105e() throws Exception
  {
    testImport("p105e");
  }

  public void testImport_p105f() throws Exception
  {
    testImport("p105f");
  }

  public void testImport_p105g() throws Exception
  {
    testImport("p105g");
  }

  /*
   * Sending to yourself?
  public void testImport_p105h() throws Exception
  {
    testImport("p105h");
  }
  */

  public void testImport_p105i() throws Exception
  {
    testImport("p105i");
  }

  public void testImport_p105j() throws Exception
  {
    testImport("p105j");
  }

  public void testImport_p105k() throws Exception
  {
    testImport("p105k");
  }

  public void testImport_p105l() throws Exception
  {
    testImport("p105l");
  }

  public void testImport_p105m() throws Exception
  {
    testImport("p105m");
  }

  public void testImport_p105n() throws Exception
  {
    testImport("p105n");
  }

  public void testImport_p105o() throws Exception
  {
    testImport("p105o");
  }

  public void testImport_p106a() throws Exception
  {
    testImport("p106a");
  }

  public void testImport_p106b() throws Exception
  {
    testImport("p106b");
  }

  public void testImport_p106c() throws Exception
  {
    testImport("p106c");
  }

  public void testImport_p106d() throws Exception
  {
    testImport("p106d");
  }

  public void testImport_p107a() throws Exception
  {
    testImport("p107a");
  }

  public void testImport_p107b() throws Exception
  {
    testImport("p107b");
  }

  public void testImport_p107c() throws Exception
  {
    testImport("p107c");
  }

  public void testImport_p108a() throws Exception
  {
    testImport("p108a");
  }

  public void testImport_p108b() throws Exception
  {
    testImport("p108b");
  }

  public void testImport_p108c() throws Exception
  {
    testImport("p108c");
  }

  public void testImport_p108d() throws Exception
  {
    testImport("p108d");
  }

  public void testImport_p108e() throws Exception
  {
    testImport("p108e");
  }

  public void testImport_p108f() throws Exception
  {
    testImport("p108f");
  }

  public void testImport_p108g() throws Exception
  {
    testImport("p108g");
  }

  public void testImport_p108h() throws Exception
  {
    testImport("p108h");
  }

  public void testImport_p109a() throws Exception
  {
    testImport("p109a");
  }

  public void testImport_p109b() throws Exception
  {
    testImport("p109b");
  }


  /**
   * This test tries to compile all Waters module (<CODE>.wmod</CODE> files)
   * in the Promela directory, to make sure the files are at least consistent.
   */
  public void testWatersModules() throws Exception
  {
    final String ext = mModuleMarshaller.getDefaultExtension();
    final String desc = mModuleMarshaller.getDescription();
    final FileFilter filter = new StandardExtensionFileFilter(desc, ext, false);
    final File[] files = mInputDirectory.listFiles(filter);
    Arrays.sort(files);
    for (final File file : files) {
      final ModuleProxy module = (ModuleProxy) mDocumentManager.load(file);
      final ModuleCompiler compiler =
        new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
      compiler.compile();
    }
  }


  //#########################################################################
  //# Hooks
  abstract ModuleProxyFactory getModuleProxyFactory();


  //#########################################################################
  //# Utilities
  void testException(final String subdir, final String name,
                     final Class<? extends Exception> exclass,
                     final String culprit)
  throws Exception
  {
    try {
      testImport(subdir, name);
      fail("Expected " + ProxyTools.getShortClassName(exclass) +
           " not caught!");
    } catch (final Exception exception) {
      if (exclass.isAssignableFrom(exception.getClass())) {
        final String msg = exception.getMessage();
        if (msg == null) {
          fail(ProxyTools.getShortClassName(exclass) +
               " caught as expected, but message is null!");
        } else if (msg.indexOf(culprit) < 0) {
          fail(ProxyTools.getShortClassName(exclass) +
               " caught as expected, but message '" + msg +
               "' does not mention culprit '" + culprit + "'!");
        }
      } else {
        throw exception;
      }
    }
  }

  void testImport(final String name)
  throws Exception
  {
    testImport(null, name);
  }

  void testImport(final String subdir, final String name) throws Exception
  {
    final String inextname = name + mImporter.getDefaultExtension();
    getLogger().info("Unmarshalling " + inextname + " ...");
    final File indirname =
      subdir == null ? mInputDirectory : new File(mInputDirectory, subdir);
    final File infilename = new File(indirname, inextname);
    final URI promelaURI = infilename.toURI();
    final File outdirname = getOutputDirectory();
    final ModuleProxy module = mImporter.unmarshal(promelaURI);
    final String wmodextname = name + mModuleMarshaller.getDefaultExtension();
    final File wmodfilename = new File(outdirname, wmodextname);
    mIntegrityChecker.check(module);
    mModuleMarshaller.marshal(module, wmodfilename);
    assertEquals("Unexpected module name in output!", module.getName(), name);
    mIdentifierChecker.check(module);
    final File expectfile = new File(indirname, wmodextname);
    if (expectfile.exists()) {
      final URI expecturi = expectfile.toURI();
      final ModuleProxy expectmodule =
          mModuleMarshaller.unmarshal(expecturi);
      assertProxyEquals(mEqualityChecker,
                        "Unexpected module contents in output!",
                        module, expectmodule);
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mInputDirectory = new File(getWatersInputRoot(), "promela");
    final ModuleProxyFactory moduleFactory = getModuleProxyFactory();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mImporter = new PromelaUnmarshaller(moduleFactory, mDocumentManager);
    mIdentifierChecker =
      ModuleIdentifierChecker.getModuleIdentifierCheckerInstance();
    mIntegrityChecker =
      ModuleIntegrityChecker.getModuleIntegrityCheckerInstance();
    mEqualityChecker = new ModuleEqualityVisitor(true, false);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mInputDirectory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
    mImporter = null;
    mIdentifierChecker = null;
    mIntegrityChecker = null;
    mEqualityChecker = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private File mInputDirectory;
  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;
  private PromelaUnmarshaller mImporter;
  private ModuleIdentifierChecker mIdentifierChecker;
  private ModuleIntegrityChecker mIntegrityChecker;
  private ModuleEqualityVisitor mEqualityChecker;

}








