package net.sourceforge.waters.analysis.composing;

import java.util.List;

import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;



public class ComposeControllabilityChecker
  extends ComposeSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public ComposeControllabilityChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public ComposeControllabilityChecker(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory)
  {
    super(model, ControllabilityKindTranslator.getInstance(), factory);
    cModel = new ConvertModel();
  }
  
  public ProductDESProxy getConvertedModel() {
    final ConvertModel convertModel = new ConvertModel(getModel(), 
                                                       ControllabilityKindTranslator.getInstance(), 
                                                       getFactory());
    ProductDESProxy desConverted = convertModel.run();
    setCmodel(convertModel);
    return desConverted;
  }
  
  public List<EventProxy> convertTrace(List<EventProxy> trace) {
    EventProxy ne = getCmodel().getOriginalEvent(trace.get(trace.size()-1)); 
    trace.remove(trace.size()-1);
    trace.add(ne);
    return trace;
  }
  
  private void setCmodel(ConvertModel cm) {
    cModel = cm;
  }
  
  private ConvertModel getCmodel() {
    return cModel;
  }

  private ConvertModel cModel;
}
