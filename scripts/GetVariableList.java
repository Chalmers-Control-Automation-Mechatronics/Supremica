/***************** GetVariableList.java ******************/
package Lupremica;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import org.apache.logging.log4j.Logger;

public class GetVariableList
{
	public GetVariableList(org.supremica.gui.ide.IDE ide) // called by RunScript
	throws ClassNotFoundException
	{
		final Logger logger = ide.getTheLog();
		logger.info("Get variable list of current module");

		final ModuleSubject module = ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();

		final java.util.List<Proxy> proxyList = module.getComponentList();
		for(int i = 0; i < proxyList.size(); i++)
		{
			final Proxy proxy = proxyList.get(i);
			// All four of these if-clauses work as expected, but not in Lua...
			// if(proxy.getProxyInterface().getSimpleName().equals("VariableComponentProxy"))
			// if(proxy instanceof VariableComponentProxy)
			// if(Class.forName("net.sourceforge.waters.model.module.VariableComponentProxy").isInstance(proxy))
			if(VariableComponentProxy.class.isInstance(proxy))
			{
				final VariableComponentProxy var = (VariableComponentProxy)proxy;
				// logger.info("var.getIdentifier(): " + var.getIdentifier());
				// logger.info("var.getType().toString(): " + var.getType().toString());
				// logger.info("var.getInitialStatePredicate().toString(): " + var.getInitialStatePredicate().toString());
				logger.info(var.getInitialStatePredicate().toString() + ": " + var.getType().toString());
			}
		}
	}

}