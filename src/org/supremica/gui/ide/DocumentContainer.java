//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ModuleContainer
//###########################################################################
//# $Id: DocumentContainer.java,v 1.1 2007-06-20 19:43:38 flordal Exp $
//###########################################################################


package org.supremica.gui.ide;

import javax.swing.JTabbedPane;
import net.sourceforge.waters.model.base.DocumentProxy;

public abstract class DocumentContainer 
{
    private final DocumentProxy mDocument;
    private final IDE mIDE;
    
    //#######################################################################
    //# Constructor
    public DocumentContainer(final IDE ide, final DocumentProxy document)
    {
        mIDE = ide;
        mDocument = document;
    }
        
    //#######################################################################
    //# Simple Access
    public IDE getIDE()
    {
        return mIDE;
    }
    
    public String getName()
    {
        return mDocument.getName();
    }
    
    public DocumentProxy getDocument()
    {
        return mDocument;
    }

    public abstract void updateActiveTab(JTabbedPane tabPanel);
    
    public abstract void addToTabPanel(JTabbedPane tabPanel);
    
    public abstract EditorPanel getEditorPanel();

    public abstract AnalyzerPanel getAnalyzerPanel();
    
    public void rememberSelectedComponent(JTabbedPane tabPanel) {}

    public void restoreSelectedComponent(JTabbedPane tabPanel) {}
}