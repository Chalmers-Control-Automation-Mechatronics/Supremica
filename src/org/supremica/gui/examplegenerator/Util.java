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
