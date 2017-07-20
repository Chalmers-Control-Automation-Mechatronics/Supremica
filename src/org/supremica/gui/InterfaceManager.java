//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import javax.swing.UIManager;

import org.supremica.properties.Config;


public class InterfaceManager
{
//	private static Logger logger = LoggerFactory.createLogger(InterfaceManager.class);
    private static InterfaceManager instance = null;

    private InterfaceManager()
    {
    }

    public void initLookAndFeel()
    {
      try
      {
        String lookAndFeel = Config.GENERAL_LOOKANDFEEL.getAsString();
        String clsname = null;
        if ((lookAndFeel == null) || "System".equalsIgnoreCase(lookAndFeel)) {
          clsname = UIManager.getSystemLookAndFeelClassName();
          if (clsname.equals(GTK_CLASS_NAME)) {
            // GTK looks broken, only use when explicitly requested ...
            clsname = METAL_CLASS_NAME;
          }
        } else if ("Metal".equalsIgnoreCase(lookAndFeel)) {
          clsname = METAL_CLASS_NAME;
        } else if ("Motif".equalsIgnoreCase(lookAndFeel)) {
          clsname = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        } else if ("Windows".equalsIgnoreCase(lookAndFeel)) {
          clsname = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        } else if ("Mac".equalsIgnoreCase(lookAndFeel)) {
          clsname = "javax.swing.plaf.mac.MacLookAndFeel";
        } else if ("GTK".equalsIgnoreCase(lookAndFeel)) {
          clsname = GTK_CLASS_NAME;
        } else {
          clsname = lookAndFeel;
        }
        UIManager.setLookAndFeel(clsname);
        /*
            // Debugging
            System.err.println("Chosen look and feel: " + lookAndFeel);
            System.err.println("Supported look and feels:");
            for (LookAndFeelInfo info: UIManager.getInstalledLookAndFeels())
            {
                System.out.println("  " + info.getName() + " " + info.getClassName());
            }
         */
      }
      catch (Exception ex)
      {
        System.err.println("Error while setting look and feel: " + ex);
        System.err.println("Reverting to System look and feel.");
        try
        {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception nope)
        {
          System.err.println(nope);
          System.exit(0);
        }
      }
    }

    public static synchronized InterfaceManager getInstance()
    {
        if (instance == null)
        {
            instance = new InterfaceManager();
        }

        return instance;
    }


    //########################################################################
    //# Class Constants
    private static final String GTK_CLASS_NAME =
      "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
    private static final String METAL_CLASS_NAME =
      "javax.swing.plaf.metal.MetalLookAndFeel";

}
