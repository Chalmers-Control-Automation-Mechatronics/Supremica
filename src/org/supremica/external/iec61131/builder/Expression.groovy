package org.supremica.external.iec61131.builder

class Expression {
	Expression(String text) {
		this.text = text?.trim()
	}
	final String text
	
	Expression expand(scope, programState) {
		scope.expand(this, programState)
	}
	
	String toSupremicaSyntax() {
		String newExpr = text
		def replaceWordOp = {oldOp, newOp ->
			newExpr.replaceAll(/(?i)\s*\b${oldOp}\b\s*/){"$newOp"}
		}
		newExpr = replaceWordOp(/true/, ' 1 ')
		newExpr = replaceWordOp(/false/, ' 0 ')
		newExpr = replaceWordOp(/and/, ' & ')
		newExpr = replaceWordOp(/or/, ' | ')
		newExpr = newExpr.replace(':=', '£££')
		newExpr = newExpr.replace('=', '==')
		newExpr = newExpr.replace('£££', '=')
		newExpr = replaceWordOp(/not/, ' !')
		newExpr = newExpr.replace('.', '_')
		newExpr = newExpr.replaceAll(/\(\s+/){'('}
		newExpr = newExpr.replaceAll(/\s+\)/){')'}
		newExpr.trim()
	}
	String toString() {
		return text
	}
	Expression cleanup() {
		String oldExpr = ''
		String newExpr = text
		while (oldExpr != newExpr) {
			oldExpr = newExpr
			newExpr = newExpr.replaceAll(/\(\s*${FULL_ID_PATTERN}\s*\)/){it[1..-2]}
			newExpr = newExpr.replaceAll(/(?i)\(\s*not\s+${FULL_ID_PATTERN}\s*\)/){it[1..-2]}
			newExpr = newExpr.replaceAll(/(?i)not\s+not/){''}
			newExpr = newExpr.replaceAll(/\s\s/){' '}
			newExpr = newExpr.replaceAll(/\(\((?:\s|${FULL_ID_PATTERN})*\)\)/){it[1..-2]}
		}
		new Expression(newExpr)
	}
	boolean equals(object) {
		if (!object) return false
		if (object instanceof Expression) {
			if (text == object.text || (text && object.text && text.toLowerCase() == object.text.toLowerCase())) return true
			return 
		}
		false
	}
	int hashCode() {
		return text.toLowerCase().hashCode()
	}
	private Map identifiersCache
	Set findIdentifiers() {
		if (identifiersCache == null) {
			Map ids = [:]
			(text =~ FULL_ID_PATTERN).each{match -> if (!KEYWORDS.any{keyword -> match ==~ keyword}) ids[new IdentifierExpression(match)] = true}
			identifiersCache = ids
		}
		identifiersCache.keySet()
	}
	protected static final KEYWORDS = [/(?i)and/, /(?i)or/, /(?i)not/, /(?i)true/, /(?i)false/]
	protected static final KEYWORD_PATTERN = /(?i)and|or|not|true|false/
	                                                                  	
	static final SIMPLE_ID_PATTERN = /\b_*[a-zA-Z]\w*\b*/
	static final FULL_ID_PATTERN = /${SIMPLE_ID_PATTERN}(?:${Converter.SEPARATOR_PATTERN}${SIMPLE_ID_PATTERN})*/
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
		assert new Expression('apa_bepa.cepa and (lepa_pep_as or epa.opa)').findIdentifiers().text.sort() == ['apa_bepa.cepa', 'lepa_pep_as', 'epa.opa'].sort()
		assert new Expression('Py2_in and not Py2_old').findIdentifiers().text.sort() == ['Py2_in', 'Py2_old'].sort()
		assert new Expression('apa and ((pepa)) and (not (cepa))').cleanup().text == 'apa and pepa and not cepa'
		assert new Expression('apa or ((sopa and ((pepa.rl or lepa))))').cleanup().text == 'apa or ((sopa and (pepa.rl or lepa)))'
	}
	Expression replaceAllIdentifiers(closure) {
		new Expression(text.replaceAll(FULL_ID_PATTERN) { it ==~ KEYWORD_PATTERN ? it : closure(new IdentifierExpression(it)).text })
	}
	boolean contains(IdentifierExpression id) {
		if (!identifiersCache) findIdentifiers()
		identifiersCache[id] 
	}
}
