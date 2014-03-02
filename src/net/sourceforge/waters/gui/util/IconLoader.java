//# -*- tab-width: 2  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   IconLoader
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.util;

import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.supremica.properties.Config;


public class IconLoader
{

  //#########################################################################
  //# Static Class Methods
  private static final ImageIcon getWatersIcon(final String name)
  {
    final String subdir = Config.GUI_EDITOR_ICONSET.getAsString();
    return getWatersIcon(subdir, name);
  }

  private static final ImageIcon getWatersIcon(final String subdir,
                                               final String name)
  {
    final Class<?> cls = IconLoader.class;
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

  private static List<ImageIcon> getIconList(final String name)
  {
    final String subdir = Config.GUI_EDITOR_ICONSET.getAsString();
    if (Character.isDigit(subdir.charAt(0))) {
      final List<ImageIcon> icons = new LinkedList<ImageIcon>();
      for (final String choice : Config.GUI_EDITOR_ICONSET.getLegalValues()) {
        if (Character.isDigit(choice.charAt(0))) {
          final ImageIcon icon = getWatersIcon(choice, name);
          icons.add(icon);
        }
      }
      return icons;
    } else {
      final ImageIcon icon = getWatersIcon(subdir, name);
      return Collections.singletonList(icon);
    }
  }

  private static List<Image> getImageList(final String name)
  {
    final List<ImageIcon> icons = getIconList(name);
    final int size = icons.size();
    switch (size) {
    case 0:
      return Collections.emptyList();
    case 1:
      final ImageIcon icon0 = icons.get(0);
      final Image image0 = icon0.getImage();
      return Collections.singletonList(image0);
    default:
      final List<Image> images = new ArrayList<Image>(size);
      for (final ImageIcon icon : icons) {
        final Image image = icon.getImage();
        images.add(image);
      }
      return images;
    }
  }


  //#########################################################################
  //# Class Constants
  private static final String NAME_APPLICATION = "application";
  private static final String NAME_BINDING = "binding";
  private static final String NAME_CONSOLE_DEBUG = "debug";
  private static final String NAME_CONSOLE_ERROR = "error";
  private static final String NAME_CONSOLE_INFO = "info";
  private static final String NAME_CONSOLE_WARNING = "warning";
  private static final String NAME_CONSTANT = "constant";
  private static final String NAME_CONTROLLABLE_OBSERVABLE =
    "controllable";
  private static final String NAME_CONTROLLABLE_OBSERVABLE_ERROR =
    "controllable_error";
  private static final String NAME_CONTROLLABLE_UNOBSERVABLE =
    "controllable_unobservable";
  private static final String NAME_EVENT = "event";
  private static final String NAME_EVENT_ALIAS = "event_alias";
  private static final String NAME_EVENT_ALIAS_ERROR = "event_alias_error";
  private static final String NAME_FOREACH = "foreach";
  private static final String NAME_FOREACH_ERROR = "foreach_error";
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
  private static final String NAME_NO = "no";
  private static final String NAME_PLANT = "plant";
  private static final String NAME_PLANT_ERROR = "plant_error";
  private static final String NAME_PROPERTY = "property";
  private static final String NAME_PROPERTY_ERROR = "property_error";
  private static final String NAME_SIMULATOR_BACK = "simulator_back";
  private static final String NAME_SIMULATOR_REPLAY = "simulator_replay";
  private static final String NAME_SIMULATOR_RESET = "simulator_reset";
  private static final String NAME_SIMULATOR_STEP = "simulator_step";
  private static final String NAME_SIMULATOR_TO_END = "simulator_to_end";
  private static final String NAME_SIMULATOR_TO_START = "simulator_to_start";
  private static final String NAME_SPEC = "specification";
  private static final String NAME_SPEC_ERROR = "specification_error";
  private static final String NAME_SUPERVISOR = "supervisor";
  private static final String NAME_SUPERVISOR_ERROR = "supervisor_error";
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
  private static final String NAME_TOOL_OPTIONS = "options";
  private static final String NAME_TOOL_PASTE = "paste";
  private static final String NAME_TOOL_PRINT = "print";
  private static final String NAME_TOOL_PRINT_EPS = "print_eps";
  private static final String NAME_TOOL_PRINT_PDF = "print_pdf";
  private static final String NAME_TOOL_REDO = "redo";
  private static final String NAME_TOOL_SAVE = "save";
  private static final String NAME_TOOL_SAVE_AS = "save_as";
  private static final String NAME_TOOL_SELECT = "select";
  private static final String NAME_TOOL_UNDO = "undo";
  private static final String NAME_UNCONTROLLABLE_OBSERVABLE =
    "uncontrollable";
  private static final String NAME_UNCONTROLLABLE_OBSERVABLE_ERROR =
    "uncontrollable_error";
  private static final String NAME_UNCONTROLLABLE_UNOBSERVABLE =
    "uncontrollable_unobservable";
  private static final String NAME_VARIABLE = "variable";
  private static final String NAME_YES = "yes";


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
  public static final ImageIcon ICON_CONTROLLABLE_OBSERVABLE_ERROR =
    getWatersIcon(NAME_CONTROLLABLE_OBSERVABLE_ERROR);
  public static final ImageIcon ICON_CONTROLLABLE_UNOBSERVABLE =
    getWatersIcon(NAME_CONTROLLABLE_UNOBSERVABLE);
  public static final ImageIcon ICON_EVENT = getWatersIcon(NAME_EVENT);
  public static final ImageIcon ICON_EVENT_ALIAS = getWatersIcon(NAME_EVENT_ALIAS);
  public static final ImageIcon ICON_EVENT_ALIAS_ERROR =
    getWatersIcon(NAME_EVENT_ALIAS_ERROR);
  public static final ImageIcon ICON_FORBIDDEN = getSupremicaIcon(NAME_FORBIDDEN);
  public static final ImageIcon ICON_FOREACH = getWatersIcon(NAME_FOREACH);
  public static final ImageIcon ICON_FOREACH_ERROR = getWatersIcon(NAME_FOREACH_ERROR);
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
  public static final ImageIcon ICON_NO = getWatersIcon(NAME_NO);
  public static final ImageIcon ICON_PLANT = getWatersIcon(NAME_PLANT);
  public static final ImageIcon ICON_PLANT_ERROR = getWatersIcon(NAME_PLANT_ERROR);
  public static final ImageIcon ICON_PROPERTY = getWatersIcon(NAME_PROPERTY);
  public static final ImageIcon ICON_PROPERTY_ERROR = getWatersIcon(NAME_PROPERTY_ERROR);
  public static final ImageIcon ICON_SPEC = getWatersIcon(NAME_SPEC);
  public static final ImageIcon ICON_SPEC_ERROR = getWatersIcon(NAME_SPEC_ERROR);
  public static final ImageIcon ICON_SUPERVISOR = getWatersIcon(NAME_SUPERVISOR);
  public static final ImageIcon ICON_SUPERVISOR_ERROR =
    getWatersIcon(NAME_SUPERVISOR_ERROR);
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
  public static final ImageIcon ICON_TOOL_OPTIONS = getWatersIcon(NAME_TOOL_OPTIONS);
  public static final ImageIcon ICON_TOOL_PASTE = getWatersIcon(NAME_TOOL_PASTE);
  public static final ImageIcon ICON_TOOL_PRINT = getWatersIcon(NAME_TOOL_PRINT);
  public static final ImageIcon ICON_TOOL_PRINT_EPS =
    getWatersIcon(NAME_TOOL_PRINT_EPS);
  public static final ImageIcon ICON_TOOL_PRINT_PDF =
    getWatersIcon(NAME_TOOL_PRINT_PDF);
  public static final ImageIcon ICON_TOOL_REDO = getWatersIcon(NAME_TOOL_REDO);
  public static final ImageIcon ICON_TOOL_SAVE = getWatersIcon(NAME_TOOL_SAVE);
  public static final ImageIcon ICON_TOOL_SAVE_AS = getWatersIcon(NAME_TOOL_SAVE_AS);
  public static final ImageIcon ICON_TOOL_SELECT = getWatersIcon(NAME_TOOL_SELECT);
  public static final ImageIcon ICON_TOOL_UNDO = getWatersIcon(NAME_TOOL_UNDO);
  public static final ImageIcon ICON_UNCONTROLLABLE_OBSERVABLE =
    getWatersIcon(NAME_UNCONTROLLABLE_OBSERVABLE);
  public static final ImageIcon ICON_UNCONTROLLABLE_OBSERVABLE_ERROR =
    getWatersIcon(NAME_UNCONTROLLABLE_OBSERVABLE_ERROR);
  public static final ImageIcon ICON_UNCONTROLLABLE_UNOBSERVABLE =
    getWatersIcon(NAME_UNCONTROLLABLE_UNOBSERVABLE);
  public static final ImageIcon ICON_VARIABLE = getWatersIcon(NAME_VARIABLE);
  public static final ImageIcon ICON_YES = getWatersIcon(NAME_YES);

  // Simulator
  public static final ImageIcon ICON_SIMULATOR_BACK =
    getWatersIcon(NAME_SIMULATOR_BACK);
  public static final ImageIcon ICON_SIMULATOR_REPLAY =
    getWatersIcon(NAME_SIMULATOR_REPLAY);
  public static final ImageIcon ICON_SIMULATOR_RESET =
    getWatersIcon(NAME_SIMULATOR_RESET);
  public static final ImageIcon ICON_SIMULATOR_STEP =
    getWatersIcon(NAME_SIMULATOR_STEP);
  public static final ImageIcon ICON_SIMULATOR_TO_END =
    getWatersIcon(NAME_SIMULATOR_TO_END);
  public static final ImageIcon ICON_SIMULATOR_TO_START =
    getWatersIcon(NAME_SIMULATOR_TO_START);
  public static final ImageIcon ICON_TABLE_NORMAL_AUTOMATON = null;
  public static final ImageIcon ICON_TABLE_ENABLED_AUTOMATON = ICON_YES;
  public static final ImageIcon ICON_TABLE_WARNING_PROPERTY = ICON_CONSOLE_WARNING;
  public static final ImageIcon ICON_TABLE_ERROR_AUTOMATON = ICON_CONSOLE_WARNING;
  public static final ImageIcon ICON_TABLE_DISABLED_PROPERTY = ICON_NO;
  public static final ImageIcon ICON_EVENTTREE_BLOCKING_EVENT = ICON_CONSOLE_WARNING;
  public static final ImageIcon ICON_EVENTTREE_INVALID_EVENT = ICON_NO;
  public static final ImageIcon ICON_EVENTTREE_VALID_EVENT = ICON_YES;
  public static final ImageIcon ICON_EVENTTREE_CAUSES_WARNING_EVENT = ICON_CONSOLE_WARNING;
  public static final ImageIcon ICON_EVENTTREE_DISABLED_AUTOMATON = ICON_NO;
  public static final ImageIcon ICON_EVENTTREE_ENABLED_AUTOMATON = ICON_YES;
  public static final ImageIcon ICON_EVENTTREE_CAUSES_WARNING_PROPERTY = ICON_CONSOLE_WARNING;

  public static final Icon ICON_EVENTTREE_BLOCKING_AUTOMATON = ICON_CONSOLE_WARNING;

  // Frame icon lists
  public static final List<Image> ICONLIST_APPLICATION =
    getImageList(NAME_APPLICATION);
  public static final List<ImageIcon> ICONLIST_PLANT = getIconList(NAME_PLANT);
  public static final List<ImageIcon> ICONLIST_PROPERTY =
    getIconList(NAME_PROPERTY);
  public static final List<ImageIcon> ICONLIST_SPEC = getIconList(NAME_SPEC);
  public static final List<ImageIcon> ICONLIST_SUPERVISOR =
    getIconList(NAME_SUPERVISOR);

}
