package net.sourceforge.waters.analysis.composing;

import java.util.List;

import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityDiagnostics;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class ComposingControllabilityChecker
  extends ComposingSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public ComposingControllabilityChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public ComposingControllabilityChecker(final ProductDESProxy model,
                                         final ProductDESProxyFactory factory)
  {
    super(model,
          ControllabilityKindTranslator.getInstance(),
          ControllabilityDiagnostics.getInstance(),
          factory);
    cModel = new ConvertModel();
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.composing.ComposingSafetyVerifier
  @Override
  public ProductDESProxy getConvertedModel()
  {
    cModel = new ConvertModel(getModel(),
                              ControllabilityKindTranslator.getInstance(),
                              getFactory());
    return cModel.run();
  }

  @Override
  public List<EventProxy> convertTrace(final List<EventProxy> trace)
  {
    final EventProxy ne = cModel.getOriginalEvent(trace.get(trace.size()-1));
    trace.remove(trace.size()-1);
    trace.add(ne);
    return trace;
  }


  //#########################################################################
  //# Data Members
  private ConvertModel cModel;

}
