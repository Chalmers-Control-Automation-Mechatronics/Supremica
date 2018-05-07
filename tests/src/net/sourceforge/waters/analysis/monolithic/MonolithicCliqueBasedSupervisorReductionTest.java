package net.sourceforge.waters.analysis.monolithic;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.abstraction.CliqueBasedSupervisorReductionTRSimplifier;
import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

public class MonolithicCliqueBasedSupervisorReductionTest
  extends AbstractSupervisorSynthesizerTest
{

  //#########################################################################
  //# To be Provided by Subclasses
  /**
   * Creates an instance of the synthesiser under test. This method
   * instantiates the class of the synthesiser tested by the particular
   * subclass of this test, and configures it as needed.
   * @param factory
   *          The factory used by the synthesiser to create its output.
   * @return An instance of the synthesiser.
   */
  @Override
  protected SupervisorSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    final MonolithicSynthesizer synthesizer =
      new MonolithicSynthesizer(factory);
    synthesizer.setSupervisorReductionSimplifier(new CliqueBasedSupervisorReductionTRSimplifier());
    synthesizer.setSupervisorLocalizationEnabled(true);
    return synthesizer;
  }


  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(MonolithicCliqueBasedSupervisorReductionTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  @Override
  public void testCatMouseUnsup1() throws Exception
  {
  }

  @Override
  public void testCatMouseUnsup2() throws Exception
  {
  }

  @Override
  public void testTrafficlights() throws Exception
  {

  }

  @Override
  public void testTransferLine2() throws Exception
  {
  }

  @Override
  public void testTransferLine3() throws Exception
  {
  }

  @Override
  public void test2LinkAltBatch() throws Exception
  {
  }

  @Override
  public void testDosingUnit() throws Exception
  {
  }

  @Override
  public void testCell() throws Exception
  {
  }

  @Override
  public void testCellSwitch() throws Exception
  {
  }

  @Override
  public void testIPC() throws Exception
  {
  }

  @Override
  public void testIPCcswitch() throws Exception
  {
  }

  @Override
  public void testIPClswitch() throws Exception
  {
  }

  @Override
  public void testIPCuswicth() throws Exception
  {
  }

  @Override
  public void testTictactoe() throws Exception
  {
  }

  @Override
  public void testCT3() throws Exception
  {
  }

  @Override
  public void testRobotAssemblyCell() throws Exception
  {
  }

  @Override
  public void test2LinkAlt() throws Exception
  {
  }

  @Override
  public void testManufacturingSystem() throws Exception
  {
  }
}
