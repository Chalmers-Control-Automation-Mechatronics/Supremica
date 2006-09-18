//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   MainPanel
//###########################################################################
//# $Id: EditorPanelInterface.java,v 1.9 2006-09-18 10:57:23 knut Exp $
//###########################################################################

package org.supremica.gui.ide;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;


public interface EditorPanelInterface
{

	/**
	 * Opens up a component dialog and allows the user to create a new
	 * component.
	 */
	public void addComponent();

	public void addComponent(AbstractSubject component);

	public void addEvent();

	public ModuleSubject getModuleSubject();

	public boolean componentNameAvailable(String name);

}
