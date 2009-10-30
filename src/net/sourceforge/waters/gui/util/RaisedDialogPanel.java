//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.util
//# CLASS:   RaisedDialogPanel
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.util;

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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
