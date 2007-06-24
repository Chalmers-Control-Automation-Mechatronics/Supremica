//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ModuleContainer
//###########################################################################
//# $Id: DocumentContainer.java,v 1.2 2007-06-24 18:40:06 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.awt.Component;
import javax.swing.JTabbedPane;

import net.sourceforge.waters.model.base.DocumentProxy;


public abstract class DocumentContainer
{

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

    public abstract Component getPanel();

    public abstract EditorPanel getEditorPanel();

    public abstract AnalyzerPanel getAnalyzerPanel();

	public abstract boolean isEditorActive();

	public abstract boolean isAnalyzerActive();


    //#######################################################################
    //# Data Members
    private final DocumentProxy mDocument;
    private final IDE mIDE;

}