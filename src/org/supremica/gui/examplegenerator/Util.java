//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.examplegenerator;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.StringTokenizer;

class Util
{
     public Point[] zigzagTraversing(Point[] result, int i, int x, int y, int di, int dj, int min_x, int min_y, int max_x, int max_y, boolean firstTime)
    {
        result[i] = new Point(x,y);

        if(firstTime && y+dj > max_y)
        {
            di=1;
            dj=0;
        }

        x += di;
        y += dj;

        if(x+1 > max_x && y+1 > max_y)
        {
            result[i+1] = new Point(x,y);
            return result;
        }

        if(x-1 < min_x)
        {
            if(di == 0)
            {
                di=1;
                dj=-1;
            }
            else
            {
                di=0;
                dj=1;

                if(y == max_y)
                {
                    di=1;
                    dj=0;
                }
            }
        }
        else if(y+1 > max_y)
        {
            if(dj == 0)
            {
                di=1;
                dj=-1;
            }
            else
            {
                di=1;
                dj=0;
            }
        }

        if(y-1 < min_y)
        {
            if(dj == 0)
            {
                di=-1;
                dj=1;
            }
            else
            {
                di=1;
                dj=0;

                if(x == max_x)
                {
                    di=0;
                    dj=1;
                }
            }

        }
        else if(x+1 > max_x)
        {
            if(di == 0)
            {
                di=-1;
                dj=1;
            }
            else
            {
                di=0;
                dj=1;
            }
        }

       zigzagTraversing(result,i+1,x,y,di,dj,min_x,min_y,max_x,max_y, false);

	return null;
    }    

    public Point[] verticalTraversing(Point[] result, int i, int x, int y, int min_x, int min_y, int max_x, int max_y)
    {
        result[i] = new Point(x,y);

        y++;

        if(y > max_y)
        {
            x++;
            y = min_y;
            if(x > max_x)
            {
                return result;
            }
            else
                verticalTraversing(result, i+1, x, y, min_x, min_y, max_x, max_y);
        }
        else
            verticalTraversing(result, i+1, x, y, min_x, min_y, max_x, max_y);

        return null;
    }

    public void writeToFile(BufferedWriter bw, String text, boolean tokenizable) throws Exception
    {
        if(tokenizable)
        {
            StringTokenizer st = new StringTokenizer(text,"\n");
            String token;
            while(st.hasMoreTokens())
            {
                token = st.nextToken();
                bw.newLine();
                try { bw.write(token); } 
                catch (IOException e) {}
            }

        }
        else
        {
            try { bw.write(text); } 
            catch (IOException e) {}
        }
    }
}
