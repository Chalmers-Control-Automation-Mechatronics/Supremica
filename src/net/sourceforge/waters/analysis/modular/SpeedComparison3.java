//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
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


public class SpeedComparison3
{
  public static List<File> files()
  {
    final List<File> files = new ArrayList<File>();
    files.add(new File("examples/waters/tests/profisafe/profisafe_i4_slave.wmod"));
    files.add(new File("examples/waters/tests/profisafe/profisafe_i4_host.wmod"));
    files.add(new File("examples/waters/tests/profisafe/profisafe_o4_slave.wmod"));
    files.add(new File("examples/waters/tests/profisafe/profisafe_o4_host.wmod"));
    files.add(new File("examples/waters/tests/profisafe/profisafe_i5_host.wmod"));
    files.add(new File("examples/waters/tests/profisafe/profisafe_o5_host.wmod"));
    files.add(new File("examples/waters/tests/profisafe/profisafe_i6_host.wmod"));
    files.add(new File("examples/waters/tests/profisafe/profisafe_o6_host.wmod"));
    files.add(new File("examples/waters/tests/incremental_suite/big_bmw.wmod"));
    files.add(new File("examples/waters/tests/incremental_suite/ftechnik.wmod"));
    files.add(new File("examples/waters/tests/incremental_suite/tbed_nocoll.wmod"));
    files.add(new File("examples/waters/tests/incremental_suite/tbed_noderail.wmod"));
    files.add(new File("examples/waters/tests/incremental_suite/verriegel4.wmod"));
    return files;
  }

  public static List<File> files2()
  {
    final List<File> files = new ArrayList<File>();
    files.add(new File("examples/waters/tests/incremental_suite/tbed_ctct.wmod"));
    files.add(new File("examples/waters/tests/incremental_suite/tbed_nocoll.wmod"));
    files.add(new File("examples/waters/tests/incremental_suite/tbed_noderail.wmod"));
    files.add(new File("examples/waters/tests/incremental_suite/rhone_tough.wmod"));
    files.add(new File("examples/waters/tests/incremental_suite/tbed_uncont.wmod"));
    return files;
  }

  public static class WFilter
    implements FilenameFilter
  {
    @Override
    public boolean accept(final File dir, final String name)
    {
      return name.endsWith(".wmod");
    }
  }

