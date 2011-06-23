//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Promela Importer
//# PACKAGE: net.sourceforge.waters.external.promela
//# CLASS:   PromelaNode
//###########################################################################
//# $Id$
//###########################################################################

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

  //#########################################################################
  //# Constructors
  public PromelaNode()
  {
    mEndType = EndType.NONE;
    mGotoLabel = null;
  }

  public PromelaNode(final EndType endtype)
  {
    mEndType = endtype;
    mGotoLabel = null;
  }

  public PromelaNode(final String gotoLabel)
  {
    mEndType = EndType.GOTO;
    mGotoLabel = gotoLabel;
  }

  // TODO Remove this
 /* public PromelaNode(final boolean isBreak,final boolean isEnd,final boolean isGoto){

  }
*/

  //#########################################################################
  //# Simple Access
  public EndType getEndType()
  {
    return mEndType;
  }

  public String getGotoLabel()
  {
    return mGotoLabel;
  }


  //#########################################################################
  //# Waters Graph Creation
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
    return mEndType == EndType.BREAK;
  }
  public boolean isEnd(){
    return mEndType == EndType.END;
  }
  public boolean isGoto(){
    return mEndType == EndType.GOTO;
  }
  public void setBreak(final boolean i){
    mEndType = EndType.BREAK;
  }
  public void setEnd(final boolean i){
    mEndType = EndType.END;
  }


  //#########################################################################
  //# Inner Enumeration EndType
  public enum EndType {
    NONE,
    END,
    BREAK,
    GOTO;
  }


  //#########################################################################
  //# Data Members
  private EndType mEndType;
  private final String mGotoLabel;
  private SimpleNodeProxy mNode;


}
