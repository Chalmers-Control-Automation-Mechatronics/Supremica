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

package net.sourceforge.waters.gui.command;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.SubjectTools;
import net.sourceforge.waters.subject.module.LabelBlockSubject;


/**
 * <P>The general superclass for the generic commands that operate
 * on a selection owner.</P>
 *
 * <P>In addition to the panel (implementing the {@link SelectionOwner}
 * interface), this class stores a changeable descriptive name and a flag
 * to indicate whether the command should update the panel's selection. The
 * latter is useful, because in some cases a command is placed in a
 * compound that does its own selection handling, so the selection handling
 * features built into the individual commands needs to be disabled.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractEditCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new edit command that updates the selection.
   * @param  panel             The panel that controls the operation of this
   *                           command.
   */
  public AbstractEditCommand(final SelectionOwner panel)
  {
    this(panel, null, true);
  }

  /**
   * Creates a new edit command that updates the selection.
   * @param  panel             The panel that controls the operation of this
   *                           command.
   * @param  name              The description of the command.
   */
  public AbstractEditCommand(final SelectionOwner panel,
                             final String name)
  {
    this(panel, name, true);
  }

  /**
   * Creates a new edit command.
   * @param  panel             The panel that controls the operation of this
   *                           command.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panel when
   *                           executed.
   */
  public AbstractEditCommand(final SelectionOwner panel,
                             final boolean updatesSelection)
  {
    this(panel, null, updatesSelection);
  }

  /**
   * Creates a new edit command.
   * @param  panel             The panel that controls the operation of this
   *                           command.
   * @param  name              The description of the command.
   * @param  updatesSelection  A flag, indicating whether this command should
   *                           update the selection of the panel when
   *                           executed.
   */
  public AbstractEditCommand(final SelectionOwner panel,
                             final String name,
                             final boolean updatesSelection)
  {
    mPanel = panel;
    mName = name;
    mUpdatesSelection = updatesSelection;
  }


  //#########################################################################
  //# Simple Access
  public SelectionOwner getPanel()
  {
    return mPanel;
  }

  public void setName(final String name)
  {
    mName = name;
  }

  public boolean getUpdatesSelection()
  {
    return mUpdatesSelection;
  }

  public void setUpdatesSelection(final boolean updatesSelection)
  {
    mUpdatesSelection = updatesSelection;
  }

  public List<ProxySubject> getSelectionAfterInsert()
  {
    return Collections.emptyList();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public String getName()
  {
    return mName != null ? mName : getClass().getName();
  }

  public boolean isSignificant()
  {
    return true;
  }


  //#########################################################################
  //# Auxiliary Methods
  List<ProxySubject> getSelectionAfterInsert(final List<InsertInfo> inserts)
  {
    final int size = inserts.size();
    final Set<Proxy> set = new THashSet<Proxy>(size);
    final List<ProxySubject> result = new ArrayList<ProxySubject>(size);
    boolean newLabelBlock = true;
    LabelBlockSubject block = null;
    for (final InsertInfo insert : inserts) {
      final Proxy proxy = insert.getProxy();
      final Subject subject = (Subject) proxy;
      //only bother if it is still possible that its a new labelblock
      if (newLabelBlock) {
        if (block == null) {
          block = SubjectTools.getAncestor(subject, LabelBlockSubject.class);
        } else if (SubjectTools.isAncestor(block, subject)) {
           newLabelBlock = false;
        }
        if (block == null || block.getEventIdentifierList().size() != size) {
          newLabelBlock = false;
        }
      }
      final Proxy ancestor = mPanel.getSelectableAncestor(proxy);
      if (ancestor != null && set.add(ancestor)) {
        final ProxySubject ancestorSubject = (ProxySubject) ancestor;
        result.add(ancestorSubject);
      }
    }
    if (newLabelBlock) {
      return Collections.singletonList((ProxySubject) block);
    }
    return result;
  }

  List<Proxy> getSelectionAfterDelete(final List<InsertInfo> deletes)
  {
    final int size = deletes.size();
    final Set<Proxy> set = new THashSet<Proxy>(size);
    final List<Proxy> result = new LinkedList<Proxy>();
    for (final InsertInfo delete : deletes) {
      final Proxy proxy = delete.getProxy();
      if (proxy instanceof AbstractSubject) {
        final AbstractSubject subject = (AbstractSubject) proxy;
        final AbstractSubject parent =
          (AbstractSubject) SubjectTools.getProxyParent(subject);
        final Proxy ancestor = mPanel.getSelectableAncestor(parent);
        if (ancestor != null && ancestor != proxy && set.add(ancestor)) {
          result.add(ancestor);
        }
      }
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final SelectionOwner mPanel;
  private String mName;
  private boolean mUpdatesSelection;

}









