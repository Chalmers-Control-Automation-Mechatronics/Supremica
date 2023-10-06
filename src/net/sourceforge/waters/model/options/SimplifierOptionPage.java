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

package net.sourceforge.waters.model.options;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierCreator;
import net.sourceforge.waters.analysis.abstraction.AutomatonSimplifierFactory;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.model.analysis.des.AutomatonBuilder;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * <P>An option page to configure a transition relation simplifier.</P>
 *
 * <P>The simplifier option page has two selector option to select a family
 * of simplifiers and then a specific {@link TransitionRelationSimplifier}.
 * Each choice leads to the set of options to configure the corresponding
 * {@link TransitionRelationSimplifier}.</P>
 *
 * @author Benjamin Wheeler
 */

public class SimplifierOptionPage
  extends SelectorLeafOptionPage<AutomatonSimplifierCreator>
{

  //#########################################################################
  //# Constructor
  protected SimplifierOptionPage(final String prefix,
                                 final String title,
                                 final String... classNames)
  {
    super(prefix, title);
    mCreatorOptionMap = new HashMap<>();
    final List<AutomatonSimplifierFactory> factories =
      getFamilies(classNames);
    mFamilyOption = new EnumOption<AutomatonSimplifierFactory>
      (prefix + ".FamilySelector", "Family", factories);
    register(mFamilyOption);
    for (final AutomatonSimplifierFactory factory : factories) {
      final List<AutomatonSimplifierCreator> creators =
        factory.getSimplifierCreators();
      final EnumOption<AutomatonSimplifierCreator> creatorOption =
        new EnumOption<AutomatonSimplifierCreator>
          (prefix + ".SimplifierSelector." + factory, "Simplifier", creators);
      mCreatorOptionMap.put(factory, creatorOption);
      register(creatorOption);
    }
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.options.SelectorLeafOptionPage
  @Override
  public EnumOption<AutomatonSimplifierFactory> getTopSelectorOption()
  {
    return mFamilyOption;
  }

  @Override
  public EnumOption<AutomatonSimplifierCreator>
  getSubSelectorOption(final EnumOption<?> parent, final Object value)
  {
    if (parent == mFamilyOption) {
      return mCreatorOptionMap.get(value);
    } else {
      return null;
    }
  }

  @Override
  public void collectOptions(final Collection<Option<?>> options,
                             final AutomatonSimplifierCreator creator)
  {
    for (final Option<?> option : creator.getOptions(this)) {
      options.add(option);
    }
    final AutomatonBuilder builder = creator.createBuilder
      (ProductDESElementFactory.getInstance());
    for (final Option<?> option : builder.getOptions(this)) {
      options.add(option);
    }
    options.add(get(AutomatonSimplifierFactory.
                    OPTION_AutomatonSimplifierFactory_KeepOriginal));
  }

  @Override
  public String getDescription(final Object key)
  {
    if (key instanceof AutomatonSimplifierCreator) {
      final AutomatonSimplifierCreator creator =
        (AutomatonSimplifierCreator) key;
      return creator.getDescription();
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private List<AutomatonSimplifierFactory> getFamilies
    (final String... classNames)
  {
    final List<AutomatonSimplifierFactory> families =
      new ArrayList<>(classNames.length);
    for (final String className : classNames) {
      try {
        final ClassLoader loader = getClass().getClassLoader();
        final Class<?> clazz = loader.loadClass(className);
        final Method method = clazz.getMethod("getInstance");
        final AutomatonSimplifierFactory factory =
          (AutomatonSimplifierFactory) method.invoke(null);
        factory.registerOptions(this);
        families.add(factory);
      } catch (NoClassDefFoundError |
               ClassNotFoundException |
               UnsatisfiedLinkError |
               NoSuchMethodException |
               SecurityException |
               IllegalAccessException |
               IllegalArgumentException |
               InvocationTargetException exception) {
        // skip this factory
      }
    }
    return families;
  }


  //#########################################################################
  //# Data Members
  private final EnumOption<AutomatonSimplifierFactory> mFamilyOption;
  private final Map<AutomatonSimplifierFactory,EnumOption<AutomatonSimplifierCreator>>
    mCreatorOptionMap;

}
