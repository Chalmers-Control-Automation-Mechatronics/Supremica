//# -*- tab-width: 2  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   IconLoader
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.util;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.supremica.properties.Config;


public class IconLoader
{

  //#########################################################################
  //# Static Class Methods
  private static final ImageIcon getWatersIcon(final String name)
  {
    final Class<?> cls = IconLoader.class;
    final String subdir = Config.GUI_EDITOR_ICONSET.getAsString();
    final String prefix = "/icons/" + subdir + "/" + name;
    final URL pngResource = cls.getResource(prefix + ".png");
    if (pngResource != null) {
      return new ImageIcon(pngResource);
    }
    final URL gifResource = cls.getResource(prefix + ".gif");
    if (gifResource != null) {
      return new ImageIcon(gifResource);
    }
    return null;
  }

  private static final ImageIcon getSupremicaIcon(final String name)
  {
    final Class<?> cls = IconLoader.class;
    final String resourcename = "/icons/" + name + ".gif";
    final URL resource = cls.getResource(resourcename);
    return new ImageIcon(resource);
  }


  //#########################################################################
  //# Class Constants
  private static final String NAME_BINDING = "binding";
  private static final String NAME_CONSOLE_DEBUG = "debug";
  private static final String NAME_CONSOLE_ERROR = "error";
  private static final String NAME_CONSOLE_INFO = "info";
  private static final String NAME_CONSOLE_WARNING = "warning";
  private static final String NAME_CONSTANT = "constant";
  private static final String NAME_CONTROLLABLE_OBSERVABLE =
    "controllable";
  private static final String NAME_CONTROLLABLE_UNOBSERVABLE =
    "controllable_unobservable";
  private static final String NAME_EVENT = "event";
  private static final String NAME_EVENT_ALIAS = "event_alias";
  private static final String NAME_FOREACH = "foreach";
  private static final String NAME_FORBIDDEN = "ForbiddenState16";
  private static final String NAME_INSTANCE = "instance";
  private static final String NAME_NEW_AUTOMATON = "new_automaton";
  private static final String NAME_NEW_BINDING = "new_binding";
  private static final String NAME_NEW_CONSTANT = "new_constant";
  private static final String NAME_NEW_EVENT = "new_event";
  private static final String NAME_NEW_EVENT_ALIAS = "new_event_alias";
  private static final String NAME_NEW_FOREACH = "new_foreach";
  private static final String NAME_NEW_INSTANCE = "new_instance";
  private static final String NAME_NEW_VARIABLE = "new_variable";
  private static final String NAME_PLANT = "plant";
  private static final String NAME_PROPERTY = "property";
  private static final String NAME_PROPOSITION = "waters/proposition";
  private static final String NAME_SIMULATOR_BACK = "waters/simulator_back";
  private static final String NAME_SIMULATOR_REPLAY = "waters/simulator_replay";
  private static final String NAME_SIMULATOR_RESET = "waters/simulator_reset";
  private static final String NAME_SIMULATOR_STEP = "waters/simulator_step";
  private static final String NAME_SIMULATOR_TO_END =
    "waters/simulator_to_end";
  private static final String NAME_SIMULATOR_TO_START =
    "waters/simulator_to_start";
  private static final String NAME_SPEC = "specification";
  private static final String NAME_SUPERVISOR = "supervisor";
  private static final String NAME_TOOL_ABOUT = "about";
  private static final String NAME_TOOL_COPY = "copy";
  private static final String NAME_TOOL_CUT = "cut";
  private static final String NAME_TOOL_DELETE = "delete";
  private static final String NAME_TOOL_EDGE = "edge";
  private static final String NAME_TOOL_EXIT = "exit";
  private static final String NAME_TOOL_GROUP_NODE = "group_node";
  private static final String NAME_TOOL_IMPORT = "import";
  private static final String NAME_TOOL_NEW = "new";
  private static final String NAME_TOOL_NODE = "node";
  private static final String NAME_TOOL_OPEN = "open";
  private static final String NAME_TOOL_PASTE = "paste";
  private static final String NAME_TOOL_PRINT = "print";
  private static final String NAME_TOOL_REDO = "redo";
  private static final String NAME_TOOL_SAVE = "save";
  private static final String NAME_TOOL_SAVE_AS = "save_as";
  private static final String NAME_TOOL_SELECT = "select";
  private static final String NAME_TOOL_UNDO = "undo";
  private static final String NAME_UNCONTROLLABLE_OBSERVABLE =
    "uncontrollable";
  private static final String NAME_UNCONTROLLABLE_UNOBSERVABLE =
    "uncontrollable_unobservable";
  private static final String NAME_VARIABLE = "variable";
  private static final String NAME_CROSS = "waters/cross16";
  private static final String NAME_TICK = "waters/tick16";


