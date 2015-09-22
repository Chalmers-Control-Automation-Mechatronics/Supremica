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

package net.sourceforge.waters.analysis.modular;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.annotation.ProjectingNonBlockingChecker;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class SpeedComparison
{
  public static class WFilter
    implements FilenameFilter
  {
    public boolean accept(final File dir, final String name)
    {
      return name.endsWith(".wmod");
    }
  }

  public static void main(final String[] args) throws Exception
  {
    /*JFileChooser chooser = new JFileChooser(new File("/home/darius/waters"));
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "wmod", "wmod");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(null);
    if(returnVal == JFileChooser.APPROVE_OPTION) {
       System.out.println("You chose to open this file: " +
            chooser.getSelectedFile().getName());
    }
    ProductDESProxy model = getCompiledDES(chooser.getSelectedFile(), null);
    ProjectingNonBlockingChecker pnbc = new ProjectingNonBlockingChecker(model, mProductDESProxyFactory);
    pnbc.setNodeLimit(1000000);
    try {
      System.out.println(pnbc.run());
      System.out.println(pnbc.getCounterExample());
    } catch (AnalysisException a) {
      a.printStackTrace();
    }*/
    /* List<File> files = new ArrayList<File>();
    File dir = new File("/media/DATA/Projects/supr/supremica/Supremica/examples/waters/tests");
    for (File d : Arrays.asList(dir.listFiles())) {
      files.addAll(Arrays.asList(d.listFiles(new WFilter())));
    } */
    final List<File> files = new ArrayList<File>();
    final File dir = new File("/home/darius/HugoExamples/");
    files.addAll(Arrays.asList(dir.listFiles(new WFilter())));
    Collections.sort(files);
    for (final File file : files) {
      final ProductDESProxy model = getCompiledDES(file, null);
      System.out.println(model.getName());
      ProjectingNonBlockingChecker pnbc = new ProjectingNonBlockingChecker(model, mProductDESProxyFactory);
      pnbc.setNodeLimit(1000000);
      try {
        System.out.println(pnbc.run());
        //System.out.println(pnbc.getCounterExample());
      } catch (final AnalysisException a) {
        a.printStackTrace();
      }
      pnbc = null;
      /*CompNonBlockingChecker cnbc = new CompNonBlockingChecker(model, mProductDESProxyFactory);
      cnbc.setNodeLimit(1000000);
      try {
        System.out.println(cnbc.run());
        //System.out.println(cnbc.getCounterExample());
      } catch (AnalysisException a) {
        a.printStackTrace();
      }*/
    }
  }

  private static ProductDESProxy getCompiledDES
    (final File filename,
     final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final DocumentProxy doc = mDocumentManager.load(filename);
    if (doc instanceof ProductDESProxy) {
      return (ProductDESProxy) doc;
    } else if (doc instanceof ModuleProxy) {
      final ModuleProxy module = (ModuleProxy) doc;
      final ModuleCompiler compiler =
        new ModuleCompiler(mDocumentManager, mProductDESProxyFactory, module);
      return compiler.compile(bindings);
    } else {
      return null;
    }
  }

  private static DocumentManager mDocumentManager = new DocumentManager();
  private static ProductDESProxyFactory mProductDESProxyFactory = ProductDESElementFactory.getInstance();

  static {
    final ModuleElementFactory mModuleFactory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    try {
      final JAXBModuleMarshaller modmarshaller =
        new JAXBModuleMarshaller(mModuleFactory, optable);
      mDocumentManager.registerUnmarshaller(modmarshaller);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}








