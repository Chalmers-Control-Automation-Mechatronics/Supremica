package org.supremica.util.BDD.li;

/**
 * If we can early see that a test will pass and dont want to build all the data structures and
 * crap to just follow the algorithm story-line, we throw in one of these
 *
 */
public class TrivialPass
	extends Exception
{
    private static final long serialVersionUID = 1L;

    public TrivialPass(String pass_what)
	{
		super(pass_what);
	}
}
