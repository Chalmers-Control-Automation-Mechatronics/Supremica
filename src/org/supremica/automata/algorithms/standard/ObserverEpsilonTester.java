
/*
 * Created on 2003-jul-24
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.supremica.automata.algorithms.standard;

import org.supremica.automata.*;

/**
 * @author knut
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ObserverEpsilonTester
    implements EpsilonTester
{
    public boolean isThisEpsilon(LabeledEvent event)
    {
        return !event.isObservable();
    }
    
    // debug only
    public String showWhatYouGot()
    {
        return "removing epsilons and unobservable events";
    }
}
