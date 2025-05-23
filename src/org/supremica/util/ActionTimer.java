
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.util;

import java.util.Date;

/**
 * Simulates a "stopwatch" with four buttons - start (resume), stop, reset and
 * restart.
 *   * Start resumes a stopped timer.
 *   * Stop interrupts the timer.
 *   * Reset sets the timer to 0 and stops the timer.
 *   * Restart resets the timer and starts it again.
 */
public class ActionTimer
{
    private Date startDate = null;
    private Date stopDate = null;
    
    /**
     * Creates a new timer set to 0.
     */
    public ActionTimer()
    {}
    
    /**
     * Resets the timer to 0 and stops the timer.
     */
    public void reset()
    {
        startDate = null;
        stopDate = null;
    }
    
    /**
     * Interrupts the timer. Start resumes the counting, reset sets the time to 0.
     */
    public void stop()
    {
        stopDate = new Date();
    }
    
    /**
     * Makes the timer resume ticking "from where it last
     * stopped". Starts from 0 if it hasn't been started before.
     */
    public void start()
    {
        // Not started yet?
        if (startDate == null)
        {
            startDate = new Date();
            stopDate = null;
        }
        else
        {
            // Shift "startDate" as long as the timer has been turned off
            startDate.setTime(startDate.getTime() + ((new Date()).getTime()-stopDate.getTime()));
            stopDate = null;
        }
    }
    
    /**
     * Restarts the timer from 0.
     */
    public void restart()
    {
        reset();
        start();
    }
    
    /**
     * Returns the elapsed time in milliseconds between last start and last stop call.
     * If stop is not called before this method then the time since last call
     * to start is returned
     */
    public long elapsedTime()
    throws IllegalStateException
    {
        if (startDate == null)
        {
            throw new IllegalStateException("startDate is negative");
        }
        
        if (stopDate == null)
        {
            return (new Date()).getTime() - startDate.getTime();
        }
        
        return stopDate.getTime() - startDate.getTime();
    }
    
    /**
     * Returns a nice, readable string presenting the currently elapsed time.
     */
    public String toString()
    {
        final String hours = " hours";
        final String minutes = " minutes";
        final String seconds = " seconds";
        final String milliseconds = " milliseconds";

        // Calculate time
        long time = elapsedTime();    // time is in millisecs
        int hrs = (int) (time / (60 * 60 * 1000.0));
        time = time - hrs * (60 * 60 * 1000);
        int mins = (int) (time / (60 * 1000.0));
        time = time - mins * (60 * 1000);
        int secs = (int) (time / (1000.0));
        time = time - secs * 1000;
        int millis = (int) time;
        
        // Produce message
        StringBuilder sbuf = new StringBuilder();
        if (hrs != 0)
        {
            //sbuf.append(hrs + hours + mins + minutes + secs + seconds + millis + milliseconds);
            sbuf.append(hrs + hours + " " + mins + minutes);
        }
        else if (mins != 0)
        {
            //sbuf.append(mins + minutes + secs + seconds + millis + milliseconds);
            sbuf.append(mins + minutes + " " + secs + seconds);
        }
        else if (secs != 0)
        {
            sbuf.append(secs + seconds + " " + millis + milliseconds);
        }
        else
        {
            sbuf.append(millis + milliseconds);
        }
        
        return sbuf.toString();
    }
    
    /**
     * Returns a nice, readable string presenting the currently elapsed time.
     */
    public String toStringShort()
    {
        final String hours = " h";
        final String seconds = " s";
        // Calculate time
        long time = elapsedTime();    // time is in millisecs

        // Produce message
        StringBuilder sbuf = new StringBuilder();
        if (time >= 60 * 60 * 1000.0)
        {
            // Hours
            sbuf.append((Math.round(time/(60*60*10.0))/100.0) + hours);
        }
		/*
        else if (time >= 60 * 1000.0)
        {
            // Minutes
            sbuf.append((Math.round(time/(60*10.0))/100.0) + minutes);
        }
		*/
        else
        {
            // Seconds
            sbuf.append((Math.round(time/(10.0))/100.0) + seconds);
        }
        
        return sbuf.toString();
    }
}
