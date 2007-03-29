package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class LogicProgram {
	static final pattern = /(?i)logicProgram/
	static final defaultAttr = 'name'
	static final parentAttr = 'programs'
	IdentifierExpression name
	List statements = []
	List variables = []
	List execute(Scope parent) {
		Scope scope = [self:this, parent:parent]
		statements*.execute(scope).flatten()//.inject([]){list, elem -> list + elem.execute(scope)}
	}
	List getNamedElements() {
		subScopeElements
	}
	List getSubScopeElements() {
		variables
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		subScopeElements*.addProcessEvents(mb, [self:this, parent:parent] as Scope)
	}
}