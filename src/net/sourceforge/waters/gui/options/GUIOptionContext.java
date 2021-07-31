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

package net.sourceforge.waters.gui.options;

import java.awt.Component;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import net.sourceforge.waters.gui.ErrorDisplay;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.options.AggregatorOptionPage;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.ChainedAnalyzerOption;
import net.sourceforge.waters.model.options.ColorOption;
import net.sourceforge.waters.model.options.ComponentKindOption;
import net.sourceforge.waters.model.options.DoubleOption;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.EventSetOption;
import net.sourceforge.waters.model.options.FileOption;
import net.sourceforge.waters.model.options.MemoryOption;
import net.sourceforge.waters.model.options.OptionContext;
import net.sourceforge.waters.model.options.OptionEditor;
import net.sourceforge.waters.model.options.ParameterBindingListOption;
import net.sourceforge.waters.model.options.PositiveIntOption;
import net.sourceforge.waters.model.options.PropositionOption;
import net.sourceforge.waters.model.options.SelectorLeafOptionPage;
import net.sourceforge.waters.model.options.SimpleLeafOptionPage;
import net.sourceforge.waters.model.options.StringListOption;
import net.sourceforge.waters.model.options.StringOption;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public class GUIOptionContext implements OptionContext
{

  //#########################################################################
  //# Constructors
  public GUIOptionContext(final IDE ide,
                          final Component dialogParent,
                          final ErrorDisplay display)
  {
    mIDE = ide;
    mModuleContainer = null;
    mAnalyzerPanel = null;
    mDialogParent = dialogParent;
    mErrorDisplay = display;
    mDES = null;
  }

  public GUIOptionContext(final WatersAnalyzerPanel panel,
                          final Component dialogParent,
                          final ErrorDisplay display)
  {
    mModuleContainer = panel.getModuleContainer();
    mIDE = mModuleContainer.getIDE();
    mAnalyzerPanel = panel;
    mDialogParent = dialogParent;
    mErrorDisplay = display;
    final String name = mModuleContainer.getName();
    final List<AutomatonProxy> automata =
      panel.getAutomataTable().getOperationArgument();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mDES = AutomatonTools.createProductDESProxy(name, automata, factory);
  }


  //#########################################################################
  //# Simple Access
  public IDE getIDE()
  {
    return mIDE;
  }

  public ModuleContainer getModuleContainer()
  {
    return mModuleContainer;
  }

  public WatersAnalyzerPanel getWatersAnalyzerPanel()
  {
    return mAnalyzerPanel;
  }

  public ProductDESProxyFactory getProductDESProxyFactory()
  {
    return ProductDESElementFactory.getInstance();
  }

  public Component getDialogParent()
  {
    return mDialogParent;
  }

  public ErrorDisplay getErrorDisplay()
  {
    return mErrorDisplay;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.options.OptionContext
  @Override
  public ProductDESProxy getProductDES()
  {
    return mDES;
  }

  @Override
  public BooleanOptionPanel createBooleanEditor(final BooleanOption option)
  {
    return new BooleanOptionPanel(this, option);
  }

  @Override
  public ChainedAnalyzerOptionPanel
  createChainedAnalyzerEditor(final ChainedAnalyzerOption option)
  {
    return new ChainedAnalyzerOptionPanel(this, option);
  }

  @Override
  public ColorOptionPanel createColorEditor(final ColorOption option)
  {
    return new ColorOptionPanel(this, option);
  }

  @Override
  public OptionEditor<ComponentKind>
  createComponentKindEditor(final ComponentKindOption option)
  {
    return new ComponentKindOptionPanel(this, option);
  }

  @Override
  public <E> OptionEditor<E>
  createEnumEditor(final EnumOption<E> option)
  {
    return new EnumOptionPanel<>(this, option);
  }

  @Override
  public OptionEditor<Set<EventProxy>>
  createEventSetEditor(final EventSetOption option)
  {
    return new EventSetOptionPanel(this, option);
  }

  @Override
  public OptionEditor<File>
  createFileEditor(final FileOption option)
  {
    return new FileOptionPanel(this, option);
  }

  @Override
  public OptionEditor<List<ParameterBindingProxy>>
  createParameterBindingListEditor(final ParameterBindingListOption option)
  {
    return null;
  }

  @Override
  public OptionEditor<Integer>
  createPositiveIntEditor(final PositiveIntOption option)
  {
    return new PositiveIntOptionPanel(this, option);
  }

  @Override
  public OptionEditor<Double>
  createDoubleEditor(final DoubleOption option)
  {
    return new DoubleOptionPanel(this, option);
  }

  @Override
  public OptionEditor<String>
  createMemoryOptionEditor(final MemoryOption option)
  {
    return new MemoryOptionPanel(this, option);
  }

  @Override
  public OptionEditor<EventProxy>
  createPropositionEditor(final PropositionOption option)
  {
    return new PropositionOptionPanel(this, option);
  }

  @Override
  public OptionEditor<String>
  createStringEditor(final StringOption option)
  {
    return new StringOptionPanel(this, option);
  }

  @Override
  public OptionEditor<List<String>>
  createStringListEditor(final StringListOption option)
  {
    return null;
  }

  @Override
  public SimpleLeafOptionPagePanel
  createSimpleLeafOptionPageEditor(final SimpleLeafOptionPage page)
  {
    return new SimpleLeafOptionPagePanel(this, page);
  }

  @Override
  public <S> SelectorLeafOptionPagePanel<S>
  createSelectorLeafOptionPageEditor(final SelectorLeafOptionPage<S> page)
  {
    return new SelectorLeafOptionPagePanel<S>(this, page);
  }

  @Override
  public AggregatorOptionPagePanel
  createAggregatorOptionPageEditor(final AggregatorOptionPage page)
  {
    return new AggregatorOptionPagePanel(this, page);
  }


  //#########################################################################
  //# Graphics and Icons
  public Icon getEventIcon(final EventProxy event)
  {
    if (mModuleContainer != null) {
      final Map<Object,SourceInfo> infoMap = mModuleContainer.getSourceInfoMap();
      final SourceInfo info = infoMap.get(event);
      if (info == null) {
        if (event.getKind() == EventKind.CONTROLLABLE) {
          if (event.isObservable()) {
            return IconAndFontLoader.ICON_CONTROLLABLE_OBSERVABLE;
          }
          else {
            return IconAndFontLoader.ICON_CONTROLLABLE_UNOBSERVABLE;
          }
        }
        else if (event.getKind() == EventKind.UNCONTROLLABLE) {
          if (event.isObservable()) {
            return IconAndFontLoader.ICON_UNCONTROLLABLE_OBSERVABLE;
          }
          else {
            return IconAndFontLoader.ICON_UNCONTROLLABLE_UNOBSERVABLE;
          }
        }
        else return null;
      }
      final Proxy proxy = info.getSourceObject();
      if (!(proxy instanceof EventDeclProxy)) {
        return null;
      }
      final EventDeclProxy decl = (EventDeclProxy) proxy;
      final ModuleContext context = mModuleContainer.getModuleContext();
      return context.getIcon(decl);
    }
    else return null;
  }


  //#########################################################################
  //# Data Members
  private final IDE mIDE;
  private final ModuleContainer mModuleContainer;
  private final WatersAnalyzerPanel mAnalyzerPanel;
  private final Component mDialogParent;
  private final ErrorDisplay mErrorDisplay;
  private final ProductDESProxy mDES;

}
