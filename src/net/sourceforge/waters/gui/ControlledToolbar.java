package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.observer.Subject;
import net.sourceforge.waters.gui.ControlledSurface.Tool;

public interface ControlledToolbar
	extends Subject
{
    public Tool getCommand();
}
