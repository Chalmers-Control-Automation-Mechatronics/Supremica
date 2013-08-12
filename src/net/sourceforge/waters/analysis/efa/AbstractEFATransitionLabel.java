//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   AbstractEFATransitionLabel
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleHashCodeVisitor;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public abstract class AbstractEFATransitionLabel {

   private final ConstraintList mConstraint;
   private final EventProxy mEvent;
   private final List<Proxy> mProxyList;
   
   public AbstractEFATransitionLabel(EventProxy event, ConstraintList constraint) {
      mConstraint = constraint;
      mEvent = event;
      mProxyList = new ArrayList<Proxy>();
      mProxyList.add(mEvent);
      mProxyList.addAll(mConstraint.getConstraints());
   }

   public AbstractEFATransitionLabel(EventProxy event) {
     this(event, new ConstraintList());
   }
   
   public EventProxy getEvent(){
      return mEvent;
   }
   
   public ConstraintList getConstraint(){
      return mConstraint;
   }
   
   @Override
   public boolean equals(final Object other) {
      if(other != null && other.getClass() == getClass()){
      final ModuleEqualityVisitor eq =
        ModuleEqualityVisitor.getInstance(false);
        AbstractEFATransitionLabel expected = (AbstractEFATransitionLabel) other;
        ArrayList<Proxy> pList = new ArrayList<Proxy>();
        pList.add(expected.getEvent());
        pList.addAll(expected.getConstraint().getConstraints());
        return eq.isEqualList(mProxyList, pList);
      }
      return false;
   }

   @Override
   public int hashCode() {
    final ModuleHashCodeVisitor hash =
      ModuleHashCodeVisitor.getInstance(false);
    return hash.getListHashCode(this.mProxyList);
   }

}
