//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.animators.scenebeans;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.ic.doc.scenebeans.animation.parse.XMLAnimationParser;

public class AnimationItem
{
    private static Logger logger = LogManager.getLogger(AnimationItem.class);

    private final String description;
    private final URL url;

    public AnimationItem(final String description, final URL url)
    {
        this.description = description;
        this.url = url;
    }

    public String getDescription()
    {
        return description;
    }

    public URL getURL()
    {
        return url;
    }

    public Animator createInstance()
    throws Exception
    {
        logger.debug("createInstance no url");
        return AnimationItem.createInstance(url);
    }

    public static Animator createInstance(final URL url)
    throws Exception
    {
        logger.debug("createInstance url: " + url);
        try
        {
            final Animator view = new Animator(" Path: " + url.toString());
                        /*
                        URL url = AnimationItem.class.getResource(path);

                        if (url == null)
                        { // The class loader could not find the file

                        }
                        */
            final XMLAnimationParser parser = new XMLAnimationParser(url, view._canvas);

            view.setAnimation(parser.parseAnimation());

            return view;
        }
        catch (final Exception ex)
        {
            System.err.println(ex);
            ex.printStackTrace();

            throw ex;
        }
    }
}
