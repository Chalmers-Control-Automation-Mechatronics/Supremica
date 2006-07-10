package org.supremica.gui.ide;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.model.expr.ExpressionParser;

public interface EditorPanelInterface
{
	public void addComponent();
	public void addComponent(AbstractSubject component);
//	public ExpressionParser getExpressionParser();
}
