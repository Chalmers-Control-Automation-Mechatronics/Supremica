//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ModuleContainers
//###########################################################################
//# $Id: ModuleContainers.java,v 1.10 2007-06-20 19:43:38 flordal Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.subject.module.ModuleSubject;


public class ModuleContainers
{
    private IDE ide;
    private List<DocumentContainer> documentContainers =
        new LinkedList<DocumentContainer>();
    private DocumentContainer activeDocumentContainer = null;
    
    private int newModuleCounter = 1;
    
    public ModuleContainers(IDE ide)
    {
        this.ide = ide;
    }
    
    public DocumentContainer getDocumentContainer(final String name)
    {
        for (final DocumentContainer currDocumentContainer : documentContainers)
        {
            if (name.equals(currDocumentContainer.getName()))
            {
                return currDocumentContainer;
            }
        }
        return null;
    }
    
    public void add(DocumentContainer documentContainer)
    {
        documentContainers.add(documentContainer);
    }
    
    public void remove(DocumentContainer documentContainer)
    {
        documentContainers.remove(documentContainer);
        if (documentContainer == activeDocumentContainer)
        {
            activeDocumentContainer = null;
        }
    }    
    
    void setActive(DocumentContainer documentContainer)
    {        
        if (getActiveModuleContainer() != documentContainer)
        {
            activeDocumentContainer = documentContainer;
        }
        ide.setTitle(ide.getName() + " [" + documentContainer.getName() + "]");
    }
    
    
    public DocumentContainer getActiveModuleContainer()
    {
        return activeDocumentContainer;
    }
    
    
    public DocumentContainer getFirst()
    {
        return (DocumentContainer) documentContainers.get(0);
    }
    
    
    public DocumentContainer getLast()
    {
        final int index = documentContainers.size() - 1;
        return (DocumentContainer) documentContainers.get(index);
    }
    
    
    public DocumentContainer getNext(DocumentContainer documentContainer)
    {
        int moduleIndex = documentContainers.indexOf(documentContainer);
        int nextModuleIndex = moduleIndex + 1;
        if (nextModuleIndex == size())
        {
            nextModuleIndex = 0;
        }
        if (size() >= 1)
        {
            return (DocumentContainer)documentContainers.get(nextModuleIndex);
        }
        return null;
    }
    
    
    public int size()
    {
        return documentContainers.size();
    }
    
    
    public Iterator<DocumentContainer> iterator()
    {
        return documentContainers.iterator();
    }
        
    public String getNewModuleName(String prefix)
    {
        String nameSuggestion = prefix + newModuleCounter++;
        while (getDocumentContainer(nameSuggestion) != null)
        {
            nameSuggestion = prefix + newModuleCounter++;
        }
        return nameSuggestion;        
    }
    
    public ModuleSubject createNewModuleSubject()
    {
        final String name = getNewModuleName("Module");
        final ModuleSubject newModule = new ModuleSubject(name, null);
        return newModule;
    }
    
    
    public ModuleContainer createNewModuleContainer()
    {
        final ModuleSubject newModule = createNewModuleSubject();
        final ModuleContainer newModuleContainer =
            new ModuleContainer(ide, newModule);
        return newModuleContainer;
    }    
}
