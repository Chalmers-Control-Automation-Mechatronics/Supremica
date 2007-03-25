package net.sourceforge.waters.subject.module.builder;

class Assignment extends Named {
	Expression input
	IdentifierExpression Q
        Expression condition
	static final pattern = /(?i)assign(?:ment)|.*\:=.*?/
	static final defaultAttr = 'condition'
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
					//println input.expand(scope, statements[0..<indexToThis]).toSupremicaSyntax()
					//println input.expand(scope, statements[0..<indexToThis]).cleanup().toSupremicaSyntax()
					selfLoop(guard:input.expand(scope, statements[0..<indexToThis]).cleanup().toSupremicaSyntax()) { set(Q.toSupremicaSyntax(scope)) }
					selfLoop(guard:new Expression("not (${input})").expand(scope, statements[0..<indexToThis]).cleanup().toSupremicaSyntax()) { reset(Q.toSupremicaSyntax(scope)) }
				}
			}
		}
	}
	List execute(Scope parent) {
            if (!condition) return [[statement:this, scope:parent]]
            else return [[statement:new Assignment(Q:Q, input:new Expression("($condition) and ($input) or (not ($condition) and $Q)")), scope:parent]]
	}
	List getNamedElements() { [] }
	List getSubScopes() { [] } 
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
	//List getNamedElementsOfParent() {[]}
}
