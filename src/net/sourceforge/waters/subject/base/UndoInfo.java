//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   UndoInfo
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;


/**
 * An undo information record used to support arbitrary assignments of
 * {@link Subject}s.
 *
 * @author Robi Malik
 */

public interface UndoInfo
{

  public enum Mode {
    UNDO,
    REDO
  }

}
