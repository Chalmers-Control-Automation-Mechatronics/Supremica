//# -*- tab-width: 2  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   IconLoader
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;


public class IconLoader
{

  //#########################################################################
  //# Static Class Methods
  private static final ImageIcon getIcon(final String name)
  {
    final Class<?> cls = IconLoader.class;
    final String resourcename = "/icons/" + name + ".gif";
    final URL resource = cls.getResource(resourcename);
    return new ImageIcon(resource);
  }


  //#########################################################################
  //# Class Constants
  private static final String NAME_AUTOMATON = "waters/automaton16";
  private static final String NAME_CONTROLLABLE = "waters/controllable";
  private static final String NAME_EVENT = "waters/event";
  private static final String NAME_FORBIDDEN = "ForbiddenState16";
  private static final String NAME_INSTANCE = "waters/instance";
  private static final String NAME_PLANT = "waters/plant";
  private static final String NAME_PROPERTY = "waters/property";
  private static final String NAME_PROPOSITION = "waters/proposition";
  private static final String NAME_SIMPLEPARAM = "waters/rangeparam";
  private static final String NAME_SIMULATOR_BACK = "waters/simulator_back";
  private static final String NAME_SIMULATOR_REPLAY = "waters/simulator_replay";
  private static final String NAME_SIMULATOR_RESET = "waters/simulator_reset";
  private static final String NAME_SIMULATOR_STEP = "waters/simulator_step";
  private static final String NAME_SIMULATOR_TO_END =
    "waters/simulator_to_end";
  private static final String NAME_SIMULATOR_TO_START =
    "waters/simulator_to_start";
  private static final String NAME_SPEC = "waters/spec";
  private static final String NAME_SUPERVISOR = "waters/supervisor";
  private static final String NAME_UNCONTROLLABLE = "waters/uncontrollable";
  private static final String NAME_VARIABLE = "waters/variable";
  private static final String NAME_CROSS = "waters/cross16";
  private static final String NAME_TICK = "waters/tick16";
  private static final String NAME_WARNING = "RedFlag";
  private static final String NAME_YELLOWWARNING = "OrangeFlag";


  //#########################################################################
  //# Class Constants
  public static final ImageIcon ICON_AUTOMATON = getIcon(NAME_AUTOMATON);
  public static final ImageIcon ICON_BINDING = null;
  public static final ImageIcon ICON_CONTROLLABLE =
    getIcon(NAME_CONTROLLABLE);
  public static final ImageIcon ICON_EVENT = getIcon(NAME_EVENT);
  public static final ImageIcon ICON_FORBIDDEN = getIcon(NAME_FORBIDDEN);
  public static final ImageIcon ICON_FOREACH = null;
  public static final ImageIcon ICON_INSTANCE = getIcon(NAME_INSTANCE);
  public static final ImageIcon ICON_PLANT = getIcon(NAME_PLANT);
  public static final ImageIcon ICON_PROPERTY = getIcon(NAME_PROPERTY);
  public static final ImageIcon ICON_PROPOSITION = getIcon(NAME_PROPOSITION);
  public static final ImageIcon ICON_SIMPLEPARAM = getIcon(NAME_SIMPLEPARAM);
  public static final ImageIcon ICON_SIMULATOR_BACK =
    getIcon(NAME_SIMULATOR_BACK);
  public static final ImageIcon ICON_SIMULATOR_REPLAY =
    getIcon(NAME_SIMULATOR_REPLAY);
  public static final ImageIcon ICON_SIMULATOR_RESET =
    getIcon(NAME_SIMULATOR_RESET);
  public static final ImageIcon ICON_SIMULATOR_STEP =
    getIcon(NAME_SIMULATOR_STEP);
  public static final ImageIcon ICON_SIMULATOR_TO_END =
    getIcon(NAME_SIMULATOR_TO_END);
  public static final ImageIcon ICON_SIMULATOR_TO_START =
    getIcon(NAME_SIMULATOR_TO_START);
  public static final ImageIcon ICON_SPEC = getIcon(NAME_SPEC);
  public static final ImageIcon ICON_SUPERVISOR = getIcon(NAME_SUPERVISOR);
  public static final ImageIcon ICON_UNCONTROLLABLE =
    getIcon(NAME_UNCONTROLLABLE);
  public static final ImageIcon ICON_VARIABLE = getIcon(NAME_VARIABLE);
  /*public static final ImageIcon ICON_CROSS = getIcon(NAME_CROSS);
  public static final ImageIcon ICON_TICK = getIcon(NAME_TICK);
  public static final ImageIcon ICON_WARNING = getIcon(NAME_WARNING);
  public static final ImageIcon ICON_YELLOWWARNING = getIcon(NAME_YELLOWWARNING);*/
  public static final ImageIcon ICON_TABLE_NORMAL_AUTOMATON = null;
  public static final ImageIcon ICON_TABLE_ENABLED_AUTOMATON = getIcon(NAME_TICK);
  public static final ImageIcon ICON_TABLE_WARNING_PROPERTY = getIcon(NAME_YELLOWWARNING);
  public static final ImageIcon ICON_TABLE_ERROR_AUTOMATON = getIcon(NAME_WARNING);
  public static final ImageIcon ICON_TABLE_DISABLED_PROPERTY = getIcon(NAME_CROSS);
  public static final ImageIcon ICON_EVENTTREE_BLOCKING_EVENT = getIcon(NAME_WARNING);
  public static final ImageIcon ICON_EVENTTREE_INVALID_EVENT = getIcon(NAME_CROSS);
  public static final ImageIcon ICON_EVENTTREE_VALID_EVENT = getIcon(NAME_TICK);
  public static final ImageIcon ICON_EVENTTREE_CAUSES_WARNING_EVENT = getIcon(NAME_YELLOWWARNING);
  public static final ImageIcon ICON_EVENTTREE_DISABLED_AUTOMATON = getIcon(NAME_CROSS);
  public static final ImageIcon ICON_EVENTTREE_ENABLED_AUTOMATON = getIcon(NAME_TICK);
  public static final ImageIcon ICON_EVENTTREE_CAUSES_WARNING_PROPERTY = getIcon(NAME_YELLOWWARNING);
  public static final Icon ICON_EVENTTREE_BLOCKING_AUTOMATON = getIcon(NAME_WARNING);

}
