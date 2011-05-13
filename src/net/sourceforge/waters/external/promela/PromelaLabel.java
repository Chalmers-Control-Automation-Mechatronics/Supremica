package net.sourceforge.waters.external.promela;

import java.util.Collection;

import net.sourceforge.waters.model.base.Proxy;

public class PromelaLabel
{
  private final Collection<Proxy> mLabelBlock;
  public PromelaLabel(final Collection<Proxy> label){
    mLabelBlock = label;
  }
  public Collection<Proxy> getLabel(){
    return mLabelBlock;
  }
}
