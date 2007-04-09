/*
 *   Copyright (C) 2006 Goran Cengic
 *
 *   This file is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *   To contact author please refer to contact information in the README file.
 */

/*
 * @author Goran Cengic
 */

package org.supremica.external.iec61499fb2efa;

import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;



class ExtendedAutomaton
{

	private ModuleProxyFactory factory;
 	private IdentifierProxy identifier;
	private SimpleComponentSubject component;
	private GraphSubject graph;


	public ExtendedAutomaton(String name, ComponentKind kind) 
	{
		factory = ModuleSubjectFactory.getInstance();

		identifier = factory.createSimpleIdentifierProxy(name);
		graph = new GraphSubject();
		component = new SimpleComponentSubject(identifier, kind, graph);
	}

	protected SimpleComponentSubject getComponent()
	{
		return component;
	}
	



	public void addState(String name)
	{
		graph.getNodesModifiable().add(new SimpleNodeSubject(name));
	}

	public void addTransition()
	{

	}

}
