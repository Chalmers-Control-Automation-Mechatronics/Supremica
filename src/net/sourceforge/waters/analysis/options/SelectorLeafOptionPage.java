//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;


/**
 * <P>An option page that can display different options depending on one or
 * more selectors.</P>
 *
 * <P>The selector leaf option page contains a pool of registered options.
 * One or more of these options may be classified as selectors and used
 * to select a subset of the options to be displayed. The selector leaf
 * option page makes it possible to identify a subset of relevant options
 * depending on the values of the selectors. It is left to option page
 * editors to query and display the correct subsets.</P>
 *
 * <P>The selector options are structured to form a tree of choices,
 * whose root is the top selector option ({@link #getTopSelectorOption()}).
 * Associated with each possible value of a selector option is either
 * a sub-selector option ({@link #getSubSelectorOption(EnumOption, Object)
 * getSubSelectorOption()})---another selector option with more choices,
 * or a subset of options to be displayed ({@link #collectOptions(Collection,
 * EnumOption) collectOptions()}).</P>
 *
 * <P>The type parameter S is the type of the values of the last options
 * in the sequence of selectors, which is used to select to option
 * subsets.</P>
 *
 * @author Benjamin Wheeler
 */

public abstract class SelectorLeafOptionPage<S> extends LeafOptionPage
{

  //#########################################################################
  //# Constructor
  public SelectorLeafOptionPage(final String prefix, final String title)
  {
    super(prefix, title);
  }


  //#########################################################################
  //# Hooks
  public abstract EnumOption<?> getTopSelectorOption();

  public EnumOption<?> getSubSelectorOption(final EnumOption<?> parent,
                                            final Object value)
  {
    return null;
  }

  public String getDescription(final Object key)
  {
    return null;
  }

  public abstract void collectOptions(final Collection<Option<?>> options,
                                      S key);


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.OptionPage
  @Override
  public OptionPageEditor<SelectorLeafOptionPage<S>> createEditor
    (final OptionContext context)
  {
    return context.createSelectorLeafOptionPageEditor(this);
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.analysis.options.LeafOptionPage
  @Override
  public List<Option<?>> getOptions()
  {
    final Collection<Option<?>> options = new LinkedHashSet<>();
    final EnumOption<?> top = getTopSelectorOption();
    collectOptions(options, top);
    return new ArrayList<>(options);
  }


  //#########################################################################
  //# Auxiliary Methods
  public List<Option<?>> getCurrentOptions()
  {
    final EnumOption<S> selector = getCurrentPageSelectorOption();
    final S key = selector.getValue();
    return getOptions(key);
  }

  public List<Option<?>> getOptions(final S key)
  {
    final List<Option<?>> options = new LinkedList<>();
    collectOptions(options, key);
    return options;
  }

  public void collectOptions(final Collection<Option<?>> options,
                             final EnumOption<?> selector)
  {
    options.add(selector);
    for (final Object key : selector.getEnumConstants()) {
      final EnumOption<?> subSelector = getSubSelectorOption(selector, key);
      if (subSelector != null) {
        collectOptions(options, subSelector);
      } else {
        @SuppressWarnings("unchecked")
        final S typedKey = (S) key;
        collectOptions(options, typedKey);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public EnumOption<S> getCurrentPageSelectorOption()
  {
    EnumOption<?> parent = getTopSelectorOption();
    while (true) {
      final Object value = parent.getValue();
      final EnumOption<?> next = getSubSelectorOption(parent, value);
      if (next == null) {
        return (EnumOption<S>) parent;
      }
      parent = next;
    }
  }

}
