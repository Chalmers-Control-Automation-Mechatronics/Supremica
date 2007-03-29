package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class Assignment {
	Expression input
	IdentifierExpression Q
	Expression condition
	static final pattern = /(?i)assign(?:ment)|.*\:=.*?/
	static final defaultAttr = 'condition'
	static final parentAttr = 'statements'

	List execute(Scope parent) {
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
	def addToModule(ModuleBuilder mb, List statements, int indexToThis) {
		Scope processScope = scope.processScope // Process scope is null if it is the controller that executes the statement
		statements = statements.findAll{it.scope.processScope == processScope}
		indexToThis = statements.indexOf(this)
		Variable assignedVariable = scope.namedElement(Q)
		assert assignedVariable, "Undeclared identifier $Q in scope ${scope.fullName}"
		if (assignedVariable.assignmentAutomatonNeeded(statements, indexToThis)) {
			boolean markedValue = (assignedVariable.markedValue != null) ? assignedVariable.markedValue : (assignedVariable.value != null ? assignedVariable.value : false) 
			mb.booleanVariable(Q.toSupremicaSyntax(scope), initial:assignedVariable.value, marked:markedValue)
			mb.plant("ASSIGN_${Q.toSupremicaSyntax(scope)}", defaultEvent:scope.eventName, deterministic:false) {
				state('q0', marked:true) {
					//println input.expand(scope, statements[0..<indexToThis])
					//println input.expand(scope, statements[0..<indexToThis]).cleanup()
					selfLoop(guard:input.expand(scope, statements[0..<indexToThis]).cleanup().toSupremicaSyntax()) { set(Q.toSupremicaSyntax(scope)) }
					selfLoop(guard:new Expression("not (${input})").expand(scope, statements[0..<indexToThis]).cleanup().toSupremicaSyntax()) { reset(Q.toSupremicaSyntax(scope)) }
				}
			}
		}
	}
	String toString() {
		"$scope { $Q := $input }"
	}
	def addToModule(ModuleBuilder mb) {
		Variable assignedVariable = scope.namedElement(Q)
		assert assignedVariable, "Undeclared identifier $Q in ${this}"
		boolean markedValue = (assignedVariable.markedValue != null) ? assignedVariable.markedValue : (assignedVariable.value != null ? assignedVariable.value : false) 
		mb.booleanVariable(Q.toSupremicaSyntax(scope), initial:assignedVariable.value, marked:markedValue)
		mb.plant("ASSIGN_${Q.toSupremicaSyntax(scope)}", defaultEvent:scope.eventName, deterministic:false) {
			state('q0', marked:true) {
				//println input.expand(scope, statements[0..<indexToThis])
				//println input.expand(scope, statements[0..<indexToThis]).cleanup()
				selfLoop(guard:input.cleanup().toSupremicaSyntax()) { set(Q.toSupremicaSyntax()) }
				selfLoop(guard:new Expression("not (${input})").cleanup().toSupremicaSyntax()) { reset(Q.toSupremicaSyntax()) }
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
			def expandedInput = elem.input.replaceAllIdentifiers {
				def fullName = elem.scope.fullNameOf(it)
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