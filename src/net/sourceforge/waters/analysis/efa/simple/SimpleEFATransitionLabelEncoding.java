//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFATransitionLabelEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionLabelEncoding;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;

/**
 * An implementation of {@link AbstractEFATransitionLabelEncoding}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFATransitionLabelEncoding
 extends AbstractEFATransitionLabelEncoding<SimpleEFATransitionLabel>
{

  public SimpleEFATransitionLabelEncoding()
  {
    this(AbstractEFATransitionLabelEncoding.DEFAULT_SIZE);
  }

  public SimpleEFATransitionLabelEncoding(final int size)
  {
    super(size);
    final SimpleEFAHelper helper = new SimpleEFAHelper();
    final SimpleEFAEventDecl event = new SimpleEFAEventDecl(helper.getTAUDecl());
    final SimpleEFATransitionLabel label =
     new SimpleEFATransitionLabel(event, ConstraintList.TRUE);
    createTransitionLabelId(label);
  }

  public SimpleEFATransitionLabelEncoding(
   final SimpleEFATransitionLabelEncoding encoding)
  {
    super(encoding);
  }


  @Override
  public List<SimpleEFATransitionLabel> getTransitionLabelsExceptTau()
  {
    return super.getTransitionLabelsExceptTau();
  }

  @Override
  public List<SimpleEFATransitionLabel> getTransitionLabelsIncludingTau()
  {
    return super.getTransitionLabelsIncludingTau();
  }

  public List<SimpleEFAEventDecl> getSimpleEFAEventDeclsExceptTau()
  {
    final List<SimpleEFAEventDecl> events = new ArrayList<>();
    for (int e = 1; e < size(); e++){
      events.add(getTransitionLabel(e).getEvent());
    }
    return events;
  }

  public List<SimpleEFAEventDecl> getSimpleEFAEventDeclsIncludingTau()
  {
    final List<SimpleEFAEventDecl> events = new ArrayList<>();
    for (int e = 0; e < size(); e++){
      events.add(getTransitionLabel(e).getEvent());
    }
    return events;
  }

  public int[] getTransitionLabelIdByEvent(final SimpleEFAEventDecl e)
  {
    final TIntHashSet events = new TIntHashSet();
    for (final SimpleEFATransitionLabel label :
         getTransitionLabelsIncludingTau()) {
      if (label.getEvent().equals(e)) {
        events.add(getTransitionLabelId(label));
      }
    }
    return events.toArray();
  }

  public TIntObjectHashMap<SimpleEFAEventDecl> getTranLabelToEventMap(){
    final TIntObjectHashMap<SimpleEFAEventDecl> map = new TIntObjectHashMap<>(size());
    for (int id = 1; id < size(); id++){
      map.put(id, getTransitionLabel(id).getEvent());
    }
    return map;
  }

  public TIntObjectHashMap<ConstraintList> getTranLabelToConstraintMap(){
    final TIntObjectHashMap<ConstraintList> map = new TIntObjectHashMap<>(size());
    for (int id = 1; id < size(); id++){
      map.put(id, getTransitionLabel(id).getConstraint());
    }
    return map;
  }

  @Override
  public int size()
  {
    return super.size();
  }
  @Override
  public String toString(){
    if (super.isEmpty()){
      return "[]";
    }
    final StringBuilder events = new StringBuilder();
    final String sep = " <> ";
    events.append("[");
    for (final SimpleEFATransitionLabel label :
         getTransitionLabelsIncludingTau()){
      final String out = Integer.toString(getTransitionLabelId(label))
                   + " -> "
                   + label.toString()
                   + sep;
      events.append(out);
    }
    events.delete(events.length() - sep.length(), events.length());
    events.append("]");
    return events.toString();
  }

}
