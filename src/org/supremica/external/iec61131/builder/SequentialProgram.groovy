package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class SequentialProgram {
	static final pattern = /(?i)sequentialProgram/
	static final defaultAttr = 'name'
	static final parentAttr = 'programs'
	static final NOT_INIT_VARIABLE = new InternalVariable(name:new IdentifierExpression(Converter.NOT_INIT_VARIABLE_NAME), markedValue:true)
	IdentifierExpression name
	List variables = [NOT_INIT_VARIABLE]
	List sequences = []
	boolean deferred = false
	List getNamedElements() {
		subScopeElements//[*variables, *sequences]
	}
	List getSubScopeElements() {
		[*variables, *sequences, *sequences.steps]
	}
	List execute(Scope parent) {
		Scope scope = [self:this, parent:parent]
		List execStatements = []               
		if (deferred) {
			execStatements += sequences.inject([]){list, elem -> list + elem.execute(scope)}
			execStatements += sequences.steps.inject([]){list, elem -> list + elem.execute(scope)}
		} else {
			execStatements += sequences.inject([]){list, seq ->
				list + seq.execute(scope) + seq.steps.inject([]){list2, step ->
					list2 + step.execute(scope)
				}
			}
		}

		execStatements << [scope:scope, statement:new Assignment(Q:NOT_INIT_VARIABLE.name, input:new Expression('true'))]
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {
		subScopeElements*.addProcessEvents(mb, [self:this, parent:parent] as Scope)
	}

}