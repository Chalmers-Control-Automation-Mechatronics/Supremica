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

import java.awt.Color;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.context.BindingContext;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDEMenuBar;
import org.supremica.gui.ide.MainPanel;
import org.supremica.gui.ide.ModuleContainer;


/**
 * @author George Hewlett
 */

public class WatersAnalyzerPanel extends MainPanel
{

  //#########################################################################
  //# Constructor
  public WatersAnalyzerPanel(final ModuleContainer moduleContainer,
                             final String name)
  {
    super(name);
    mModuleContainer = moduleContainer;
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mSimpleExpressionCompiler =
      new SimpleExpressionCompiler(factory, optable);
    mAutomataTable = new AutomataTable(moduleContainer, this);
    final JScrollPane scroll = new JScrollPane(mAutomataTable);
    setLeftComponent(scroll);
    final JPanel emptyPanel = new JPanel();
    emptyPanel.setBackground(Color.WHITE);
    setRightComponent(emptyPanel);
  }


  //#########################################################################
  //# Simple Access
  public ModuleContainer getModuleContainer()
  {
    return mModuleContainer;
  }

  public ModuleSubject getModule()
  {
    return mModuleContainer.getModule();
  }

  public AutomataTableModel getAutomataTableModel()
  {
    return mAutomataTable.getModel();
  }

  public AutomataTable getAutomataTable()
  {
    return mAutomataTable;
  }


  //#########################################################################
  //# Overrides for org.supremica.gui.ide.MainPanel
  @Override
  protected void activate()
  {
    FocusTracker.requestFocusFor(mAutomataTable);
  }

  @Override
  public void createPanelSpecificMenus(final IDEMenuBar menuBar)
  {
    menuBar.createWatersAnalyzeMenu();
  }

  @Override
  public AutomatonDisplayPane setRightComponent(final Proxy proxy)
    throws GeometryAbsentException
  {
    final AutomatonProxy aut = (AutomatonProxy) proxy;
    final Map<Object,SourceInfo> infoMap =
      mModuleContainer.getSourceInfoMap();
    final SourceInfo info = infoMap.get(aut);
    SimpleComponentSubject comp = null;
    if (info != null) {
      final Proxy source = info.getSourceObject();
      if (source instanceof SimpleComponentSubject) {
        comp = (SimpleComponentSubject) source;
      }
    }
    final BindingContext bindings;
    if (comp != null) {
      bindings = info.getBindingContext();
    } else {
      final AutomataTableModel autModel = mAutomataTable.getModel();
      if (autModel.containsDisplayMap(aut)) {
        comp = autModel.getCompFromDisplayMap(aut);
      } else {
        try {
          final ModuleProxyFactory factory =
            ModuleSubjectFactory.getInstance();
          final ProductDESImporter importer = new ProductDESImporter(factory);
          comp = (SimpleComponentSubject) importer.importComponent(aut);
          autModel.addToDisplayMap(aut, comp);
        } catch (final ParseException exception) {
          final Logger logger = LogManager.getLogger();
          logger.error(exception.getMessage());
          return null;
        }
      }
      bindings = null;
    }
    final GraphSubject graph = comp.getGraph();
    final AutomatonDisplayPane displayPane =
      new AutomatonDisplayPane(graph, bindings, mModuleContainer,
                               mSimpleExpressionCompiler, aut);
    final JScrollPane scroll = new JScrollPane(displayPane);
    setRightComponent(scroll);
    return displayPane;
  }


  //#########################################################################
  //# Data Members
  private final ModuleContainer mModuleContainer;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final AutomataTable mAutomataTable;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 8731351995076903210L;

}
