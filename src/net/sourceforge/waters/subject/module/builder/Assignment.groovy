package net.sourceforge.waters.subject.module.builder;

class Assignment extends Named {
	Expression input
	IdentifierExpression Q
	static final pattern = /(?i)assign(?:ment)?/
	static final defaultAttr = 'name'
	static final parentAttr = 'statements'

	def addToModule(ModuleBuilder mb, List statements, int indexToThis) {
		Scope scope = statements[indexToThis].scope
		Scope processScope = scope.processScope // Process scope is null if it is the controller that executes the statement
		def thisOne = statements[indexToThis]
		statements = statements.findAll{it.scope.processScope == processScope}
		indexToThis = statements.indexOf(thisOne)
		Variable assignedVariable = scope.namedElement(Q)
		if (assignedVariable.assignmentAutomatonNeeded(statements, indexToThis)) {
			mb.booleanVariable(Q.toSupremicaSyntax(scope), initial:assignedVariable.value, marked:assignedVariable.value ? true : false)
			mb.plant("ASSIGN_${Q.toSupremicaSyntax(scope)}", defaultEvent:scope.eventName, deterministic:false) {
				state('q0', marked:true) {
					selfLoop(guard:input.toSupremicaSyntax(scope, statements[0..<indexToThis])) { set(Q.toSupremicaSyntax(scope)) }
					selfLoop(guard:new Expression("not (${input})").toSupremicaSyntax(scope, statements[0..<indexToThis])) { reset(Q.toSupremicaSyntax(scope)) }
				}
			}
		}
	}
	List execute(Scope parent) {
		[[statement:this, scope:parent]]
	}
	List getNamedElements() { [] }
	List getSubScopes() { [] } 
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
	//List getNamedElementsOfParent() {[]}
}
