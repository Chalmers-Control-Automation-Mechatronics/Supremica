package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.observer.Subject;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public interface UndoInterface extends Subject
{
    /** calls the commands execution function then if it is undoable
     * adds it to the undo list
     */
    public void executeCommand(Command c);

    public void addUndoable(UndoableCommand e);

    public boolean canRedo();

    public boolean canUndo();

    public void clearList();

    public String getRedoPresentationName();

    public String getUndoPresentationName();

    public void redo() throws CannotRedoException;

    public void undo() throws CannotUndoException;

    public void undoAndRemoveLastCommand() throws CannotUndoException;

    public Command getLastCommand();

    public void removeLastCommand();

}
