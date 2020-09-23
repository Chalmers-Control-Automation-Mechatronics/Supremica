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

package net.sourceforge.waters.gui.renderer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NestedBlockProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class EPSGraphPrinterTest extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(EPSGraphPrinterTest.class);
  }

  public static void main(final String args[])
  {
    junit.textui.TestRunner.run(suite());
  }


  public EPSGraphPrinterTest()
  {
  }

  public EPSGraphPrinterTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new SAXModuleMarshaller(factory, optable);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mPrinter = new EPSPrinterVisitor();
  }


  //#########################################################################
  //# Test Cases
  /*
   * TODO: Implement and test for MissingGeometryException ...
  public void testSmallFactory2() throws Exception
  {
    final String group = "handwritten";
    final String name = "small_factory_2.wmod";
    printGraphs(group, name);
  }
  */

  public void testAT() throws Exception
  {
    printGraphs("tests", "mtsa", "AT.wmod");
  }

  public void testKoordwsp() throws Exception
  {
    printGraphs("valid", "central_locking", "koordwsp.wmod");
  }

  public void testTransferline() throws Exception
  {
    printGraphs("handwritten", "transferline.wmod");
  }


  //#########################################################################
  //# Auxiliary Methods
  protected void printGraphs(final String... path)
    throws Exception
  {
    final String ext = mModuleMarshaller.getDefaultExtension();
    final File file = getInputFile(path, ext);
    printGraphs(file);
  }

  protected void printGraphs(final File filename)
    throws Exception
  {
    final DocumentProxy doc = mDocumentManager.load(filename);
    final ModuleProxy module = (ModuleProxy) doc;
    mPrinter.processModule(module);
  }


  //#########################################################################
  //# Inner Class EPSPrinterVisitor
  private class EPSPrinterVisitor
    extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private void processModule(final ModuleProxy module)
      throws IOException
    {
      try {
        visitModuleProxy(module);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof IOException) {
          throw (IOException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    /**
     * Skip instances and variables.
     */
    @Override
    public Object visitComponentProxy(final ComponentProxy comp)
    {
      return null;
    }

    /**
     * Visit all components in given module and print them.
     */
    @Override
    public Object visitModuleProxy(final ModuleProxy module)
      throws VisitorException
    {
      final Collection<Proxy> components = module.getComponentList();
      return visitCollection(components);
    }

    /**
     * Visit the children of conditional and foreach constructs in the
     * component list.
     */
    @Override
    public Object visitNestedBlockProxy(final NestedBlockProxy block)
      throws VisitorException
    {
      final Collection<Proxy> body = block.getBody();
      return visitCollection(body);
    }

    /**
     * Visit simple components and output eps-files.
     * The reason why visitGraphProxy is not used is because we need the name.
     */
    @Override
    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
        final File dir = getOutputDirectory();
        final String name = comp.getName();
        final File file = new File(dir, name + ".eps");
        final GraphProxy graph = comp.getGraph();
        final EPSGraphPrinter printer = new EPSGraphPrinter(graph, file);
        printer.print();
        return null;
      } catch (final IOException exception) {
        throw wrap(exception);
      }
    }
  }


  //#########################################################################
  //# Data Members
  private DocumentManager mDocumentManager;
  private SAXModuleMarshaller mModuleMarshaller;
  private EPSPrinterVisitor mPrinter;

}
