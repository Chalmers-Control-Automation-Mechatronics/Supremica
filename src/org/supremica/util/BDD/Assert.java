

package org.supremica.util.BDD;

public class Assert {

    public static void fatal(String s) {
	System.err.println("FATAL ERROR: " + s);
	System.exit(20);
    }
    public static void warning(String s) {
	System.err.println("WARNING: " + s);
    }

    public static void debug(String s) {
	if(Options.debug_on)
	    System.err.println("DEBUG: " + s);
    }
    public static void assert(boolean condition, String msg) {
	if(!condition) fatal(msg);
    }
}
