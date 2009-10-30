//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ModuleContainer
//###########################################################################
//# $Id$
//###########################################################################


package org.supremica.gui.ide;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;

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
    

    //#######################################################################
    //# To be Overriden by Subclasses
    public boolean hasUnsavedChanges()
    {
        return false;
    }

    public void setCheckPoint()
    {
    }

	/**
	 * Cleans up. This method is called by the GUI to notify that the
	 * document of this container has been closed by the user. It should
	 * unregister all listeners on external components and perform any
	 * other cleanup that may be necessary. The component does not have to
	 * support any other methods once <CODE>close()</CODE> has been called.
	 */
	public void close()
	{
	}
    
    public abstract Component getPanel();
    
    public abstract EditorPanel getEditorPanel();
    
    public abstract AnalyzerPanel getAnalyzerPanel();
    
    public abstract boolean isEditorActive();
    
    public abstract boolean isAnalyzerActive();
    
    public abstract String getTypeString();    

    
    //#######################################################################
    //# Titling
    public File getFileLocation()
    {
        final DocumentProxy doc = getDocument();
        return getFileLocation(doc);
    }
    
    public String getWindowTitle()
    {
        final String type = getTypeString();
        final DocumentProxy doc = getDocument();
        final String name = doc.getName();
        final File file = getFileLocation();
        final StringBuffer buffer = new StringBuffer(type);
        if (name != null && !name.equals(""))
        {
            buffer.append(": ");
            buffer.append(name);
        }
        if (file != null)
        {
            buffer.append(" [");
            buffer.append(file);
            buffer.append(']');
        }
        return buffer.toString();
    }
    
    
    //#######################################################################
    //# Auxiliary Static Access
    static File getFileLocation(final DocumentProxy doc)
    {
        try
        {
            return doc.getFileLocation();
        }
        catch (final MalformedURLException exception)
        {
            return null;
        }
    }
    
    
    //#######################################################################
    //# Data Members
    private final DocumentProxy mDocument;
    private final IDE mIDE;
    
}