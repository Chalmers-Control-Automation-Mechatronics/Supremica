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

import net.sourceforge.waters.gui.IconLoader;

/**
 * An enumeration representing the different status values an automaton
 * may have in each simulation step.
 *
 * @author Robi Malik
 */

enum AutomatonStatus
{

  /**
   * Status to indicate a property automaton that has been disabled because
   * of a failure in an earlier step.
   */
  DISABLED(IconLoader.ICON_TABLE_DISABLED_PROPERTY, "has been disabled"),
  /**
   * Status to indicate that the state of an automaton is unchanged
   * from the previous step, with the current event not contained in
   * the automaton alphabet.
   */
  IGNORED(null, null),
  /**
   * Status to indicate that the state of an automaton is unchanged
   * from the previous step, with the current event being an explicit
   * selfloop on that state.
   */
  SELFLOOPED(IconLoader.ICON_TABLE_ENABLED_AUTOMATON,
             "contains a selfloop that has just been fired"),
  /**
   * Status to indicate that the state of the automaton is correctly
   * changed from the previous step.
   */
  OK(IconLoader.ICON_TABLE_ENABLED_AUTOMATON,
     "contains a transition that has just been fired"),
  /**
   * Status to indicate an invalid successor state in a property automaton.
   * The language inclusion check fails in this step.
   */
  WARNING(IconLoader.ICON_TABLE_WARNING_PROPERTY,
          "contains a language inclusion problem"),
  /**
   * Status to indicate an invalid successor state in a specification with
   * an uncontrollable event. The controllability check fails in this step.
   */
  ERROR(IconLoader.ICON_TABLE_ERROR_AUTOMATON,
        "contains a controllability problem");


  //#########################################################################
  //# Constructor
  private AutomatonStatus(final ImageIcon icon, final String text)
  {
    mIcon = icon;
    mText = text;
  }


  //#########################################################################
  //# Enumeration Values
  ImageIcon getIcon()
  {
    return mIcon;
  }

  String getText()
  {
    return mText;
  }


  //#########################################################################
  //# Data Members
  private ImageIcon mIcon;
  private String mText;

}
