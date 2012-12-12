//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Simulator
//# PACKAGE: net.sourceforge.waters.gui.simulator
//# CLASS:   AutomatonStatus
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.simulator;

import javax.swing.ImageIcon;

import net.sourceforge.waters.gui.util.IconLoader;

/**
 * An enumeration representing the different status values an event
 * may have in each simulation step.
 *
 * @author Robi Malik
 */

public enum EventStatus
{

  //#########################################################################
  //# Enumeration Values
  /**
   * Status to indicate that the event is enabled in the current step.
   */
  DISABLED(IconLoader.ICON_EVENTTREE_INVALID_EVENT),
  /**
   * Status to indicate that the event is disabled in the current step.
   */
  ENABLED(IconLoader.ICON_EVENTTREE_VALID_EVENT),
  /**
   * Status to indicate that the event is enabled in the model but disabled in
   * some property automaton. The language inclusion check fails in this step.
   */
  WARNING(IconLoader.ICON_EVENTTREE_CAUSES_WARNING_EVENT),
  /**
   * Status to indicate this is an uncontrollable event enabled in the plant but
   * disabled in some specification. The controllability check fails in this
   * step.
   */
  ERROR(IconLoader.ICON_EVENTTREE_BLOCKING_EVENT);


  //#########################################################################
  //# Constructor
  private EventStatus(final ImageIcon icon)
  {
    mIcon = icon;
  }


  //#########################################################################
  //# Simple Access
  ImageIcon getIcon()
  {
    return mIcon;
  }

  public boolean canBeFired()
  {
    return this == ENABLED || this == WARNING;
  }


  //#########################################################################
  //# Data Members
  private ImageIcon mIcon;

}
