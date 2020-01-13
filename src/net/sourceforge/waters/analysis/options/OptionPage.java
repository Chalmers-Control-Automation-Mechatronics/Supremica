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

package net.sourceforge.waters.analysis.options;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.StepSimplifierFactory;
import net.sourceforge.waters.analysis.trcomp.ChainSimplifierFactory;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;


/**
 * <P>A collection of available options.</P>
 *
 * <P>An option map acts as a database that maps string identifiers to
 * {@link Option} objects. For persistent storage of options values,
 * the option map is first initialised with all available options,
 * typically at program start-up. The initialised option map is then
 * passed to {@link Configurable} objects, which select the options
 * stored in it.</P>
 *
 * @author Robi Malik, Benjamin Wheeler
 */
public abstract class OptionPage
{

  //#########################################################################
  //# Constructors


  //#########################################################################
  //# Simple Access
  public abstract Option<?> get(final String id);

  public abstract void add(final Option<?> param);

  public abstract String getPrefix();

  public abstract String getTitle();

  //#########################################################################
  //# Manipulating Option Lists//TODO Move to LeafOptionPage?
  public void append(final List<Option<?>> list, final String id)
  {
    final Option<?> option = get(id);
    assert option != null;
    list.add(option);
  }

  public void prepend(final List<Option<?>> list, final String id)
  {
    final Option<?> option = get(id);
    assert option != null;
    list.add(0, option);
  }

  public boolean remove(final List<Option<?>> list, final String id)
  {
    final Iterator<Option<?>> iter = list.iterator();
    while (iter.hasNext()) {
      final Option<?> option = iter.next();
      if (option.hasID(id)) {
        iter.remove();
        return true;
      }
    }
    return false;
  }

  public abstract OptionPageEditor<? extends OptionPage> createEditor(OptionContext context);

  public static LeafOptionPage getOptionPage(final String prefix) {
    for (final LeafOptionPage map : OPTION_PAGES) {
      if (map.getPrefix().equals(prefix)) return map;
    }
    return null;
  }

  public static OptionPage loadOptionPage(final String className,
                                                    final String fieldName) {
    try {
      final Class<?>cls = OptionPage.class.getClassLoader().loadClass(className);
      final Field f = cls.getField(fieldName);
      final OptionPage page = (OptionPage) f.get(null);
      return page;
    } catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
      return null;
    }
  }


  //#########################################################################
  //# Data Members

  //#########################################################################
  //# Class Constants
  public static final List<LeafOptionPage> OPTION_PAGES = new LinkedList<>();

  public static final SelectorLeafOptionPage ConflictCheck =
    new AnalysisOptionPage(AnalysisOperation.CONFLICT_CHECK);
  public static final SelectorLeafOptionPage ControllabilityCheck =
    new AnalysisOptionPage(AnalysisOperation.CONTROLLABILITY_CHECK);
  public static final SelectorLeafOptionPage ControlLoop =
    new AnalysisOptionPage(AnalysisOperation.CONTROL_LOOP_CHECK);
  public static final SelectorLeafOptionPage DeadlockCheck =
    new AnalysisOptionPage(AnalysisOperation.DEADLOCK_CHECK);
  public static final SelectorLeafOptionPage LanguageInclusion =
    new AnalysisOptionPage(AnalysisOperation.LANGUAGE_INCLUSION_CHECK);
  public static final SelectorLeafOptionPage StateCounter =
    new AnalysisOptionPage(AnalysisOperation.STATE_COUNTER);
  public static final SelectorLeafOptionPage SynchronousProduct =
    new AnalysisOptionPage(AnalysisOperation.SYNCHRONOUS_PRODUCT);
  public static final SelectorLeafOptionPage Synthesis =
    new AnalysisOptionPage(AnalysisOperation.SUPERVISOR_SYNTHESIZER);

  public static final SelectorLeafOptionPage Simplifier = new SimplifierOptionPage
    ("waters.analysis.simplification",
     "Simplifiers",
     StepSimplifierFactory.class.getName(),
     "org.supremica.automata.waters.SupremicaSimplifierFactory",
     ChainSimplifierFactory.class.getName());



  private static final AggregatorOptionPage[] TOP_LEVEL_AGGREGATORS =
  new AggregatorOptionPage[] {
    (AggregatorOptionPage)
    loadOptionPage("org.supremica.properties.SupremicaOptionPage",
                   "IDE_AGGREGATOR_OPTION_PAGE"),

    (AggregatorOptionPage)
    loadOptionPage("org.supremica.properties.SupremicaOptionPage",
                   "GUI_AGGREGATOR_OPTION_PAGE"),

    new AggregatorOptionPage("Analysis", ConflictCheck, ControllabilityCheck,
                             ControllabilityCheck, ControlLoop,
                             DeadlockCheck, LanguageInclusion,
                             StateCounter, SynchronousProduct, Synthesis,
                             Simplifier),

    (AggregatorOptionPage)
    loadOptionPage("org.supremica.properties.SupremicaOptionPage",
                   "ANALYZER_AGGREGATOR_OPTION_PAGE")
  };

  public static final AggregatorOptionPage TOP_LEVEL_AGGREGATOR =
    new AggregatorOptionPage("top-level", TOP_LEVEL_AGGREGATORS);

}