  //#########################################################################
  //# Class Constants
  // Editor
  public static final ImageIcon ICON_CONSTANT = getWatersIcon(NAME_CONSTANT);
  public static final ImageIcon ICON_CONSOLE_DEBUG = getWatersIcon(NAME_CONSOLE_DEBUG);
  public static final ImageIcon ICON_CONSOLE_ERROR = getWatersIcon(NAME_CONSOLE_ERROR);
  public static final ImageIcon ICON_CONSOLE_INFO = getWatersIcon(NAME_CONSOLE_INFO);
  public static final ImageIcon ICON_CONSOLE_WARNING = getWatersIcon(NAME_CONSOLE_WARNING);
  public static final ImageIcon ICON_BINDING = getWatersIcon(NAME_BINDING);
  public static final ImageIcon ICON_CONTROLLABLE_OBSERVABLE =
    getWatersIcon(NAME_CONTROLLABLE_OBSERVABLE);
  public static final ImageIcon ICON_CONTROLLABLE_UNOBSERVABLE =
    getWatersIcon(NAME_CONTROLLABLE_UNOBSERVABLE);
  public static final ImageIcon ICON_EVENT = getWatersIcon(NAME_EVENT);
  public static final ImageIcon ICON_EVENT_ALIAS = getWatersIcon(NAME_EVENT_ALIAS);
  public static final ImageIcon ICON_FORBIDDEN = getSupremicaIcon(NAME_FORBIDDEN);
  public static final ImageIcon ICON_FOREACH = getWatersIcon(NAME_FOREACH);
  public static final ImageIcon ICON_INSTANCE = getWatersIcon(NAME_INSTANCE);
  public static final ImageIcon ICON_NEW_AUTOMATON =
    getWatersIcon(NAME_NEW_AUTOMATON);
  public static final ImageIcon ICON_NEW_BINDING = getWatersIcon(NAME_NEW_BINDING);
  public static final ImageIcon ICON_NEW_CONSTANT = getWatersIcon(NAME_NEW_CONSTANT);
  public static final ImageIcon ICON_NEW_EVENT = getWatersIcon(NAME_NEW_EVENT);
  public static final ImageIcon ICON_NEW_EVENT_ALIAS = getWatersIcon(NAME_NEW_EVENT_ALIAS);
  public static final ImageIcon ICON_NEW_FOREACH = getWatersIcon(NAME_NEW_FOREACH);
  public static final ImageIcon ICON_NEW_INSTANCE = getWatersIcon(NAME_NEW_INSTANCE);
  public static final ImageIcon ICON_NEW_VARIABLE = getWatersIcon(NAME_NEW_VARIABLE);
  public static final ImageIcon ICON_PLANT = getWatersIcon(NAME_PLANT);
  public static final ImageIcon ICON_PROPERTY = getWatersIcon(NAME_PROPERTY);
  // TODO Replace this by computed icon:
  public static final ImageIcon ICON_PROPOSITION = getSupremicaIcon(NAME_PROPOSITION);
  public static final ImageIcon ICON_SPEC = getWatersIcon(NAME_SPEC);
  public static final ImageIcon ICON_SUPERVISOR = getWatersIcon(NAME_SUPERVISOR);
  public static final ImageIcon ICON_TOOL_ABOUT = getWatersIcon(NAME_TOOL_ABOUT);
  public static final ImageIcon ICON_TOOL_COPY = getWatersIcon(NAME_TOOL_COPY);
  public static final ImageIcon ICON_TOOL_CUT = getWatersIcon(NAME_TOOL_CUT);
  public static final ImageIcon ICON_TOOL_DELETE = getWatersIcon(NAME_TOOL_DELETE);
  public static final ImageIcon ICON_TOOL_EDGE = getWatersIcon(NAME_TOOL_EDGE);
  public static final ImageIcon ICON_TOOL_EXIT = getWatersIcon(NAME_TOOL_EXIT);
  public static final ImageIcon ICON_TOOL_GROUP_NODE = getWatersIcon(NAME_TOOL_GROUP_NODE);
  public static final ImageIcon ICON_TOOL_IMPORT = getWatersIcon(NAME_TOOL_IMPORT);
  public static final ImageIcon ICON_TOOL_NEW = getWatersIcon(NAME_TOOL_NEW);
  public static final ImageIcon ICON_TOOL_NODE = getWatersIcon(NAME_TOOL_NODE);
  public static final ImageIcon ICON_TOOL_OPEN = getWatersIcon(NAME_TOOL_OPEN);
  public static final ImageIcon ICON_TOOL_PASTE = getWatersIcon(NAME_TOOL_PASTE);
  public static final ImageIcon ICON_TOOL_PRINT = getWatersIcon(NAME_TOOL_PRINT);
  public static final ImageIcon ICON_TOOL_REDO = getWatersIcon(NAME_TOOL_REDO);
  public static final ImageIcon ICON_TOOL_SAVE = getWatersIcon(NAME_TOOL_SAVE);
  public static final ImageIcon ICON_TOOL_SAVE_AS = getWatersIcon(NAME_TOOL_SAVE_AS);
  public static final ImageIcon ICON_TOOL_SELECT = getWatersIcon(NAME_TOOL_SELECT);
  public static final ImageIcon ICON_TOOL_UNDO = getWatersIcon(NAME_TOOL_UNDO);
  public static final ImageIcon ICON_UNCONTROLLABLE_OBSERVABLE =
    getWatersIcon(NAME_UNCONTROLLABLE_OBSERVABLE);
  public static final ImageIcon ICON_UNCONTROLLABLE_UNOBSERVABLE =
    getWatersIcon(NAME_UNCONTROLLABLE_UNOBSERVABLE);
  public static final ImageIcon ICON_VARIABLE = getWatersIcon(NAME_VARIABLE);

