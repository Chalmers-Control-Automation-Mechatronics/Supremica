//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ModuleContainer
//###########################################################################
//# $Id: DocumentContainer.java,v 1.4 2007-06-26 20:45:14 robi Exp $
//###########################################################################


package org.supremica.gui.ide;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
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

    //#######################################################################
    //# To be Overriden by Subclasses
	public boolean hasUnsavedChanges()
	{
		return false;
	}

	public void setCheckPoint()
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
		if (name != null && !name.equals("")) {
			buffer.append(": ");
			buffer.append(name);
		}
		if (file != null) {
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
		try {
			return doc.getFileLocation();
		} catch (final MalformedURLException exception) {
			return null;
		}
	}


    //#######################################################################
    //# Data Members
    private final DocumentProxy mDocument;
    private final IDE mIDE;

}