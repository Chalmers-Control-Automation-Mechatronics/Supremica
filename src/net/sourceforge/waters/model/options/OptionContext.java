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

import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


/**
 * <P>A context for editing options.</P>
 *
 * <P>The option context is a bridge that allows the creation of option
 * editors ({@link OptionEditor}) for a specific environment. There may
 * be different implementations, for example the {@link
 * net.sourceforge.waters.gui.options.GUIOptionContext GUIOptionContext}
 * creates Swing components to edit each option.</P>
 *
 * <P>Additionally, the option context provides access to a model
 * being analysed ({@link ProductDESProxy}), to support options whose
 * that take their values from the within model.</P>
 *
 * @author Robi Malik
 */

public interface OptionContext
{
  /**
   * Gets the model being analysed in this context.
   */
  public ProductDESProxy getProductDES();

  /**
   * Creates an option editor for a Boolean option.
   */
  public OptionEditor<Boolean>
  createBooleanEditor(BooleanOption option);

  /**
   * Creates an option editor for a chained analyser option.
   */
  public OptionEditor<ModelAnalyzerFactoryLoader>
  createChainedAnalyzerEditor(ChainedAnalyzerOption option);

  /**
   * Creates an option editor for a colour option.
   */
  public OptionEditor<Color>
  createColorEditor(ColorOption option);

  /**
   * Creates an option editor for a component kind option.
   */
  public OptionEditor<ComponentKind>
  createComponentKindEditor(ComponentKindOption option);

  /**
   * Creates an option editor for an enumeration option.
   */
  public <E> OptionEditor<E>
  createEnumEditor(EnumOption<E> option);

  /**
   * Creates an option editor for an event set option.
   */
  public OptionEditor<Set<EventProxy>>
  createEventSetEditor(EventSetOption option);

  /**
   * Creates an option editor for a file option.
   */
  public OptionEditor<File>
  createFileEditor(FileOption option);

  /**
   * Creates an option editor for a parameter binding list option.
   */
  public OptionEditor<List<ParameterBindingProxy>>
  createParameterBindingListEditor(ParameterBindingListOption option);

  /**
   * Creates an option editor for a integer option.
   */
  public OptionEditor<Integer>
  createPositiveIntEditor(PositiveIntOption option);

  /**
   * Creates an option editor for a double option.
   */
  public OptionEditor<Double>
  createDoubleEditor(DoubleOption option);

  /**
   * Creates an option editor for a memory option.
   */
  public OptionEditor<String>
  createMemoryOptionEditor(MemoryOption option);

  /**
   * Creates an option editor for a proposition option.
   */
  public OptionEditor<EventProxy>
  createPropositionEditor(PropositionOption option);

  /**
   * Creates an option editor for a string option.
   */
  public OptionEditor<String>
  createStringEditor(StringOption option);

  /**
   * Creates an option editor for a string list option.
   */
  public OptionEditor<List<String>>
  createStringListEditor(StringListOption option);

  /**
   * Creates an option editor for a simple leaf option page.
   */
  public OptionPageEditor<SimpleLeafOptionPage>
  createSimpleLeafOptionPageEditor(SimpleLeafOptionPage page);

  /**
   * Creates an option editor for a selector leaf option page.
   */
  public <S> OptionPageEditor<SelectorLeafOptionPage<S>>
  createSelectorLeafOptionPageEditor(SelectorLeafOptionPage<S> page);

  /**
   * Creates an option editor for a string aggregator option page.
   */
  public OptionPageEditor<AggregatorOptionPage>
  createAggregatorOptionPageEditor(AggregatorOptionPage page);

}
