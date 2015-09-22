//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
    final PlainEventListProxy eventList;
    if (marked) {
      final String accepting = EventDeclProxy.DEFAULT_MARKING_NAME;
      final SimpleIdentifierProxy ident =
          factory.createSimpleIdentifierProxy(accepting);
      final List<SimpleIdentifierProxy> list = Collections.singletonList(ident);
      eventList = factory.createPlainEventListProxy(list);
    } else {
      eventList = null;
    }
    mNode = factory.createSimpleNodeProxy(name + "_" + index, eventList, null,
                                          initial, null, null, null);
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
    if(i){
      mEndType = EndType.END;
    }
  }

  public boolean isAccepting()
  {
    return mAccepting;
  }

  public void setAccepting(final boolean acc)
  {
    if (acc) {
      mAccepting = acc;
    }
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
  private boolean mAccepting;

}








