package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class Process {
	static final pattern = /(?i)process/
	static final defaultAttr = 'name'
	static final parentAttr = 'processes'
	IdentifierExpression name
	List variables = []
	List programs = []
	List types = []
	
	List execute(Scope parent) {
		Scope scope = [self:this, parent:parent]
		List statements = []
		programs*.execute(scope).each { statements += it }
		statements
	}
	List getNamedElements() {
		[*variables, *programs, *types]
	}
	List getSubScopeElements() {
		programs
	}
	def addEvent(ModuleBuilder mb, Scope parent) {
		Scope scope = [self:this, parent:parent]
		def eventName = scope.eventName
		mb.event(eventName, controllable:false)
		mb.eventAlias(Converter.PROCESS_EVENTS_ALIAS_NAME, events:[eventName])
	}
	
	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		addEvent(mb, parent)
		subScopeElements*.addProcessEvents(mb, [self:this, parent:parent] as Scope)
	}	
}