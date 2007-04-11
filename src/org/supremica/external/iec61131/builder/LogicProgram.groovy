package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class LogicProgram {
	static final pattern = /(?i)logicProgram/
	static final defaultAttr = 'name'
	static final parentAttr = 'programs'
	Identifier name
	List statements = []
	List variables = []
	List getRuntimeAssignments(Scope parent) {
		Scope scope = [self:this, parent:parent]
		statements*.getRuntimeAssignments(scope).flatten()
	}
	List getControllableVariables(Scope parent) {
		Scope scope = [self:this, parent:parent]
		subScopeElements*.getControllableVariables(scope).flatten()
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