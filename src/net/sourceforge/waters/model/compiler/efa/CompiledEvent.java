//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   CompiledEvent
//###########################################################################
//# $Id: CompiledEvent.java,v 1.1 2008-06-29 04:01:44 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.Collection;

import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;


class CompiledEvent {

  //#########################################################################
  //# Constructors
  CompiledEvent(final EventDeclProxy decl)
  {
    mEventDecl = decl;
    mVariables = new ProxyAccessorHashMapByContents<IdentifierProxy>();
  }


  //#########################################################################
  //# Simple Access
  EventDeclProxy getEventDecl()
  {
    return mEventDecl;
  }

  void addVariable(final IdentifierProxy ident)
  {
    mVariables.addProxy(ident);
  }

  void addVariables(final Collection<? extends IdentifierProxy> idents)
  {
    mVariables.addAll(idents);
  }


  //#########################################################################
  //# Data Members
  private final EventDeclProxy mEventDecl;
  private final ProxyAccessorMap<IdentifierProxy> mVariables;

}
