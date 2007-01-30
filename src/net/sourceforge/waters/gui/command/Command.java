package net.sourceforge.waters.gui.command;

public interface Command
{
    public void execute();
    
    public void undo();
    
    public String getName();
    
    // What does this method do?
    public boolean isSignificant();
}
