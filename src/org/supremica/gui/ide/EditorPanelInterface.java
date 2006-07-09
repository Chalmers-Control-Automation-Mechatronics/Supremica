package org.supremica.gui.ide;

import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.model.expr.ExpressionParser;

public interface EditorPanelInterface
{
	public void addComponent();
	public void addComponent(SimpleComponentSubject component);
//	public ExpressionParser getExpressionParser();
}
