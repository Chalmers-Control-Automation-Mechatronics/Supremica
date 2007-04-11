package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class SequentialProgram {
	static final pattern = /(?i)sequentialProgram/
	static final defaultAttr = 'name'
	static final parentAttr = 'programs'
	static final NOT_INIT_VARIABLE = new InternalVariable(name:new Identifier(Converter.NOT_INIT_VARIABLE_NAME), markedValue:true)
	Identifier name
	List variables = [NOT_INIT_VARIABLE]
	List sequences = []
	boolean deferred = false
	List getNamedElements() {
		subScopeElements//[*variables, *sequences]
	}
	List getSubScopeElements() {
		[*variables, *sequences, *sequences.steps]
	}
	List getRuntimeAssignments(Scope parent) {
		Scope scope = [self:this, parent:parent]
		List rtAssignments = []               
		if (deferred) {
			rtAssignments += sequences*.getRuntimeAssignments(scope).flatten()
			rtAssignments += sequences.steps*.getRuntimeAssignments(scope).flatten()
		} else {
			rtAssignments += sequences.collect{it.getRuntimeAssignments(scope) + it.steps*.getRuntimeAssignments(scope)}.flatten()
		}
		rtAssignments << new RuntimeAssignment(scope:scope, Q:NOT_INIT_VARIABLE.name, input:new Expression('true'))
	}
	List getControllableVariables(Scope parent) {
		Scope scope = [self:this, parent:parent]
		subScopeElements*.getControllableVariables(scope).flatten()
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		subScopeElements*.addProcessEvents(mb, [self:this, parent:parent] as Scope)
	}

}