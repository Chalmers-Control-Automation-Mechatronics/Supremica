//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   WmodFileFilter
//###########################################################################
//# $Id: WmodFileFilter.java,v 1.7 2007-06-21 20:56:53 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;


public class WmodFileFilter
    extends FileFilter
{

	//#######################################################################
	//# Singleton Pattern
	public static WmodFileFilter getInstance()
	{
		return theInstance;
	}

	private WmodFileFilter()
	{
	}


	//#######################################################################
	//# Overrides for Base Class javax.swing.filechooser.FileFilter
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
    
    public boolean accept(File f)
    {
        if (f.isDirectory())
        {
            return true;
        }
        
        String extension = getExtension(f);
        if (extension != null)
        {
            if (extension.equals(WMOD))
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
        return "Waters Module Files";
    }


	//#######################################################################
	//# Class Constants
    public static final String WMOD = "wmod";

    private static final WmodFileFilter theInstance = new WmodFileFilter();
    
}
