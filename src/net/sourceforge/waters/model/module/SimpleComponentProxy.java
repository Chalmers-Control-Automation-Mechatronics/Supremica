//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.decl
//# CLASS:   SimpleComponentProxy
//###########################################################################
//# $Id: SimpleComponentProxy.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * <P>A component representing a single finite-state machine.</P>
 *
 * <P>Simple components are the basic way of representing the <I>plants</I>,
 * <I>specifications</I>, and <I>properties</I> in a Waters module.
 * Each simple component contains of the following information.</P>
 *
 * <DL>
 * <DT><I>Name.</I></DT>
 * <DD>The name uniquely identifies the component in its module.
 * It is of type {@link IdentifierProxy}, to support structured names
 * as they may occur in parameterised structures.</DD>
 * <DT><I>Kind.</I></DT>
 * <DD>The kind of a component identifies it as a <I>plants</I>,
 * <I>specification</I>, or <I>properties</I>. It uses the enumerative
 * type {@link ComponentKind}.</DD>
 * <DT><I>Graph.</I></DT>
 * <DD>The graph shows the states and transitions of the finite-state
 * machine representing the component. It is of type {@link GraphProxy}.</DD>
 * </DL>
 *
 * @author Robi Malik
 */

public interface SimpleComponentProxy extends ComponentProxy {


  //#########################################################################
  //# Getters and Setters
  public ComponentKind getKind();

  public GraphProxy getGraph();

}
