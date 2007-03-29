package org.supremica.external.iec61131.builder

class Scope {
	final Scope parent
	final Object self
	
	boolean isGlobal() {!parent}
	
	IdentifierExpression fullNameOf(IdentifierExpression id) {
		//println id
		assert id
		scopeOf(id)?.fullNameOf(namedElement(id))
	}
	List getSubScopesOfSelf() {
		if (self.properties['type']) return parent.namedElement(self.type).subScopeElements.collect{new Scope(self:it, parent:this)}
		else return self.subScopeElements.collect{new Scope(self:it, parent:this)}
	}
	List getNamedElementsOfSelf() {
		if (self.properties['type']) return parent.namedElement(self.type).namedElements
		else return self.namedElements
	}
	Object namedElement(IdentifierExpression id) {
		assert id
		scopeOf(id)?.namedElementsOfSelf.find{it.name == id.rightMostPart()}
	}
	private Map scopeOfIdCache = [:]
	Scope scopeOf(IdentifierExpression id) {
		scopeOf(id, true)
	}
	Scope scopeOf(IdentifierExpression id, boolean delegateToParent) {
		if (scopeOfIdCache[id]) return scopeOfIdCache[id]
//		println "id: $id, scope: ${fullName}, delegateToParent: $delegateToParent, leftMostPart: ${id.leftMostPart()}, except: ${id.exceptLeftMostPart()}, namedElements: ${namedElementsOfSelf.name}, subscopes:${subScopesOfSelf.fullName}"
		Scope scopeOfId
		Scope subScope
		if (id.leftMostPart() && (subScope = subScopesOfSelf.find{it.self.name == id.leftMostPart()})) {
			scopeOfId = subScope.scopeOf(id.exceptLeftMostPart(), false)
		} else if (namedElementsOfSelf.any{it.name == id}) {
			scopeOfId = this
		} else if (delegateToParent) scopeOfId = parent?.scopeOf(id)
//		assert scopeOfId, "Undeclared identifier $id, scope ${fullName}, identifiers: ${namedElementsOfSelf.name}"
		scopeOfIdCache[id] = scopeOfId
	}
	
	List identifiersInExpression(Expression expr) {
//		println expr
		expr.findIdentifiers().collect{
			def fullId = fullNameOf(it)
			assert fullId, "Undeclared identifier $it in expr '${expr}', scope ${fullName}"
			fullId
		}
	}
	
	private Map expandCache = [:]
	Expression expand(Expression expr, List earlierStatements) {
//		println "expand, expr:$expr, identifiers: ${expr.findIdentifiers()}"
		def cacheKey = "$expr${earlierStatements.size()}"
		if (expandCache[cacheKey]) return expandCache[cacheKey]
		def expandedExpr = expr.text
		expr.findIdentifiers().each { id ->
			def newExpr = exchange(id, earlierStatements)
			assert newExpr, "Undeclared identifier $id in expr '${expr}', scope ${fullName}"
			expandedExpr = expandedExpr.replaceAll(Expression.FULL_ID_PATTERN){(new IdentifierExpression(it) == id) ? newExpr.text : it}
 		}
		expandCache[cacheKey] = new Expression(expandedExpr)
	}
	Expression exchange(IdentifierExpression id, List earlierStatements) {
//		println "exchange, id:$id"
		IdentifierExpression fullId = fullNameOf(id)
		def newExpr
		if (earlierStatements.empty) newExpr = fullId
		else {
			def earlierAssigmentToId = earlierStatements.reverse().find{it.scope.fullNameOf(it.Q) == fullId}
			if (earlierAssigmentToId) {
				def i = earlierStatements.lastIndexOf(earlierAssigmentToId)
				newExpr = earlierAssigmentToId.scope.expand(earlierAssigmentToId.input, earlierStatements[0..<i])
				//if (!(newExpr.text ==~ /(?i)(?:not\s+)?${FULL_ID_PATTERN}/))
				newExpr = new Expression("($newExpr)")
			} else {
				newExpr = fullId
			}
		}
		newExpr
	}
	IdentifierExpression fullNameOf(namedObject) {
		assert namedObject.name
		assert namedElementsOfSelf.any{it.name == namedObject.name}, "${namedObject.name} does not belong to scope $fullName, identifiers: ${namedElementsOfSelf.name}"
		!global ? new IdentifierExpression("${fullName}${Converter.SEPARATOR}$namedObject.name") : namedObject.name
	}
	IdentifierExpression relativeNameOf(namedObject, Scope relativeTo) {
		assert namedElementsOfSelf.any{it.name == namedObject.name}, "${namedObject.name} does not belong to scope $fullName, identifiers: ${namedElementsOfSelf.name}"
		
	}
	
	boolean descendantOf(Scope scope) {
		if (this.fullName == scope.fullName) return false
		if (scope.global) return true
		if (this.global) return false
		return this.fullName.startsWith(scope.fullName)
	}
	
	IdentifierExpression relativeNameOf(IdentifierExpression id, Scope relativeTo) {
		if (relativeTo.global) return fullNameOf(id) 
		else return fullNameOf(id)?.relativeTo(relativeTo.fullName)
	}

	IdentifierExpression fullNameCache = null
	IdentifierExpression getFullName() {
		if (fullNameCache) return fullNameCache
		if (global) fullNameCache = new IdentifierExpression('(global)')
		else if (parent.global) fullNameCache = self.name
		else {fullNameCache = new IdentifierExpression("${parent.fullName}${Converter.SEPARATOR}${self.name}")}
		fullNameCache
	}
	final def process
	def getProcess() {
		if (!process) return parent.process
		else return process
		/*if (self instanceof Process) return this
		else return parent?.processScope*/
	}
	def getProcessScope() {
		if (!process) return parent.processScope
		else return this
	}
	String getEventName() {
		global ? Converter.SCAN_CYCLE_EVENT_NAME : "${fullName.toSupremicaSyntax()}_change" 
	}
	
	Scope getGlobalScope() {
		if (global) return this
		else return parent.globalScope
	}
	String toString() {
		fullName
	}
}