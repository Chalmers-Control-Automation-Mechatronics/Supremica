//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   ListBufferTransitionRelationTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractAutomatonBuilder;
import net.sourceforge.waters.model.analysis.AbstractAutomatonBuilderTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AutomatonBuilder;
import net.sourceforge.waters.model.analysis.IsomorphismChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.module.ParameterBindingProxy;

/**
 * @author Robi Malik
 */
public class ListBufferTransitionRelationTest extends
    AbstractAutomatonBuilderTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(ListBufferTransitionRelationTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void test_alpharemoval_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_1.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_2.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_3.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_4.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_5.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_6.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_7.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_8.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_9.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_10.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_11() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_11.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_12() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_12.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_13() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_13.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_14() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_14.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_15() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_15.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_16() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_16.wmod";
    runAutomatonBuilder(group, subdir, name);
  }

  public void test_alpharemoval_17() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_17.wmod";
    runAutomatonBuilder(group, subdir, name);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAutomatonBuilderTest
  protected AutomatonBuilder createAutomatonBuilder
    (final ProductDESProxyFactory factory)
  {
    return new ListBufferAutomatonBuilder(factory);
  }

  protected String getExpectedName(final String desname)
  {
    return desname;
  }


  protected void runAutomatonBuilder(final File dir,
                                     final String name)
    throws Exception
  {
    runAutomatonBuilder(dir, name, (List<ParameterBindingProxy>) null);
  }

  protected void runAutomatonBuilder(final File dir, final String name,
                                     final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final File filename = new File(dir, name);
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    runAutomatonBuilder(des, bindings);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void runAutomatonBuilder(final ProductDESProxy des,
                                   final List<ParameterBindingProxy> bindings)
    throws WatersMarshalException, IOException, AnalysisException
  {
    String desName = des.getName();
    if (bindings != null) {
      desName = appendSuffixes(desName, bindings);
    }
    final Collection<AutomatonProxy> automata = des.getAutomata();
    if (automata.size() == 1) {
      final AutomatonProxy aut = automata.iterator().next();
      runAutomatonBuilder(aut, desName);
    } else {
      for (final AutomatonProxy aut : automata) {
        final String name = desName + '-' + aut.getName();
        runAutomatonBuilder(aut, name);
      }
    }
  }

  private void runAutomatonBuilder(final AutomatonProxy aut, final String name)
    throws WatersMarshalException, IOException, AnalysisException
  {
    configureAutomatonBuilder(aut);
    final AutomatonBuilder builder = getAutomatonBuilder();
    builder.run();
    final AutomatonProxy result = builder.getComputedAutomaton();
    checkResult(result, aut, name);
  }

  private void checkResult(final AutomatonProxy aut,
                           final AutomatonProxy expected,
                           final String name)
    throws WatersMarshalException, IOException, AnalysisException
  {
    final String comment = "Test output from " +
      ProxyTools.getShortClassName(this) + '.';
    saveAutomaton(aut, name, comment);
    final IsomorphismChecker checker = getIsomorphismChecker();
    checker.checkIsomorphism(aut, expected);
  }


  //#########################################################################
  //# Inner Class ListBufferAutomatonBuilder
  private static class ListBufferAutomatonBuilder
    extends AbstractAutomatonBuilder
  {

    //#######################################################################
    //# Constructors
    public ListBufferAutomatonBuilder(final ProductDESProxyFactory factory)
    {
      super(factory);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
    public boolean run()
      throws AnalysisException
    {
      final AutomatonProxy aut = getInputAutomaton();
      final EventEncoding enc = new EventEncoding(aut);
      final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
        (aut, enc, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      rel.setName("output");
      final ProductDESProxyFactory factory = getFactory();
      final AutomatonProxy result = rel.createAutomaton(factory, enc);
      return setAutomatonResult(result);
    }

  }

}
