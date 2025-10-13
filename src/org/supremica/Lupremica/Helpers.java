/***************** Lupremica.java **********************/
/* Helper functiosn for Lua scripts. Since Lua scripts
 * cannot seem to use proxys due to limitations from
 * reflection (unclear exactly what or why), this class
 * collects a bunch of useful static helper functions.
 */
package org.supremica.Lupremica;

import java.util.List;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

public class Helpers
{
    private Helpers() {} // private constructor, do not instantiate

    public static List<VariableComponentProxy> getVariableList(final ModuleSubject module)
    {
        final List<VariableComponentProxy> variableList = new java.util.ArrayList<>();
        for(final Proxy proxy : module.getComponentList())
        {
            // All four of these if-clauses work as expected, but none work in Lua...
            // if(proxy.getProxyInterface().getSimpleName().equals("VariableComponentProxy"))
            // if(proxy instanceof VariableComponentProxy)
            // if(Class.forName("net.sourceforge.waters.model.module.VariableComponentProxy").isInstance(proxy))
            if(VariableComponentProxy.class.isInstance(proxy))
            {
                final VariableComponentProxy var = (VariableComponentProxy)proxy;
                variableList.add(var);
            }
            /* else // to get what the other components are called
            	System.out.println(proxy.getProxyInterface().getName()); */
        }
        return variableList;
    }

    public static List<SimpleComponentProxy> getAutomatonList(final ModuleSubject module)
    {
		final List<SimpleComponentProxy> automatonList = new java.util.ArrayList<>();
		for(final Proxy proxy : module.getComponentList())
		{
			if(proxy instanceof SimpleComponentProxy)
			{
				final SimpleComponentProxy automaton = (SimpleComponentProxy)proxy;
				automatonList.add(automaton);
			}
		}
		return automatonList;
	}

}