package org.supremica.gui.ide;

import org.supremica.gui.LogDisplay;
import org.supremica.gui.WhiteScrollPane;


class LogPanel
    extends WhiteScrollPane
{
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
	private IDE ide;
    private String title;
    
    LogPanel(IDE ide, String title)
    {
        super(LogDisplay.getInstance().getComponentWithoutScrollPane());
        this.ide = ide;
        this.title = title;
        setPreferredSize(IDEDimensions.loggerPreferredSize);
        setMinimumSize(IDEDimensions.loggerMinimumSize);
        updateUI();
    }
    
    public String getTitle()
    {
        return title;
    }
    
}