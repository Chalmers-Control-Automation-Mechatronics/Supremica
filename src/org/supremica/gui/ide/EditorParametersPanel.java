package org.supremica.gui.ide;

import javax.swing.*;
import org.supremica.gui.WhiteScrollPane;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.gui.ParameterListCell;


class EditorParametersPanel
	extends WhiteScrollPane
{
	private static final long serialVersionUID = 1L;

	private ModuleContainer moduleContainer;
	private String name;

	EditorParametersPanel(ModuleContainer moduleContainer, String name)
	{
		this.moduleContainer = moduleContainer;
		this.name = name;

		createContentPane();

		setPreferredSize(IDEDimensions.leftEditorPreferredSize);
		setMinimumSize(IDEDimensions.leftEditorMinimumSize);
	}

	public String getName()
	{
		return name;
	}

	private void createContentPane()
	{
		ModuleProxy module = moduleContainer.getModule();
		DefaultListModel paramData = new DefaultListModel();

		if (module != null)
		{
			for (int i = 0; i < module.getParameterList().size(); i++)
			{
				paramData.addElement(((ParameterProxy) (module.getParameterList().get(i))));
			}
		}

		JList paramdataList = new JList(paramData);
		paramdataList.setCellRenderer(new ParameterListCell());
		getViewport().add(paramdataList);

	}
}

