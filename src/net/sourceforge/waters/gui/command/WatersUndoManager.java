package net.sourceforge.waters.gui.command;

import javax.swing.undo.UndoManager;

public class WatersUndoManager extends UndoManager
{

  public void removeLast(){
    final int index = edits.indexOf(lastEdit());
    trimEdits(index, index);
  }

  public Command getLastCommand(){
    return mLastCommand;
  }

  @Override
  public void undo(){
    super.undo();
    mLastCommand = null;
  }

  public boolean addCommand(final UndoableCommand anEdit){
    mLastCommand = anEdit.getCommand();
    return super.addEdit(anEdit);
  }

  private Command mLastCommand;
  private static final long serialVersionUID = 1L;

}
