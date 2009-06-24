package net.sourceforge.waters.analysis.distributed;

import java.util.List;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.analysis.CommandLineArgumentInteger;
import net.sourceforge.waters.model.analysis.CommandLineArgumentString;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;

/**
 * Factory to create distributed model verifiers.
 */
public class DistributedModelVerifierFactory
  extends AbstractModelVerifierFactory
{
  //####################################################################
  // The current implementation makes a cast to
  // DistributedSafetyVerifier when processing command line arguments. 
  // An interface should be created for any distributed model verifiers
  // that allow the host and port parameters to be specified.
  //####################################################################


  private DistributedModelVerifierFactory()
  {
  }

  private DistributedModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
    addArgument(new HostArgument());
    addArgument(new PortArgument());
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


  /**
   * Process the host-name command line argument
   */
  private static class HostArgument extends CommandLineArgumentString
  {
    private HostArgument()
    {
      super("-host",
	    "Server to submit job to");
    }

    protected void configure(final ModelVerifier verifier)
    {
      DistributedSafetyVerifier dsv = 
	(DistributedSafetyVerifier) verifier;

      String value = getValue();
      dsv.setHostname(value);
    }
  }

  private static class PortArgument extends CommandLineArgumentInteger
  {
    private PortArgument()
    {
      super("-port",
	    "Port to connect to the server with");
    }

    protected void configure(final ModelVerifier verifier)
    {
      DistributedSafetyVerifier dsv = 
	(DistributedSafetyVerifier) verifier;

      int value = getValue();
      dsv.setPort(value);
    }
  }

  private static DistributedModelVerifierFactory theInstance = null;
}