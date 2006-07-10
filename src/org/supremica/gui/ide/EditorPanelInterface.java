package org.supremica.gui.ide;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import javax.swing.DefaultListModel;

public interface EditorPanelInterface
{
	public void addComponent();
	public void addComponent(AbstractSubject component);
	public ModuleSubject getModuleSubject();
	public DefaultListModel getEventDataList();

//	public IndexedList<EventDeclSubject> getEventDeclListModifiable();
//	public ExpressionParser getExpressionParser();
}
