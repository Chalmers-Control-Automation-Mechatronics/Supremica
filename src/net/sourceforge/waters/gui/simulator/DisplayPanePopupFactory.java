//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.gui.simulator;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


class DisplayPanePopupFactory
  extends SimulatorPopupFactory
{

  //#########################################################################
  //# Constructor
  DisplayPanePopupFactory(final Simulation sim,
                          final AutomatonDisplayPane displayPane)
  {
    super(sim);
    mDisplayPane = displayPane;
    mVisitor = new PopupVisitor();
  }


  //#########################################################################
  //# Menu Items
  @Override
  protected void addDefaultMenuItems()
  {
    final AutomatonProxy aut = mDisplayPane.getAutomaton();
    super.addItemSpecificMenuItems(aut);
  }

  @Override
  protected void addItemSpecificMenuItems(final Proxy proxy)
  {
    mVisitor.addMenuItems(proxy);
    final AutomatonProxy aut = mDisplayPane.getAutomaton();
    super.addItemSpecificMenuItems(aut);
  }


  //#########################################################################
  //# Inner Class DisplayPanePopupVisitor
  private class PopupVisitor
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private void addMenuItems(final Proxy proxy)
    {
      try {
        proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      // do nothing
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    @Override
    public Object visitEdgeProxy(final EdgeProxy edge)
    {
      visitProxy(edge);
      if (mDisplayPane != null && mDisplayPane.canExecute()) {
        final WatersPopupActionManager master = getMaster();
        final JPopupMenu popup = getPopup();
        final IDEAction execute =
          master.getDesktopExecuteAction(mDisplayPane.getAutomaton(), edge);
        popup.add(execute);
        popup.addSeparator();
      }
      return null;
    }

    @Override
    public Object visitIdentifierProxy(final IdentifierProxy ident)
    {
      visitProxy(ident);
      if (mDisplayPane != null && mDisplayPane.canExecute()) {
        final WatersPopupActionManager master = getMaster();
        final JPopupMenu popup = getPopup();
        final IDEAction execute =
          master.getDesktopExecuteAction(mDisplayPane.getAutomaton(), ident);
        popup.add(execute);
        popup.addSeparator();
      }
      return null;
    }

    @Override
    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    {
      visitProxy(node);
      if (mDisplayPane != null && mDisplayPane.canSetState(node)) {
        final AutomatonProxy aut = mDisplayPane.getAutomaton();
        final WatersPopupActionManager master = getMaster();
        final IDEAction teleport = master.getDesktopSetStateAction(aut, node);
        final JPopupMenu popup = getPopup();
        popup.add(teleport);
        popup.addSeparator();
      }
      return null;
    }

  }


  //#######################################################################
  //# Data Members
  private final PopupVisitor mVisitor;
  private final AutomatonDisplayPane mDisplayPane;

}