  // Simulator
  public static final ImageIcon ICON_SIMULATOR_BACK =
    getSupremicaIcon(NAME_SIMULATOR_BACK);
  public static final ImageIcon ICON_SIMULATOR_REPLAY =
    getSupremicaIcon(NAME_SIMULATOR_REPLAY);
  public static final ImageIcon ICON_SIMULATOR_RESET =
    getSupremicaIcon(NAME_SIMULATOR_RESET);
  public static final ImageIcon ICON_SIMULATOR_STEP =
    getSupremicaIcon(NAME_SIMULATOR_STEP);
  public static final ImageIcon ICON_SIMULATOR_TO_END =
    getSupremicaIcon(NAME_SIMULATOR_TO_END);
  public static final ImageIcon ICON_SIMULATOR_TO_START =
    getSupremicaIcon(NAME_SIMULATOR_TO_START);
  public static final ImageIcon ICON_TABLE_NORMAL_AUTOMATON = null;
  public static final ImageIcon ICON_TABLE_ENABLED_AUTOMATON = getSupremicaIcon(NAME_TICK);
  public static final ImageIcon ICON_TABLE_WARNING_PROPERTY = ICON_CONSOLE_WARNING;
  public static final ImageIcon ICON_TABLE_ERROR_AUTOMATON = ICON_CONSOLE_WARNING;
  public static final ImageIcon ICON_TABLE_DISABLED_PROPERTY = getSupremicaIcon(NAME_CROSS);
  public static final ImageIcon ICON_EVENTTREE_BLOCKING_EVENT = ICON_CONSOLE_WARNING;
  public static final ImageIcon ICON_EVENTTREE_INVALID_EVENT = getSupremicaIcon(NAME_CROSS);
  public static final ImageIcon ICON_EVENTTREE_VALID_EVENT = getSupremicaIcon(NAME_TICK);
  public static final ImageIcon ICON_EVENTTREE_CAUSES_WARNING_EVENT = ICON_CONSOLE_WARNING;
  public static final ImageIcon ICON_EVENTTREE_DISABLED_AUTOMATON = getSupremicaIcon(NAME_CROSS);
  public static final ImageIcon ICON_EVENTTREE_ENABLED_AUTOMATON = getSupremicaIcon(NAME_TICK);
  public static final ImageIcon ICON_EVENTTREE_CAUSES_WARNING_PROPERTY = ICON_CONSOLE_WARNING;

  public static final Icon ICON_EVENTTREE_BLOCKING_AUTOMATON = ICON_CONSOLE_WARNING;

}
