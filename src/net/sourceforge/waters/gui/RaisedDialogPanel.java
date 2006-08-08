//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   RaisedDialogPanel
//###########################################################################
//# $Id: RaisedDialogPanel.java,v 1.1 2006-08-08 23:59:21 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;


/**
 * An auxiliary panel class that represents a standard {@link JPanel}
 * with a raised bevel border and some additional space at its inside.
 * @author Robi Malik
 */

public class RaisedDialogPanel extends JPanel
{

  //#########################################################################
  //# Constructor
  public RaisedDialogPanel()
  {
    this(4);
  }

  public RaisedDialogPanel(final int spc)
  {
    final Border outer = BorderFactory.createRaisedBevelBorder();
    final Border inner = BorderFactory.createEmptyBorder(spc, spc, spc, spc);
    final Border compound = BorderFactory.createCompoundBorder(outer, inner);
    setBorder(compound);
  }

}
