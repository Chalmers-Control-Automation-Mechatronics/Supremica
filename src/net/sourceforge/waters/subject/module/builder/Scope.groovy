package net.sourceforge.waters.subject.module.builder;

class Scope {
	Scope parent
	Named self
	
	boolean isGlobal() {!parent}
	
	IdentifierExpression fullNameOf(IdentifierExpression id) {
		assert id
		scopeOf(id).fullNameOf(namedElement(id))
	}
	Named namedElement(IdentifierExpression id) {
		assert id
		scopeOf(id).self.namedElements.find{it.name == id.rightMostPart()}
	}
	Scope scopeOf(IdentifierExpression id) {
		Scope scopeOfId
		Named subScopeElement
		if (id.leftMostPart() && (subScopeElement = self.subScopeElements.find{it.name == id.leftMostPart()})) {
			scopeOfId = new Scope(self:subScopeElement, parent:this).scopeOf(id.exceptLeftMostPart())
		} else if (self.namedElements.any{it.name == id}) {
			scopeOfId = this
		} else scopeOfId = parent?.scopeOf(id)
		assert scopeOfId, "Undeclared identifier $id, scope ${fullName}"
		scopeOfId
	}
	protected static final KEYWORDS = [/(?i)and/, /(?i)or/, /(?i)not/, /(?i)true/, /(?i)false/]
	
	protected static final SIMPLE_ID_PATTERN = /\b_*[a-zA-Z]\w*\b*/
	protected static final FULL_ID_PATTERN = /${SIMPLE_ID_PATTERN}(?:${Converter.SEPARATOR_PATTERN}${SIMPLE_ID_PATTERN})*/
	static {
		assert 'apa' ==~ FULL_ID_PATTERN
		assert '__apa' ==~ FULL_ID_PATTERN
		assert '_apa1' ==~ FULL_ID_PATTERN
		assert !('_1apa' ==~ FULL_ID_PATTERN)
		assert !('1apa' ==~ FULL_ID_PATTERN)
		assert 'a__p1a' ==~ FULL_ID_PATTERN
		assert 'a__p1a' ==~ FULL_ID_PATTERN
		assert 'a__p1a.asd' ==~ FULL_ID_PATTERN
		assert 'a__p1a.__asd.sd1' ==~ FULL_ID_PATTERN
		assert !('a__p1a.__1.sd1' ==~ FULL_ID_PATTERN)
	}
	
	List identifiersInExpression(Expression expr) {
		List identifiers = []
		def expandedExpr = (expr.text =~ FULL_ID_PATTERN).each { word ->
			if (!KEYWORDS.any{keyword -> word ==~ keyword}) {
				def fullId = fullNameOf(new IdentifierExpression(word))
				assert fullId, "Undeclared identifier $word in expr '${expr.text}', scope ${fullName}"
				identifiers << fullId
			}
		}
		identifiers
	}
	
	Expression expand(Expression expr, List earlierStatements) {
		def expandedExpr = expr.text.replaceAll(FULL_ID_PATTERN) { word ->
			def expandedWord = word
   			if (!KEYWORDS.any{keyword -> word ==~ keyword}) {
   				expandedWord = exchange(new IdentifierExpression(word), earlierStatements).text
			}
			assert expandedWord, "Undeclared identifier $word in expr '${expr.text}', scope ${fullName}"
			expandedWord
		}
   		new Expression(expandedExpr)
	}
	Expression exchange(IdentifierExpression id, List earlierStatements) {
		IdentifierExpression fullId = fullNameOf(id)
		if (earlierStatements.empty) return fullId
		def earlierAssigmentToId = earlierStatements.reverse().find{it.scope.fullNameOf(it.statement.Q) == fullId}
		if (earlierAssigmentToId) {
			def i = earlierStatements.lastIndexOf(earlierAssigmentToId)
			def newExpr = earlierAssigmentToId.scope.expand(earlierAssigmentToId.statement.input, earlierStatements[0..<i])
			if (newExpr.text ==~ /(?i)(?:not\s+)?${FULL_ID_PATTERN}/) return newExpr
			else return new Expression("($newExpr)")
		} else {
			return fullId
		}
	}
	IdentifierExpression fullNameOf(Named namedObject) {
		!global ? new IdentifierExpression("${fullName}${Converter.SEPARATOR}$namedObject.name") : namedObject.name
	}
	
	IdentifierExpression getFullName() {
		if (global) return null
		if (parent.global) return self.name
		new IdentifierExpression("${parent.fullName}${Converter.SEPARATOR}${self.name}")
	}
}