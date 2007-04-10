package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class Assignment {
	Expression input
	IdentifierExpression Q
	Expression condition
	static final pattern = /(?i)assign(?:ment)|.*\:=.*?/
	static final defaultAttr = 'condition'
	static final parentAttr = 'statements'

	List getRuntimeAssignments(Scope parent) {
		if (!condition) return [[scope:parent, Q:Q, input:input] as RuntimeAssignment]
		else return [[scope:parent, Q:Q, input:new Expression("($condition) and ($input) or (not ($condition) and $Q)")] as RuntimeAssignment]
	}
	List getNamedElements() { [] }
	List getSubScopes() { [] } 
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
	//List getNamedElementsOfParent() {[]}
	String toString() {
		(condition ? "IF $condition THEN " : '') + "$Q := $input"
	}
}

class RuntimeAssignment {
	final IdentifierExpression Q
	final Expression input
	final Scope scope
	final boolean stochastic // Means that it will not necessarily execute
	
	String toString() {
		"$scope { $Q := $input }"
	}
	def addToModule(ModuleBuilder mb) {
		Variable assignedVariable = scope.namedElement(Q)
		assert assignedVariable, "Undeclared identifier $Q in ${this}"
		boolean markedValue
		if (assignedVariable.markedValue != null) markedValue = assignedVariable.markedValue
		else if (assignedVariable.value != null)  markedValue = assignedVariable.value
		else markedValue = false
		String supremicaNameOfQ = Q.toSupremicaSyntax() //Q must already be fully qualified
		mb.booleanVariable(supremicaNameOfQ, initialValue:assignedVariable.value, markedValue:markedValue)
		mb.plant("ASSIGN_${supremicaNameOfQ}", defaultEvent:scope.eventName, deterministic:false) {
			state('q0', marked:true) {
				//println input.expand(scope, statements[0..<indexToThis])
				//println input.expand(scope, statements[0..<indexToThis]).cleanup()
				selfLoop(guard:input?.cleanup()?.toSupremicaSyntax()) { set(supremicaNameOfQ) }
				selfLoop(guard:input ? new Expression("not (${input})").cleanup().toSupremicaSyntax() : null) { reset(supremicaNameOfQ) }
			}
		}
	}
	
	static Map separateBasedOnProcess(List assignments) {
		Map result = [:]
		assignments.each { 
			if (!result[it.scope.processScope]) result[it.scope.processScope] = []
			result[it.scope.processScope] << it
		}
		result
	}
	static Map substituteIntoStateless(List assignmentForOneProcess) {
		assignmentForOneProcess.inject([:]){ Map map, elem ->
			def expandedInput = elem.input?.replaceAllIdentifiers {
				def fullName = elem.scope.fullNameOf(it)
				assert fullName, "Undeclared identifier $it in $elem"
				map[fullName] ? new Expression("(${map[fullName]})") : fullName
			}
			map[elem.scope.fullNameOf(elem.Q)] = expandedInput
			map
		}
	}
	static removeUnnecessary(FunctionBlock application, Map statelessAssignments) {
		statelessAssignments.findAll { ass ->
			[*application.inputs, *application.outputs].any{it.name == ass.key} ||
				statelessAssignments.values().any{it.contains(ass.key)}
		}
	}
}