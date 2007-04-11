package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class Process {
	static final pattern = /(?i)process/
	static final defaultAttr = 'name'
	static final parentAttr = 'processes'
	Identifier name
	List variables = []
	List programs = []
	List types = []
	Speed speed = Speed.MEDIUM
	
	
	List getRuntimeAssignments(Scope parent) {
		Scope scope = [self:this, parent:parent, process:this]
		programs*.getRuntimeAssignments(scope).flatten()
	}
	List getNamedElements() {
		[*variables, *programs, *types]
	}
	List getSubScopeElements() {
		programs
	}
	def addEvent(ModuleBuilder mb, Scope parent) {
		Scope scope = [self:this, parent:parent]
		println scope.fullName
		def eventName = scope.eventName
		mb.event(eventName, controllable:false)
		mb.eventAlias(Converter.PROCESS_EVENTS_ALIAS_NAME, events:[eventName])
	}
	
	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		addEvent(mb, parent)
		subScopeElements*.addProcessEvents(mb, [self:this, parent:parent] as Scope)
	}
	def addPriorityAutomaton(ModuleBuilder mb, Scope scope, Scope scopeOfSlowerProcess, forSynthesis) {
		mb.plant("${scope.fullName.toSupremicaSyntax()}_vs_${scopeOfSlowerProcess.fullName.toSupremicaSyntax()}", defaultEvent:scope.eventName) {
			state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:true) {
				outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME)
			}
			state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
				outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, event:scopeOfSlowerProcess.eventName)
				selfLoop()
			}
		}
	}
	def addPriorityAutomaton(ModuleBuilder mb, Scope scope, List scopesOfMuchSlowerProcesses, forSynthesis) {
		if(scopesOfMuchSlowerProcesses) {
			mb.plant("${scope.fullName.toSupremicaSyntax()}_vs_SlowProcesses", defaultEvent:scope.eventName) {
				state(Converter.AFTER_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.BEFORE_SLOWER_PROCESS_STATE_NAME)
				}
				state(Converter.BEFORE_SLOWER_PROCESS_STATE_NAME, marked:true) {
					outgoing(to:Converter.AFTER_SLOWER_PROCESS_STATE_NAME, events:scopesOfMuchSlowerProcesses.eventName)
					selfLoop()
				}
			}
		}
	}
}