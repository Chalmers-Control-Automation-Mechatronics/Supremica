//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   AbstractEditCommand
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.SubjectTools;


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
  List<Proxy> getSelectionAfterInsert(final List<InsertInfo> inserts)
  {
    final int size = inserts.size();
    final Set<Proxy> set = new HashSet<Proxy>(size);
    final List<Proxy> result = new ArrayList<Proxy>(size);
    for (final InsertInfo insert : inserts) {
      final Proxy proxy = insert.getProxy();
      final Proxy ancestor = mPanel.getSelectableAncestor(proxy);
      if (ancestor != null && set.add(ancestor)) {
        result.add(ancestor);
      }
    }
    return result;
  }

  List<Proxy> getSelectionAfterDelete(final List<InsertInfo> deletes)
  {
    final int size = deletes.size();
    final Set<Proxy> set = new HashSet<Proxy>(size);
    final List<Proxy> result = new LinkedList<Proxy>();
    for (final InsertInfo delete : deletes) {
      final Proxy proxy = delete.getProxy();
      final AbstractSubject subject = (AbstractSubject) proxy;
      final AbstractSubject parent =
        (AbstractSubject) SubjectTools.getProxyParent(subject);
      final Proxy ancestor = mPanel.getSelectableAncestor(parent);
      if (ancestor != null && ancestor != proxy && set.add(ancestor)) {
        result.add(ancestor);
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
