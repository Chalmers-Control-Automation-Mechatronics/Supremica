package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.IdentifierProxy;

public class PromelaLabel
{
  private final Collection<Proxy> mLabelBlock;
  public PromelaLabel(final IdentifierProxy ident){
    mLabelBlock = new ArrayList<Proxy>();
    mLabelBlock.add(ident);
  }
  public Collection<Proxy> getLabel(){
    return mLabelBlock;
  }
}
