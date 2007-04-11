package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class FunctionBlockInstance {
	static final pattern = /(?i)functionblockinstance/
	static final defaultAttr = 'name'
	static final parentAttr = 'variables'
	Identifier name
	Identifier type
	FunctionBlock getType(Scope parent) {
		parent.namedElement(type)
	}

	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		FunctionBlock type = getType(parent)
		type.addProcessEvents(mb, [self:this, parent:parent] as Scope)
	}
	List getControllableVariables(Scope parent) {
		FunctionBlock type = getType(parent)
		Scope scope = [self:this, parent:parent]
		[*type.inputs*.getControllableVariables(scope), *type*.getControllableVariables(scope)]
	}
}
