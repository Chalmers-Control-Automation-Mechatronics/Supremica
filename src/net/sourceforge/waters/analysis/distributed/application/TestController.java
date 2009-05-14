package net.sourceforge.waters.analysis.distributed.application;

/**
 * A test controller implementation.
 */
public class TestController extends AbstractController
{
  public TestController()
  {
  }

  protected void executeController() throws Exception
  {
    Thread.sleep(10000);
  }
}