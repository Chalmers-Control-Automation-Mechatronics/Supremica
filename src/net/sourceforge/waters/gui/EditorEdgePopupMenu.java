//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorEdgePopupMenu
//###########################################################################
//# $Id: EditorEdgePopupMenu.java,v 1.9 2007-05-23 16:28:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

import org.supremica.util.VPopupMenu;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteEdgeCommand;
import net.sourceforge.waters.gui.command.FlipEdgeCommand;
import net.sourceforge.waters.subject.module.EdgeSubject;


/**
 * Popup menu for editing edges.
 *
 * @author Hugo Flordal, Martin Byr&ouml;d
 */

class EditorEdgePopupMenu
  extends VPopupMenu
  implements ActionListener
{

  //#########################################################################
  //# Constructors
  public EditorEdgePopupMenu(final EditorWindowInterface root,
			     final EdgeSubject edge)
  {
    mRoot = root;
    mEdge = edge;

    mEditEdgeItem = new JMenuItem("Edit guard/actions");
    mEditEdgeItem.addActionListener(this);
    add(mEditEdgeItem);
    mDeleteItem = new JMenuItem("Delete edge");
    mDeleteItem.addActionListener(this);
    add(mDeleteItem);
    mRecallItem = new JMenuItem("Recall label");
    mRecallItem.setEnabled(false);
    //mRecallItem.addActionListener(this);
    add(mRecallItem);
    mFlipItem = new JMenuItem("Flip edge");
    mFlipItem.addActionListener(this);
    add(mFlipItem);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event) 
  {
    if (event.getSource() == mEditEdgeItem) {
      final ModuleWindowInterface root = mRoot.getModuleWindowInterface();
      EditorEditEdgeDialog.showDialog(mEdge, root);
    } else if (event.getSource() == mDeleteItem) {
      final Command deleteEdge =
	new DeleteEdgeCommand(mRoot.getControlledSurface().getGraph(), mEdge);
      mRoot.getUndoInterface().executeCommand(deleteEdge);
    } else if (event.getSource() == mRecallItem) {
      // *** BUG ***
      // not yet implemented
      // parent.getLabelGroup(edge).setOffset
      //   (EditorLabelGroup.DEFAULTOFFSETX, EditorLabelGroup.DEFAULTOFFSETY);
    } else if (event.getSource() == mFlipItem) {
      final Command flipEdge = new FlipEdgeCommand(mEdge);
      mRoot.getUndoInterface().executeCommand(flipEdge);
    }
  }


  //#########################################################################
  //# Data Members
  private final EdgeSubject mEdge;
  private final EditorWindowInterface mRoot;
  private JMenuItem mEditEdgeItem;
  private JMenuItem mDeleteItem;
  private JMenuItem mRecallItem;
  private JMenuItem mFlipItem;

}