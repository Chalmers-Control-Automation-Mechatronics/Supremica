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

package net.sourceforge.waters.gui.analyzer;

import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import net.sourceforge.waters.analysis.options.ProductDESContext;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;

import org.supremica.gui.ide.ModuleContainer;


public class AnalyzerProductDESContext
  implements ProductDESContext
{

  //#########################################################################
  //# Constructor
  public AnalyzerProductDESContext(final WatersAnalyzerPanel panel)
  {
    mModuleContainer = panel.getModuleContainer();
    mAnalyzerPanel = panel;
  }

  //#########################################################################
  //# Constructors
  @Override
  public ProductDESProxy getProductDES()
  {
    return mModuleContainer.getCompiledDES();
  }

  @Override
  public List<AutomatonProxy> getActiveAutomata()
  {
    return mAnalyzerPanel.getAutomataTable().getOperationArgument();
  }

  @Override
  public Icon getEventIcon(final EventProxy event)
  {
    final Map<Object,SourceInfo> infoMap = mModuleContainer.getSourceInfoMap();
    final SourceInfo info = infoMap.get(event);
    if (info == null) {
      return null;
    }
    final Proxy proxy = info.getSourceObject();
    if (!(proxy instanceof EventDeclProxy)) {
      return null;
    }
    final EventDeclProxy decl = (EventDeclProxy) proxy;
    final ModuleContext context = mModuleContainer.getModuleContext();
    return context.getIcon(decl);
  }

  @Override
  public Icon getComponentKindIcon(final ComponentKind kind)
  {
    return ModuleContext.getComponentKindIcon(kind);
  }

  @Override
  public String getComponentKindText(final ComponentKind kind)
  {
    return ModuleContext.getComponentKindToolTip(kind);
  }

  //#########################################################################
  //# Constructors
  private final ModuleContainer mModuleContainer;
  private final WatersAnalyzerPanel mAnalyzerPanel;

}
