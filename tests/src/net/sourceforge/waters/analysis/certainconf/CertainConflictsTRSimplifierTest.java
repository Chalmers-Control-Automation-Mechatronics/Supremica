package net.sourceforge.waters.analysis.certainconf;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifierTest;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;

public class CertainConflictsTRSimplifierTest extends AbstractTRSimplifierTest {

  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(CertainConflictsTRSimplifierTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  @Override
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    return new CertainConflictsTRSimplifier();
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }

  public void test_certainconflicts_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_11() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_12() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_13() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_14() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_14.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_15() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_15.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_16() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_16.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_17() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_17.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_18() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_18.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_tau1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_tau1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_tau2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_tau2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_tau3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_tau3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_tau4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_tau4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_tau5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_tau5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  @Override
  public void test_basic_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_basic7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }
}
