//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.analysis.coobs.CoobservabilityAttributeFactory;
import net.sourceforge.waters.analysis.coobs.CoobservabilitySignature;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.CoobservabilityCounterExampleProxy;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * <P>A model verifier that checks whether a system of composed automata
 * is <I>coobservable</I>.</P>
 *
 * <P>A coobservability checker determines the controllability and
 * observability status of events using the attributes prefixes
 * {@link CoobservabilityAttributeFactory#CONTROLLABITY_KEY} and
 * {@link CoobservabilityAttributeFactory#OBSERVABITY_KEY} defined in class
 * {@link CoobservabilityAttributeFactory}. Any attribute whose name starts
 * with one of these prefixes carries the name of a site or supervisor that
 * can disable or observe all instances of the event.</P>
 *
 * <P>For controllable and observable events without these attributes
 * declared, the coobservability checker may be configured to assign
 * as default supervisor site using {@link #setDefaultSiteName(String)
 * setDefaultSite()}. Using the default site, coobservability is equivalent to
 * controllability for models without any supervisor site attributes.</P>
 *
 * <P>As usual, a {@link KindTranslator} can be set to transform the
 * controllability status of events as well as component kinds.
 * Alternatively, the relationship between events and supervisor sites
 * can be defined by specifying a {@link CoobservabilitySignature}.</P>
 *
 * <P>A model is coobservable with respect to a group of supervisor sites if it
 * satisfies the following controllability condition: If in the synchronous
 * composition of all plants and specifications a state can be reached by some
 * trace&nbsp;<I>t</I> and in that state an event&nbsp;<I>e</I> is enabled
 * by all plant components and disabled by some specification component,
 * then there is a supervisor site&nbsp;<I>S</I> that can disable the
 * event&nbsp;<I>e</I> and that has sufficient observation capability to make
 * this disablement, i.e., any trace accepted by the system that is
 * indistinguishable from&nbsp;<I>t</I> by&nbsp;<I>S</I> leads to a state where
 * <I>e</I> is disabled by at least one specification component.</P>
 *
 * @see CoobservabilityAttributeFactory
 *
 * @author Robi Malik
 */

public interface CoobservabilityChecker extends ModelVerifier
{

  //#########################################################################
  //# Configuration
  /**
   * <P>Sets the name of the default supervisor site.</P>
   * <P>If specified, controllable or observable events with no explicit
   * supervisor declared through attributes are assigned to be controlled or
   * observed by the default supervisor site.</P>
   * <P>The default supervisor site is enabled by default, using the name
   * {@link CoobservabilityAttributeFactory#DEFAULT_SITE_NAME}.</P>
   * @param  name  Name of default supervisor, or <CODE>null</CODE> or
   *               empty string if no default supervisor is used.
   * @see AbstractModelAnalyzerFactory#OPTION_CoobservabilityChecker_DefaultSite
   */
  public void setDefaultSiteName(final String name);

  /**
   * Gets the name of the default supervisor site.
   * @return Name of default supervisor, or empty string if no default
   *         supervisor is used.
   * @see #setDefaultSiteName(String) setDefaultSite()
   */
  public String getDefaultSiteName();

  /**
   * Sets a coobservability signature to determine which events are controlled
   * by which supervisor site. If not specified, a signature is obtained from
   * the event kind and attribute information in the model. If specified, the
   * signature replaces the information in the model. The signature object may
   * be modified while running the coobservability check, so the caller should
   * not assume that it retains its contents afterwards.
   */
  public void setSignature(CoobservabilitySignature sig);


  //#########################################################################
  //# More Specific Access to the Results
  /**
   * Gets a counterexample if the model was found to be not coobservable.
   * A coobservability counterexample consists of a first trace representing the
   * system behaviour followed by further traces representing supervisor sites.
   * The system behaviour trace takes the plant and specification
   * to a state where some event is enabled by the plant but disabled by the
   * specification, with that last event included in the trace. Each of the
   * supervisor site traces is labelled by the name of a supervisor (through
   * {@link TraceProxy#getName()}) that can disable the last event of the system
   * trace and contains a sequence of events that is indistinguishable from the
   * system behaviour trace based on the event observability of its supervisor
   * site. It takes the specification to a state where the last event of the
   * system behaviour trace (also included in the trace) is enabled.
   * @return A counterexample object constructed for the input product DES
   *         of this coobservability checker that shares its automata and
   *         event objects.
   * @throws IllegalStateException if this method is called before
   *         model checking has completed, i.e., before {@link #run()}
   *         has been called, or model checking has found that the
   *         property is satisfied and there is no counterexample.
   */
  @Override
  public CoobservabilityCounterExampleProxy getCounterExample();

}
