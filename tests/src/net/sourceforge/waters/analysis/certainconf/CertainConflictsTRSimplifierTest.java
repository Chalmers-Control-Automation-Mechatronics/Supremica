package net.sourceforge.waters.analysis.certainconf;

import net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifierTest;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import junit.framework.Test;
import junit.framework.TestSuite;

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


  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    // TODO Auto-generated method stub
    return new CertainConflictsTRSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
  {
    return createEventEncodingWithPropositions(des, aut);
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

}