//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorLabelBlockPopupMenu
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

import org.supremica.util.VPopupMenu;

import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;

/**
 * Popup for editing attributes of a label block.
 *
 * @author Martin Byr&ouml;d
 */

class EditorLabelBlockPopupMenu
  extends VPopupMenu
  implements ActionListener
{

  //#########################################################################
  //# Constructor
  public EditorLabelBlockPopupMenu(final EditorWindowInterface root,
                                   final LabelBlockSubject block)
  {
    mRoot = root;
    mEdge = (EdgeSubject) block.getParent();
    mEditEdgeItem = new JMenuItem("Edit guard/actions");
    mEditEdgeItem.addActionListener(this);
    add(mEditEdgeItem);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event) 
  {
    if (event.getSource() == mEditEdgeItem) {
      final ModuleWindowInterface root = mRoot.getModuleWindowInterface();
      EditorEditEdgeDialog.showDialog(mEdge, root);
    }
  }


  //#########################################################################
  //# Data Members
  private final EdgeSubject mEdge;
  private final EditorWindowInterface mRoot;
  private JMenuItem mEditEdgeItem;

}
