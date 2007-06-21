//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui
//# CLASS:   SupremicaXMLFileFilter
//###########################################################################
//# $Id: SupremicaXMLFileFilter.java,v 1.2 2007-06-21 20:56:53 robi Exp $
//###########################################################################


package org.supremica.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;


public class SupremicaXMLFileFilter
    extends FileFilter
{

	//#######################################################################
	//# Singleton Pattern
	public static SupremicaXMLFileFilter getInstance()
	{
		return theInstance;
	}

	private SupremicaXMLFileFilter()
	{
	}

    
	//#######################################################################
	//# Overrides for Base Class javax.swing.filechooser.FileFilter
    public boolean accept(File f)
    {
        if (f.isDirectory())
        {
            return true;
        }
        
        String extension = getExtension(f);
        if (extension != null)
        {
            if (extension.equals(SUPXML))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        
        return false;
    }
    
    public String getDescription()
    {
        return "Supremica Project Files";
    }


	//#######################################################################
	//# Static Class Methods
    public static String getExtension(File f)
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        
        if ((i > 0) && (i < s.length() - 1))
        {
            ext = s.substring(i + 1).toLowerCase();
        }
        
        return ext;
    }


	//#######################################################################
	//# Class Constants
    public static final String SUPXML = "xml";

    private static final SupremicaXMLFileFilter theInstance =
		new SupremicaXMLFileFilter();

}
