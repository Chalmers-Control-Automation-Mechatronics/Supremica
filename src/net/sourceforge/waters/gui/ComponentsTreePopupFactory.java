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

package net.sourceforge.waters.gui;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


class ComponentsTreePopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  ComponentsTreePopupFactory(final WatersPopupActionManager master,
                             final ModuleContext context)
  {
    super(master);
    mVisitor = new ComponentsTreePopupVisitor();
    mContext = context;
  }


  //#########################################################################
  //# Shared Menu Items
  @Override
  protected void addItemSpecificMenuItems(final Proxy proxy)
  {
    try {
      proxy.acceptVisitor(mVisitor);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }

  @Override
  protected void addCommonMenuItems()
  {
    super.addCommonMenuItems();
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    popup.addSeparator();
    final IDEAction newaut = master.getInsertSimpleComponentAction();
    popup.add(newaut);
    final IDEAction newVar = master.getInsertVariableAction();
    if (newVar.isEnabled()) {
      popup.add(newVar);
    }
    final IDEAction newCond = master.getInsertConditionalAction();
    if (newCond.isEnabled()) {
      popup.add(newCond);
    }
    final IDEAction newFor = master.getInsertForeachAction();
    if (newFor.isEnabled()) {
      popup.add(newFor);
    }
    final IDEAction newInst = master.getInsertInstanceAction();
    if (newInst.isEnabled()) {
      popup.add(newInst);
    }
  }


  //#########################################################################
  //# Inner Class GraphPopupVisitor
  private class ComponentsTreePopupVisitor
    extends DefaultModuleProxyVisitor
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    @Override
    public Object visitProxy(final Proxy proxy)
    {
      addPropertiesAndDeleteMenuItems(proxy);
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    @Override
    public Object visitInstanceProxy(final InstanceProxy inst)
    {
      visitProxy(inst);
      final WatersPopupActionManager master = getMaster();
      final JPopupMenu popup = getPopup();
      final String name = inst.getModuleName();
      final ModuleProxy module = mContext.getModule();
      final IDEAction gotomod = master.getGotoModuleAction(module, name);
      popup.add(gotomod);
      final IDEAction param = master.getInsertParameterBindingAction();
      popup.add(param);
      return null;
    }

    @Override
    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    {
      visitProxy(comp);
      final WatersPopupActionManager master = getMaster();
      final JPopupMenu popup = getPopup();
      final IDEAction editgraph = master.getShowGraphAction(comp);
      popup.add(editgraph);
      if (comp.getKind() == ComponentKind.PROPERTY)
      {
        final IDEAction languageInclusion = master.getLanguageIncusionAction(comp);
        popup.add(languageInclusion);
      }
      return null;
    }

  }


  //#######################################################################
  //# Data Members
  private final ComponentsTreePopupVisitor mVisitor;
  private final ModuleContext mContext;

}
