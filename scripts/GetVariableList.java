/***************** GetVariableList.java ******************/
package Lupremica;

import java.util.List;
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
		logger.info("Get variable list of current module 0");

		final ModuleSubject module = ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();
		List<VariableComponentProxy> variableList = GetVariableList.getVariableComponentProxys(module);
		for(final VariableComponentProxy var : variableList)
		{
			logger.info(var.getInitialStatePredicate() + " (" + var.getType() + ")");
		}
	}
	public static List<VariableComponentProxy> getVariableComponentProxys(final ModuleSubject module)
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
		}
		return variableList;
	}
}