  public static void main(final String[] args) throws Exception
  {
    ProductDESProxy model = null; //getCompiledDES(new File("rhone_tough.wmod"), null);
    final ControllabilityChecker nativec = new NativeControllabilityChecker(mProductDESProxyFactory);
    final List<File> files = new ArrayList<File>();
    //
    final File dir = new File("/home/darius/Projects/supr/supremica/Supremica/waters/logs/results/samples/maze/NDPRMazeTest/");
    files.addAll(Arrays.asList(dir.listFiles(new WFilter())));
    Collections.sort(files);
    //dir = new File("examples/waters/tests/profisafe/");
    //files.addAll(Arrays.asList(dir.listFiles(new WFilter())));
    //files = files();
    //files = files2();
    //files.addAll(files2());
    PrintStream ps = null;
    PrintStream pt = null;
    /*ps = new PrintStream("modstate.txt");
    pt = new PrintStream("modtime.txt");
    for (File f : files) {
      model = getCompiledDES(f, null);
    }
    for (HeuristicType h : types())
    {
      ps.println(h);
      pt.println(h);
      ps.print(",");
      pt.print(",");
      List<ModularHeuristic> l = list(h);
      for (ModularHeuristic mh : l)
      {
        ps.print(mh);
        ps.print(",");
        pt.print(mh);
        pt.print(",");
      }
      ps.println();
      pt.println();
      for (File f : files) {
        model = getCompiledDES(f, null);
        ps.print(model.getName());
        ps.print(",");
        pt.print(model.getName());
        pt.print(",");
        for (ModularHeuristic mh : l) {
          mo = new ModularControllabilityChecker(model, mProductDESProxyFactory,
                                                 nativec, mh, true);
          LanguageInclusionChecker lang = new ModularLanguageInclusionChecker(model,
                                         mProductDESProxyFactory,
                                         mo, mh);
          lang.setStateLimit(2000000);
          mo.setStateLimit(2000000);
          long timeb = 0;
          long timea = 0;
          try {
            timeb = System.currentTimeMillis();
            mo.run();
            //lang.run();
            timea = System.currentTimeMillis();
            ps.print(mo.getAnalysisResult().getTotalNumberOfStates());
            ps.print(",");
          } catch (Throwable t) {
            timea = System.currentTimeMillis();
            ps.print(",");
          }
          pt.print(timea - timeb);
          pt.print(",");
        }
        ps.println();
        pt.println();
      }
    }
    ps.flush();
    pt.flush();
    ps.close();
    pt.close();
    /*ps = new PrintStream("parlangstate.txt");
    pt = new PrintStream("parlangtime.txt");
    for (HeuristicType h : types())
    {
      ps.println(h);
      pt.println(h);
      ps.print(",");
      pt.print(",");
      List<ModularHeuristic> l = list(h);
      for (ModularHeuristic mh : l)
      {
        ps.print(mh);
        ps.print(",");
        pt.print(mh);
        pt.print(",");
      }
      ps.println();
      pt.println();
      for (File f : files) {
        model = getCompiledDES(f, null);
        ps.print(model.getName());
        ps.print(",");
        pt.print(model.getName());
        pt.print(",");
        for (ModularHeuristic mh : l) {
          mo = new ParallelModularControllabilityChecker(model, mProductDESProxyFactory,
                                                         nativec, mh);
          LanguageInclusionChecker lang = new ModularLanguageInclusionChecker(model,
                                         mProductDESProxyFactory,
                                         mo, mh);
          lang.setStateLimit(2000000);
          mo.setStateLimit(2000000);
          long timeb = 0;
          long timea = 0;
          try {
            timeb = System.currentTimeMillis();
            //mo.run();
            lang.run();
            timea = System.currentTimeMillis();
            ps.print(mo.getAnalysisResult().getTotalNumberOfStates());
            ps.print(",");
          } catch (Throwable t) {
            timea = System.currentTimeMillis();
            ps.print(",");
          }
          pt.print(timea - timeb);
          pt.print(",");
        }
        ps.println();
        pt.println();
      }
    }
    ps.flush();
    pt.flush();
    ps.close();
    pt.close();
    ps = new PrintStream("culstate.txt");
    pt = new PrintStream("cultime.txt");
    for (HeuristicType h : types())
    {
      ps.println(h);
      pt.println(h);
      ps.print(",");
      pt.print(",");
      List<ModularHeuristic> l = list(h);
      for (ModularHeuristic mh : l)
      {
        ps.print(mh);
        ps.print(",");
        pt.print(mh);
        pt.print(",");
      }
      ps.println();
      pt.println();
      for (File f : files) {
        model = getCompiledDES(f, null);
        ps.print(model.getName());
        ps.print(",");
        pt.print(model.getName());
        pt.print(",");
        for (ModularHeuristic mh : l) {
          mo = new CullingControllabilityChecker(model, mProductDESProxyFactory,
                                                 nativec, mh, true);
          LanguageInclusionChecker lang = new ModularLanguageInclusionChecker(model,
                                         mProductDESProxyFactory,
                                         mo, mh);
          lang.setStateLimit(2000000);
          mo.setStateLimit(2000000);
          long timeb = 0;
          long timea = 0;
          try {
            timeb = System.currentTimeMillis();
            mo.run();
            //lang.run();
            timea = System.currentTimeMillis();
            ps.print(mo.getAnalysisResult().getTotalNumberOfStates());
            ps.print(",");
          } catch (Throwable t) {
            timea = System.currentTimeMillis();
            ps.print(",");
          }
          pt.print(timea - timeb);
          pt.print(",");
        }
        ps.println();
        pt.println();
      }
    }
    ps.flush();
    pt.flush();
    ps.close();
    pt.close();*/
    /*ps = new PrintStream("projlangstate.txt");
    pt = new PrintStream("projlangtime.txt");
    for (HeuristicType h : types())
    {
      ps.println(h);
      pt.println(h);
      ps.print(",");
      pt.print(",");
      List<ModularHeuristic> l = list(h);
      for (ModularHeuristic mh : l)
      {
        ps.print(mh);
        ps.print(",");
        pt.print(mh);
        pt.print(",");
      }
      ps.println();
      pt.println();
      for (File f : files) {
        model = getCompiledDES(f, null);
        ps.print(model.getName());
        ps.print(",");
        pt.print(model.getName());
        pt.print(",");
        for (ModularHeuristic mh : l) {
          mo = new ProjectingControllabilityChecker(model, mProductDESProxyFactory,
                                                    nativec, mh, false);
          LanguageInclusionChecker lang = new ModularLanguageInclusionChecker(model,
                                         mProductDESProxyFactory,
                                         mo, mh);
          mo.setStateLimit(2000000);
          lang.setStateLimit(2000000);
          long timeb = 0;
          long timea = 0;
          try {
            timeb = System.currentTimeMillis();
            //mo.run();
            lang.run();
            timea = System.currentTimeMillis();
            ps.print(mo.getAnalysisResult().getTotalNumberOfStates());
            ps.print(",");
          } catch (Throwable t) {
            timea = System.currentTimeMillis();
            ps.print(",");
          }
          pt.print(timea - timeb);
          pt.print(",");
        }
        ps.println();
        pt.println();
      }
    }
    ps.flush();
    pt.flush();
    ps.close();
    pt.close();*/
    /*ps = new PrintStream("projnatstate.csv");
    pt = new PrintStream("projnattime.csv");
    for (int i = 100; i <= 3200; i *= 2) {
      ps.print(i);
      ps.print(",");
      pt.print(i);
      pt.print(",");
    }
    ps.println();
    pt.println();
    for (File f : files) {
      model = getCompiledDES(f, null);
      ps.print(model.getName());
      ps.print(",");
      pt.print(model.getName());
      pt.print(",");
      //for (int i = 100; i <= 3200; i *= 2) {
      int i = 6000;
        ControllabilityChecker mo = new ProjectingControllabilityChecker
          (model, mProductDESProxyFactory, nativec, false, i);
        LanguageInclusionChecker lang = new ModularLanguageInclusionChecker
	  (model, mProductDESProxyFactory, mo);
        mo.setNodeLimit(2000000);
        lang.setNodeLimit(2000000);
        long timeb = 0;
        long timea = 0;
        try {
          timeb = System.currentTimeMillis();
          //mo.run();
          lang.run();
          timea = System.currentTimeMillis();
          ps.print(mo.getAnalysisResult().getTotalNumberOfStates());
          ps.print(",");
        } catch (Throwable t) {
          timea = System.currentTimeMillis();
          ps.print(",");
        }
        pt.print(timea - timeb);
        pt.print(",");
      //}
      ps.println();
      pt.println();
    }
    ps.flush();
    pt.flush();
    ps.close();
    pt.close();*/
    ps = new PrintStream("ndprojnatstate.csv");
    pt = new PrintStream("ndprojnattime.csv");
    for (int i = 100; i <= 3200; i *= 2) {
      ps.print(i);
      ps.print(",");
      pt.print(i);
      pt.print(",");
    }
    ps.println();
    pt.println();
    for (final File f : files) {
      model = getCompiledDES(f, null);
      ps.print(model.getName());
      ps.print(",");
      pt.print(model.getName());
      pt.print(",");
      //for (int i = 100; i <= 3200; i *= 2) {
      final int i = 6000;
      System.out.println(model.getName());
      final ControllabilityChecker mo =
        new NDProjectingControllabilityChecker(model, mProductDESProxyFactory,
                                               nativec, false, i);
      final ModularLanguageInclusionChecker lang =
        new ModularLanguageInclusionChecker(model,
                                            mProductDESProxyFactory,
                                            null);
      lang.setInnerControllabilityChecker(mo);
        mo.setNodeLimit(2000000);
        lang.setNodeLimit(2000000);
        long timeb = 0;
        long timea = 0;
        try {
          timeb = System.currentTimeMillis();
          //mo.run();
          lang.run();
          timea = System.currentTimeMillis();
          ps.print(mo.getAnalysisResult().getTotalNumberOfStates());
          ps.print(",");
        } catch (final Throwable t) {
          timea = System.currentTimeMillis();
          ps.print(",");
        }
        pt.print(timea - timeb);
        pt.print(",");
      //}
      ps.println();
      pt.println();
    }
    ps.flush();
    pt.flush();
    ps.close();
    pt.close();
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
