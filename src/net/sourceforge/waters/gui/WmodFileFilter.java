//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   WmodFileFilter
//###########################################################################
//# $Id: WmodFileFilter.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

public class WmodFileFilter extends FileFilter {

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("wmod")) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public String getDescription() {
        return "Waters Module Files";
    }
}
