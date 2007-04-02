package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class Input extends ExternalVariable {
	static final pattern = /(?i)input/
	static final defaultAttr = 'name'
	static final parentAttr = 'inputs'
}
class Output extends ExternalVariable {
	static final pattern = /(?i)output/
	static final defaultAttr = 'name'
	static final parentAttr = 'outputs'
}
abstract class ExternalVariable extends Variable {
	def assignmentAutomatonNeeded(List statements, int indexToThis) {
		assert false
		def statementScope = statements[indexToThis].scope
		def variableScope = statementScope.scopeOf(statements[indexToThis].Q)
		def fullNameOfThis = statementScope.fullNameOf(statements[indexToThis].Q)
		def needed = variableScope.global 
		int i = 0
		needed |= statements[0..indexToThis].any{s ->
			statementScope.globalScope.identifiersInExpression(s.scope.expand(s.input, statements[0..<i++])).any{it == fullNameOfThis}
		}
		needed &= indexToThis == statements.size() - 1 || statements[indexToThis+1..-1].every{it.scope.fullNameOf(it.Q) != fullNameOfThis}
		needed
	}
}
class InternalVariable extends Variable {
	static final pattern = /(?i)variable/
	static final defaultAttr = 'name'
	static final parentAttr = 'variables'
	def assignmentAutomatonNeeded(List statements, int indexToThis) {
		assert false
		def statementScope = statements[indexToThis].scope
		def variableScope = statementScope.scopeOf(statements[indexToThis].Q)
		def fullNameOfThis = statementScope.fullNameOf(statements[indexToThis].Q)
		def needed = false 
		int i = 0
		// Don't add if it is not used in any earlier assignment expression, inluding this
		needed |= statements[0..indexToThis].any{s ->
			statementScope.globalScope.identifiersInExpression(s.scope.expand(s.input, statements[0..<i++])).any{it == fullNameOfThis}
		}
		// Don't add if it wil be added later
		needed &= indexToThis == statements.size() - 1 || statements[indexToThis+1..-1].every{it.scope.fullNameOf(it.Q) != fullNameOfThis}
	}
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
}

abstract class Variable {
	boolean value
	boolean markedValue
	IdentifierExpression name
	List getControllableVariables(Scope parent) {
		[name.fullyQualified(parent)]
	}
}
