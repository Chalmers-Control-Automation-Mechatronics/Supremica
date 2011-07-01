package net.sourceforge.waters.external.promela;

import java.util.Collection;

import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

public class PromelaLabel
{
  private final Collection<SimpleExpressionProxy> mLabelBlock;
  public PromelaLabel(final Collection<SimpleExpressionProxy> label){
    mLabelBlock = label;
  }
  public PromelaLabel()
  {
    // TODO Auto-generated constructor stub
    mLabelBlock = null;
  }
  public Collection<SimpleExpressionProxy> getLabel(){
    return mLabelBlock;
  }
  public Collection<SimpleExpressionProxy> getCloneLabel(final ModuleProxyFactory factory)
  {
    final ModuleProxyCloner cloner = factory.getCloner();
    return cloner.getClonedList(mLabelBlock);
  }
}
