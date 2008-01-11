package org.supremica.external.iec61131.builder

class Scope {
	Scope parent
	Object self
	
	boolean isGlobal() {!parent}
	
	Identifier fullNameOf(Identifier id) {
		//println id
		assert id
		scopeOf(id)?.fullNameOf(namedElement(id))
	}
	List getSubScopesOfSelf() {
		if (self.metaClass.properties.any{it.name == 'type'}) return parent.namedElement(self.type).subScopeElements.collect{new Scope(self:it, parent:this)}
		else return self.subScopeElements.collect{new Scope(self:it, parent:this)}
	}
	List getNamedElementsOfSelf() {
		if (self.metaClass.properties.any{it.name == 'type'}) 
			return parent.namedElement(self.type).namedElements
		else return self.namedElements
	}
	Object namedElement(Identifier id) {
		assert id
		scopeOf(id)?.namedElementsOfSelf.find{it.name == id.rightMostPart()}
	}
	private Map scopeOfIdCache = [:]
	Scope scopeOf(Identifier id) {
		scopeOf(id, true)
	}
	Scope scopeOf(Identifier id, boolean delegateToParent) {
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
		
	Identifier fullNameOf(namedObject) {
		assert namedObject.name
		assert namedElementsOfSelf.any{it.name == namedObject.name}, "${namedObject.name} does not belong to scope $fullName, identifiers: ${namedElementsOfSelf.name}"
		!global ? new Identifier("${fullName}${Converter.SEPARATOR}$namedObject.name") : namedObject.name
	}

	Identifier relativeNameOf(Identifier id, Scope relativeTo) {
		if (relativeTo.global) return fullNameOf(id) 
		else return fullNameOf(id)?.relativeTo(relativeTo.fullName)
	}

	Identifier fullNameCache = null
	Identifier getFullName() {
		if (fullNameCache) return fullNameCache
		if (global) fullNameCache = new Identifier('ControlUnit')
		else if (parent.global) fullNameCache = self.name
		else {fullNameCache = new Identifier("${parent.fullName}${Converter.SEPARATOR}${self.name}")}
		fullNameCache
	}
    def process
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