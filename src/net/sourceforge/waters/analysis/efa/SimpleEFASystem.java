//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFASystem
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.List;

/**
 * An implementation of the {@link AbstractEFASystem}.
 * <p/>
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFASystem
 extends AbstractEFASystem<SimpleEFATransitionLabel, 
                           SimpleEFAVariable, 
                           SimpleEFAComponent, 
                           SimpleEFAVariableContext>
{

  public SimpleEFASystem(final String name,
                         final SimpleEFAVariableContext context,
                         final int size)
  {
    super(name, context, size);
    mAlphabet = new THashSet<SimpleEFAEventDecl>();
  }

  public SimpleEFASystem(final String name,
                         final List<SimpleEFAVariable> variables,
                         final List<SimpleEFAComponent> components,
                         final SimpleEFAVariableContext context)
  {
    super(name, variables, components, context);
    mAlphabet = new THashSet<SimpleEFAEventDecl>();
  }

  public int getNbrComponents()
  {
    return super.getTransitionRelations().size();
  }

  public List<SimpleEFAComponent> getComponents()
  {
    return super.getTransitionRelations();
  }

  public void addComponent(final SimpleEFAComponent component)
  {
    super.addTransitionRelation(component);
  }

  protected void removeComponent(final SimpleEFAComponent component)
  {
    super.removeTransitionRelation(component);
  }  
  
  public Collection<SimpleEFAEventDecl> getSystemEvents()
  {
    return mAlphabet;
  }

  public void AddSystemEvent(final SimpleEFAEventDecl event)
  {
    mAlphabet.add(event);
  }

  public void AddSystemEvents(final Collection<SimpleEFAEventDecl> events)
  {
    mAlphabet.addAll(events);
  }
  
  public void clearSystemEvents(){
    mAlphabet.clear();
  }
  
  public boolean removeSystemEvent(SimpleEFAEventDecl event){
    return mAlphabet.remove(event);
  }

  //#########################################################################
  //# Data Members
  private final Collection<SimpleEFAEventDecl> mAlphabet;  
}
