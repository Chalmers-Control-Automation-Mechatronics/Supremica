//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package org.supremica.properties;

import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.options.AggregatorOptionPage;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.ColorOption;
import net.sourceforge.waters.analysis.options.DoubleOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.analysis.options.OptionChangeEvent;
import net.sourceforge.waters.analysis.options.OptionChangeListener;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.analysis.options.SimpleLeafOptionPage;
import net.sourceforge.waters.analysis.options.StringOption;

/**
 *
 * @author Benjamin Wheeler
 */
public class SupremicaOptionPage extends SimpleLeafOptionPage
{

  public SupremicaOptionPage(final PropertyType type, final String title)
  {
    super(type.getTitle(), title);
    mType = type;
    mOptions = new LinkedList<>();


    for (final Property property : Property.getAllProperties()) {
      if (property.getPropertyType() == mType) {
        final Option<?> option;
        if (property instanceof BooleanProperty) {
          option = new BooleanOption(property.getKey(),
                                     property.getComment(),
                                     null, null,
                                     ((BooleanProperty) property).get());
        } else if (property instanceof IntegerProperty) {
          option = new PositiveIntOption(property.getKey(),
                                         property.getComment(),
                                         null,
                                         null,
                                         ((IntegerProperty) property).get());
        } else if (property instanceof DoubleProperty) {
          final DoubleProperty prop = (DoubleProperty) property;
          option = new DoubleOption(property.getKey(), property.getComment(),
                                    null, null,
                                    prop.get(),
                                    prop.getMinValue(),
                                    prop.getMaxValue());
        } else if (property instanceof EnumProperty) {
          option = new EnumOption<Object>
            (property.getKey(), property.getComment(),
             null, null,
             ((EnumProperty<?>) property).getLegalValues(),
             ((EnumProperty<?>) property).get());
        } else if (property instanceof ObjectProperty) {
          final ObjectProperty<?> prop = (ObjectProperty<?>) property;
          final Object[] legalValues = prop.getLegalValues();
          if (legalValues != null) {
            option = new EnumOption<Object>
              (property.getKey(), property.getComment(),
               null, null,
               ((ObjectProperty<?>) property).getLegalValues(),
               ((ObjectProperty<?>) property).get());
          } else {
            try {
              option = new StringOption(prop.getKey(), prop.getComment(),
                                        null, null, (String) prop.get());
            } catch(final ClassCastException e) {
              System.err.println(prop.getFullKey()+" is type "+prop.get().getClass().getSimpleName());
              continue;
            }

          }
        } else if (property instanceof ColorProperty) {
          option = new ColorOption(property.getKey(),
                                   property.getComment(),
                                   null, null,
                                   ((ColorProperty) property).get());
        } else {
          System.err.println("Cannot handle property "
            +property.getFullKey()+" of type "
            +property.getClass().getSimpleName());
          continue;
        }
        option.setEditable(property.isEditable());
        mOptions.add(option);
        add(option);
        option.addPropertyChangeListener(new OptionChangeListener() {
          @Override
          public void optionChanged(final OptionChangeEvent event)
          {
            property.set(event.getNewValue());
          }
        });
        property.addPropertyChangeListener(new SupremicaPropertyChangeListener() {
          @Override
          public void propertyChanged(final SupremicaPropertyChangeEvent event)
          {
            option.set(event.getNewValue());
          }
        });
      }
    }
  }

  @Override
  public List<Option<?>> getOptions()
  {
    return mOptions;
  }

  private final PropertyType mType;
  private final List<Option<?>> mOptions;




  public static final SimpleLeafOptionPage General =
    new SupremicaOptionPage(PropertyType.GENERAL, "General");
  public static final SimpleLeafOptionPage GeneralLog =
    new SupremicaOptionPage(PropertyType.GENERAL_LOG, "Log");
  public static final SimpleLeafOptionPage GeneralFile =
    new SupremicaOptionPage(PropertyType.GENERAL_FILE, "File");
  public static final SimpleLeafOptionPage Gui =
    new SupremicaOptionPage(PropertyType.GUI, "Compiler");
  public static final SimpleLeafOptionPage GuiEditor =
    new SupremicaOptionPage(PropertyType.GUI_EDITOR, "Editor");
  public static final SimpleLeafOptionPage GuiAnalyzer =
    new SupremicaOptionPage(PropertyType.GUI_ANALYZER, "Analyzer");
  public static final SimpleLeafOptionPage GuiSimulator =
    new SupremicaOptionPage(PropertyType.GUI_SIMULATOR, "Simulator");
  public static final SimpleLeafOptionPage GuiDot =
    new SupremicaOptionPage(PropertyType.GUI_DOT, "Dot");
//  public static final SimpleLeafOptionPage Algorithms =
//    new SupremicaOptionPage(PropertyType.ALGORITHMS, "Algorithms");
  public static final SimpleLeafOptionPage AlgorithmsSynchronization =
    new SupremicaOptionPage(PropertyType.ALGORITHMS_SYNCHRONIZATION, "Synchronization");
  public static final SimpleLeafOptionPage AlgorithmsVerification =
    new SupremicaOptionPage(PropertyType.ALGORITHMS_VERIFICATION, "Verification");
  public static final SimpleLeafOptionPage AlgorithmsSynthesis =
    new SupremicaOptionPage(PropertyType.ALGORITHMS_SYNTHESIS, "Synthesis");
  public static final SimpleLeafOptionPage AlgorithmsMinimization =
    new SupremicaOptionPage(PropertyType.ALGORITHMS_MINIMIZATION, "Minimization");
  public static final SimpleLeafOptionPage AlgorithmsBDD =
    new SupremicaOptionPage(PropertyType.ALGORITHMS_BDD, "BDD");
  public static final SimpleLeafOptionPage AlgorithmsHMI =
    new SupremicaOptionPage(PropertyType.ALGORITHMS_HMI, "HMI");
  public static final SimpleLeafOptionPage Misc =
    new SupremicaOptionPage(PropertyType.MISC, "Misc");

  public static final AggregatorOptionPage IDE_AGGREGATOR_OPTION_PAGE =
    new AggregatorOptionPage("IDE", General, GeneralFile, GeneralLog, Misc);

  public static final AggregatorOptionPage GUI_AGGREGATOR_OPTION_PAGE =
    new AggregatorOptionPage("GUI", GuiEditor, GuiSimulator, GuiAnalyzer, Gui);

  public static final AggregatorOptionPage ANALYZER_AGGREGATOR_OPTION_PAGE =
    new AggregatorOptionPage("Supremica Analyzer", AlgorithmsSynchronization,
                             AlgorithmsVerification, AlgorithmsSynthesis,
                             AlgorithmsMinimization,
                             AlgorithmsBDD, AlgorithmsHMI, GuiDot);


}
