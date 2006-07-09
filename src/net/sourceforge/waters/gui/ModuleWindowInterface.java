package net.sourceforge.waters.gui;

import java.awt.event.ActionListener;

import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

public interface ModuleWindowInterface extends ActionListener{

	ModuleSubject getModuleSubject();

	EditorWindowInterface showEditor(SimpleComponentSubject scp);

}
