package org.supremica.external.iec61131.builder

class IdentifierExpression extends Expression implements Comparable {
	IdentifierExpression(String expr) {
		super(expr)
	}
	IdentifierExpression leftMostPart() {
		def parts = text.split(Converter.SEPARATOR_PATTERN)
		if (parts.size() > 1) return new IdentifierExpression(parts[0]) 
		null
	}
	IdentifierExpression exceptLeftMostPart() {
		def parts = text.split(Converter.SEPARATOR_PATTERN)
		if (parts.size() > 1) return new IdentifierExpression(parts[1..-1].join(Converter.SEPARATOR)) 
		else return new IdentifierExpression(text)
	}
	IdentifierExpression rightMostPart() {
		new IdentifierExpression(text.split(Converter.SEPARATOR_PATTERN)[-1])
	}
	String toSupremicaSyntax(Scope scope) {
		super.expand(scope, []).toSupremicaSyntax()
	}
	IdentifierExpression plus(other) {
		new IdentifierExpression("${this}${Converter.SEPARATOR}$other")
	}
	boolean startsWith(IdentifierExpression other) {
		text.toLowerCase().startsWith(other.text.toLowerCase())
	}
	IdentifierExpression relativeTo(IdentifierExpression scope) {
		if (startsWith(scope)) return new IdentifierExpression(text[scope.text.size()+1..-1])
		else if (scope.startsWith(this)) return rightMostPart()
		else return this
	}
	int compareTo(other) {
		text.toLowerCase().compareTo(other.text.toLowerCase())
	}
}