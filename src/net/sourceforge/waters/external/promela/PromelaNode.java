package net.sourceforge.waters.external.promela;

import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

public class PromelaNode
{
  //private final String eName;

  public PromelaNode(){

  }
 public PromelaNode(final boolean isBreak,final boolean isEnd,final boolean isGoto){
   mBreak = isBreak;
   mEnd = isEnd;
   mGoto = isGoto;
 }
  public SimpleNodeProxy createNode(final String name, final int index,
                                    final boolean initial,
                                    final boolean marked,
                                    final ModuleProxyFactory factory)
  {
    if (marked) {
      final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;
      final SimpleIdentifierProxy ident =
          factory.createSimpleIdentifierProxy(accepting);
      final List<SimpleIdentifierProxy> list = Collections.singletonList(ident);
      final PlainEventListProxy eventList =
          factory.createPlainEventListProxy(list);
      mNode =
          factory.createSimpleNodeProxy(name + "_" + index, eventList, initial,
                                        null, null, null);
      return mNode;
    } else {
      mNode =
          factory.createSimpleNodeProxy(name + "_" + index, null, initial,
                                        null, null, null);
    }
    return mNode;
  }

  public SimpleNodeProxy getNode(){
    return mNode;
  }
  public boolean isBreak(){
    return mBreak;
  }
  public boolean isEnd(){
    return mEnd;
  }
  public boolean isGoto(){
    return mGoto;
  }
  public void setBreak(final boolean i){
    mBreak = i;
  }
  public void setEnd(final boolean i){
    mEnd = i;
  }
  private SimpleNodeProxy mNode;
  private boolean mBreak,mEnd,mGoto;
}
