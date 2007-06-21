
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   WmodFileFilter
//###########################################################################
//# $Id: SupremicaXMLFileFilter.java,v 1.1 2007-06-21 11:03:54 flordal Exp $
//###########################################################################
package org.supremica.gui;

import java.io.File;
import javax.swing.filechooser.*;

public class SupremicaXMLFileFilter
    extends FileFilter
{
    public static final String SUPXML = "xml";
    
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
}
