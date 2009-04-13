package net.sourceforge.waters.analysis.distributed;

import java.util.List;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

public class DistributedModelVerifierFactory
  extends AbstractModelVerifierFactory
{
  private DistributedModelVerifierFactory()
  {
  }

  private DistributedModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
  }

  public DistributedControllabilityChecker 
    createControllabilityChecker (final ProductDESProxyFactory factory)
  {
    return new DistributedControllabilityChecker(factory);
  }


  public static DistributedModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new DistributedModelVerifierFactory();
    }
    return theInstance;
  }

  public static DistributedModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new DistributedModelVerifierFactory(cmdline);
  }

  private static DistributedModelVerifierFactory theInstance = null;
}