package net.sourceforge.waters.subject.module.builder;

class Scope {
	Scope parent
	Named self
	
	boolean isGlobal() {!parent}
	
	IdentifierExpression fullNameOf(IdentifierExpression id) {
		//println id
		assert id
		scopeOf(id)?.fullNameOf(namedElement(id))
	}
	List getSubScopesOfSelf() {
		self.subScopeElements.collect{new Scope(self:it, parent:this)}
	}
	List getNamedElementsOfSelf() {
		if (self.properties['type']) return self.namedElements + parent.namedElement(self.type).namedElements
		else return self.namedElements
	}
	Named namedElement(IdentifierExpression id) {
		assert id
		scopeOf(id)?.namedElementsOfSelf.find{it.name == id.rightMostPart()}
	}
	private Map scopeOfIdCache = [:]
	Scope scopeOf(IdentifierExpression id) {
		scopeOf(id, true)
	}
	Scope scopeOf(IdentifierExpression id, boolean delegateToParent) {
		if (scopeOfIdCache[id]) return scopeOfIdCache[id]
//		println "id: $id, scope: ${fullName}, delegateToParent: $delegateToParent, leftMostPart: ${id.leftMostPart()}, except: ${id.exceptLeftMostPart()}"
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
		//println expr
		expr.findIdentifiers().collect{
			def fullId = fullNameOf(it)
			assert fullId, "Undeclared identifier $it in expr '${expr}', scope ${fullName}"
			fullId
		}
	}
	
	private Map expandCache = [:]
	Expression expand(Expression expr, List earlierStatements) {
		//println "expand, expr:$expr, identifiers: ${expr.findIdentifiers()}"
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
			def earlierAssigmentToId = earlierStatements.reverse().find{it.scope.fullNameOf(it.statement.Q) == fullId}
			if (earlierAssigmentToId) {
				def i = earlierStatements.lastIndexOf(earlierAssigmentToId)
				newExpr = earlierAssigmentToId.scope.expand(earlierAssigmentToId.statement.input, earlierStatements[0..<i])
				//if (!(newExpr.text ==~ /(?i)(?:not\s+)?${FULL_ID_PATTERN}/))
				newExpr = new Expression("($newExpr)")
			} else {
				newExpr = fullId
			}
		}
		newExpr
	}
	IdentifierExpression fullNameOf(Named namedObject) {
		assert namedElementsOfSelf.any{it.name == namedObject.name}, "${namedObject.name} does not belong to scope $fullName, identifiers: ${namedElementsOfSelf.name}"
		!global ? new IdentifierExpression("${fullName}${Converter.SEPARATOR}$namedObject.name") : namedObject.name
	}
	IdentifierExpression relativeNameOf(Named namedObject, Scope relativeTo) {
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
/*		Scope idScope = scopeOf(id)
		assert(idScope), "${id} does not belong to scope $fullName, identifiers: ${namedElementsOfSelf.name}"
		if (idScope != this) return idScope.relativeNameOf(id.rightMostPart, relativeTo)
		if (relativeTo == this || relativeTo.descendantOf(this)) return id
		else if (this.descendantOf(relativeTo)) parent.relativeNameOf(self.name + id, relativeTo)
		else if (relativeTo.descendantOf(this)) return scopeOf(id)?.relativeNameOf(id.rightMostPart(), relativeTo)
		else return parent.relativeNameOf(self.name + id, relativeTo)*/
	}

	IdentifierExpression fullNameCache = null
	IdentifierExpression getFullName() {
		if (fullNameCache) return fullNameCache
		if (global) fullNameCache = null
		else if (parent.global) fullNameCache = self.name
		else fullNameCache = new IdentifierExpression("${parent.fullName}${Converter.SEPARATOR}${self.name}")
		fullNameCache
	}
	Scope getProcessScope() {
		if (self instanceof Process) return this
		else return parent?.processScope
	}
	String getEventName() {
		processScope ? "${processScope.fullName.toSupremicaSyntax(processScope)}_change" : Converter.SCAN_CYCLE_EVENT_NAME
	}
	
	Scope getGlobalScope() {
		if (global) return this
		else return parent.globalScope
	}
}