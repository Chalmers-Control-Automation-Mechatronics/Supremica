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
package org.supremica.properties;

public class LocalSystem
{
    private LocalSystem()
    {
        // Do not instantiate this class
    }
    
    /**
     * @return <code>true</code>, if the application is running on Mac OS 8/9, <code>false</code> otherwise
     */
    public static boolean isMacOS()
    {
        return com.muchsoft.util.Sys.isMacOS();
    }
    
    /**
     * @return <code>true</code>, if the application is running on Mac OS X, <code>false</code> otherwise
     */
    public static boolean isMacOSX()
    {
        return com.muchsoft.util.Sys.isMacOSX();
    }
    
    /**
     * @return <code>true</code>, if the application is running on a Mac (OS 8, 9 or X), <code>false</code> otherwise
     */
    public static boolean isAMac()
    {
        return com.muchsoft.util.Sys.isAMac();
    }
    
    /**
     * @return <code>true</code>, if the application is running on Linux, <code>false</code> otherwise
     */
    public static boolean isLinux()
    {
        return com.muchsoft.util.Sys.isLinux();
    }
    
    /**
     * @return <code>true</code>, if the application is running on Windows, <code>false</code> otherwise
     */
    public static boolean isWindows()
    {
        return com.muchsoft.util.Sys.isWindows();
    }
    
    /**
     * @return <code>true</code>, if the application is running on OS/2, <code>false</code> otherwise
     */
    public static boolean isOS2()
    {
        return com.muchsoft.util.Sys.isOS2();
    }
    
    /**
     * The home directory contains the user's data and applications. On UNIX systems this directory is denoted
     * by <code>~</code> and can be queried through the system property <code>user.home</code>.
     * @return the user's home directory without a trailing path separator
     */
    public static String getHomeDirectory()
    {
        return com.muchsoft.util.Sys.getHomeDirectory();
    }
    
    /**
     * The directory from which the application was launched is called the working directory. Its path can
     * be queried through the system property <code>user.dir</code>.
     * @return the application's working directory without a trailing path separator
     */
    public static String getWorkingDirectory()
    {
        return com.muchsoft.util.Sys.getWorkingDirectory();
    }
    
    /**
     * The preferences directory contains the user's configuration files. On Mac OS X, this method returns
     * <code>~/Library/Preferences</code>, on all other systems the user's home directory is used.
     * @return the user's preferences directory without a trailing path separator
     */
    public static String getPrefsDirectory()
    {
        return com.muchsoft.util.Sys.getPrefsDirectory();
    }
    
    /**
     * The local preferences directory contains configuration files that are shared by all users on the computer.
     * On Mac OS X, this method returns <code>/Library/Preferences</code>, on Linux <code>/etc</code>. On all
     * other systems the application's working directory is used.
     * <i>Please note: There is no guarantee that your application has permission to use this directory!</i>
     * @return the shared preferences directory (without a trailing path separator) of all users on a local computer
     */
    public static String getLocalPrefsDirectory()
    {
        return com.muchsoft.util.Sys.getLocalPrefsDirectory();
    }
    
    /**
     * The Java home directory contains the <code>bin</code> subdirectory and is needed to invoke the Java tools
     * at runtime. It is specified by the environment variable <code>$JAVA_HOME</code> and can be queried through
     * the system property <code>java.home</code>. If the variable is not set properly, this method returns
     * <code>/Library/Java/Home</code> on Mac OS X.
     * @return the Java home directory without a trailing path separator
     */
    public static String getJavaHome()
    {
        return com.muchsoft.util.Sys.getJavaHome();
    }
    
}
