package org.supremica.util;

import java.util.Collection;

/**
 * Utility methods for common argument validations.
 *
 *<P> Replace <code>if</code> statements at the start of a method with
 * more compact method calls.
 *
 * @used.By large number of classes in this application.
 * @author <a href="http://www.javapractices.com/">javapractices.com</a>
 *
 * Modified by Knut.
 */
public final class Args
{
    /**
     * If <code>aObject</code> is null, throw a <code>NullPointerException</code>.
     *
     * <P>Use cases :
  <pre>
   doSomething( SoccerBall aBall ){
     //call some method on the argument :
     //if aBall is null, then exception is automatically thrown, so
     //there is no need for an explicit check for null.
     aBall.inflate();
   
     //assign to a corresponding field (common in constructors):
     //if aBall is null, no exception is immediately thrown, so
     //an explicit check for null may be useful here
     Args.checkForNull( aBall );
     fBall = aBall;
   
     //passed on to some other method as param :
     //it may or may not be appropriate to have an explicit check
     //for null here, according the needs of the problem
     Args.checkForNull( aBall ); //??
     fReferee.verify( aBall );
   }
   </pre>
     */
    public static void checkForNull(Object aObject)
    {
        if ( aObject == null )
        {
            throw new NullPointerException();
        }
    }

     /**
     * Throws an <code>IllegalArgumentException</code> if <code>aObject</code> is
     * not of type theClass.
     *
     */
    public static void checkForClass(Object aObject, Class theClass)
    {
        if (!theClass.isInstance(aObject))
        {
            throw new IllegalArgumentException("The object is of class " + aObject.getClass().getName() + " while " + theClass.getName() + " was expected.");
        }
    }
        
    /**
     * Throws an <code>NullPointerException</code> if <code>aText</code> is null.
     * Throws an <code>IllegalArgumentException</code> if <code>aText</code> does
     * only contain whitespaces.
     *
     * <P>Most text used in an application is meaningful only if it has visible content.
     */
    public static void checkForContent(String aText)
    {
        if (aText == null)
        {
            throw new NullPointerException();
        }
        if(aText.trim().length() == 0)
        {
            throw new IllegalArgumentException("Text has no visible content");
        }
    }
    
    /**
     * Throw an <code>IndexOutOfBoundsException</code> if not aLow <= aNumber <= aHigh
     * returns <code>false</code>.
     *
     * @param aLow is less than or equal to <code>aHigh</code>.
     */
    public static void checkForRange(int aNumber, int aLow, int aHigh)
    {
        if (aNumber < aLow || aNumber > aHigh)
        {
            throw new IndexOutOfBoundsException(aNumber + " not in range " + aLow + ".." + aHigh);
        }
    }
 
    /**
     * Throw an <code>IndexOutOfBoundsException</code> if not aLow <= aNumber <= Integer.MAX_VALUE
     * returns <code>false</code>.
     *
     * @param aLow is less than or equal to <code>aHigh</code>.
     */
    public static void checkForRange(int aNumber, int aLow)
    {
        if (aNumber < aLow)
        {
            throw new IndexOutOfBoundsException(aNumber + " not in range " + aLow + ".." + Integer.MAX_VALUE);
        }
    }    
    
    /**
     * Throw an <code>IllegalArgumentException</code> only if <code>aCollection.isEmpty</code>
     * returns <code>true</code>.
     */
    public static void checkForEmpty(Collection aCollection)
    {
        if ( aCollection.isEmpty() )
        {
            throw new IllegalArgumentException("Collection is empty.");
        }
    }
}

