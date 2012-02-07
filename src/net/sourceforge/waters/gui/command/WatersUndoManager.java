package net.sourceforge.waters.gui.command;

import javax.swing.undo.UndoManager;

public class WatersUndoManager extends UndoManager
{

  public void removeLast(){
    final int index = edits.indexOf(lastEdit());
    trimEdits(index, index);
  }

  public Command getLastCommand(){
    final UndoableCommand undo = (UndoableCommand) lastEdit();
    return undo.getCommand();
  }

  private static final long serialVersionUID = 1L;

}
