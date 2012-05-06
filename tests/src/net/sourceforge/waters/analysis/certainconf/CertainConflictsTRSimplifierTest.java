package net.sourceforge.waters.analysis.certainconf;

import net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifierTest;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
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

}