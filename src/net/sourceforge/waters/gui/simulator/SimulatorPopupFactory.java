//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.DefaultProductDESProxyVisitor;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import org.supremica.gui.ide.ModuleContainer;


class SimulatorPopupFactory extends PopupFactory
{

  //#########################################################################
  //# Constructor
  SimulatorPopupFactory(final Simulation sim)
  {
    super(sim.getModuleContainer().getIDE().getPopupActionManager());
    mSimulation = sim;
    mPopupVisitor = new PopupVisitor();
    mTime = -1;
  }


  //#########################################################################
  //# Invocation
  void maybeShowPopup(final Component invoker,
                      final MouseEvent event,
                      final Proxy proxy,
                      final int time)
  {
    try {
      mTime = time;
      maybeShowPopup(invoker, event, proxy);
    } finally {
      mTime = -1;
    }
  }


  //#########################################################################
  //# Menu Items
  @Override
  protected void addDefaultMenuItems()
  {
    if (mTime >= 0) {
      final WatersPopupActionManager master = getMaster();
      final JPopupMenu popup = getPopup();
      final IDEAction fireEvent = master.getTraceTravelAction(mTime);
      popup.add(fireEvent);
      popup.addSeparator();
    }
  }

  @Override
  protected void addItemSpecificMenuItems(final Proxy proxy)
  {
    mPopupVisitor.addMenuItems(proxy);
  }

  @Override
  protected void addCommonMenuItems()
  {
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    final IDEAction closeAll = master.getDesktopCloseAllAction();
    popup.add(closeAll);
    final IDEAction showAll = master.getDesktopShowAllAction();
    popup.add(showAll);
    final IDEAction cascade = master.getDesktopCascadeAction();
    popup.add(cascade);
  }


  //#########################################################################
  //# Inner Class PopupVisitor
  private class PopupVisitor extends DefaultProductDESProxyVisitor
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
    //# Interface net.sourceforge.waters.model.des.ProductDESProxyVisitor
    @Override
    public Object visitAutomatonProxy(final AutomatonProxy aut)
    {
      visitProxy(aut);
      final JPopupMenu popup = getPopup();
      final WatersPopupActionManager master = getMaster();
      final ModuleContainer container = mSimulation.getModuleContainer();
      final SimulatorPanel panel = container.getSimulatorPanel();
      final AutomatonDesktopPane desktop = panel.getDesktop();
      final boolean open = desktop.automatonIsOpen(aut);
      if (open) {
        final IDEAction close = master.getDesktopCloseWindowAction(aut);
        popup.add(close);
      } else {
        final IDEAction openWindow = master.getDesktopOpenWindowAction(aut);
        popup.add(openWindow);
      }
      final String name = aut.getName();
      if (desktop.canOpenOther(name)) {
        final IDEAction closeOther = master.getDesktopCloseOtherAction(aut);
        popup.add(closeOther);
      }
      if (desktop.canResize(name)) {
        final IDEAction resize = master.getResizeAction(aut);
        popup.add(resize);
      }
      final IDEAction edit = master.getDesktopEditAction(aut);
      popup.add(edit);
      popup.addSeparator();
      return null;
    }

    @Override
    public Object visitEventProxy(final EventProxy event)
    {
      visitProxy(event);
      final JPopupMenu popup = getPopup();
      final WatersPopupActionManager master = getMaster();
      final IDEAction fireEvent = master.getEventExecuteAction(event);
      popup.add(fireEvent);
      popup.addSeparator();
      return null;
    }

  }


  //#########################################################################
  //# Data Members
  private final PopupVisitor mPopupVisitor;
  private final Simulation mSimulation;
  private int mTime;

}








