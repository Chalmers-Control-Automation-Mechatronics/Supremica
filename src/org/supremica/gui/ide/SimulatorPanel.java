package org.supremica.gui.ide;

import org.supremica.gui.WhiteScrollPane;

class SimulatorPanel
    extends WhiteScrollPane
{
    private static final long serialVersionUID = 1L;
    
    private DocumentContainer documentContainer;
    private String name;
    
    SimulatorPanel(DocumentContainer moduleContainer, String name)
    {
        this.documentContainer = moduleContainer;
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
}