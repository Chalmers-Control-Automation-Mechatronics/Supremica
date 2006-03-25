
package org.supremica.gui.ide.actions;

import javax.swing.AbstractButton;
import javax.swing.AbstractAction;
import java.util.List;

public abstract class IDEAction
	extends AbstractAction
{
	private AbstractButton theButton = null;
	protected IDEActionInterface ide;
	private int minimumNumberOfSelectedComponents = 0;
	private boolean editorActiveRequired = false;
	private boolean analyzerActiveRequired = false;

	public IDEAction(List<IDEAction> actionList)
	{
		this.ide = null;

		assert(actionList != null);
		actionList.add(this);
	}

	public void setIDEActionInterface(IDEActionInterface ide)
	{
		this.ide = ide;
	}

	public abstract void doAction();

	public void setButton(AbstractButton theButton)
	{
		this.theButton = theButton;
	}

	public AbstractButton getButton()
	{
		return theButton;
	}

	public void setEditorActiveRequired(boolean required)
	{
		editorActiveRequired = required;
	}

	public void setAnalyzerActiveRequired(boolean required)
	{
	    analyzerActiveRequired = required;
	}

	public void setMinimumNumberOfSelectedComponents(int numberOfComponents)
	{
		minimumNumberOfSelectedComponents = numberOfComponents;
	}

	public boolean isEnabled()
	{
		// TO Do 
		/*
		if (editorActiveRequired)
		{
			if (!ide.editorActive())
			{
				return false;
			}
		}
		if (analyzerActiveRequired)
		{
			if (!ide.analyzerActive())
			{
				return false;
			}
		}
		
		if (minimumNumberOfSelectedComponents > ide.numberOfSelectedComponents())
		{
			return false;
		}
		*/
		return super.isEnabled();
	}
}
