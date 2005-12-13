package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.observer.Subject;

public interface ControlledToolbar
	extends Subject
{
    public String getCommand();
}
