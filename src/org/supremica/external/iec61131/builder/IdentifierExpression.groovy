package org.supremica.external.iec61131.builder

class Identifier extends Expression implements Comparable {
	Identifier(String expr) {
		super(expr)
	}
	Identifier leftMostPart() {
		def parts = text.split(Converter.SEPARATOR_PATTERN)
		if (parts.size() > 1) return new Identifier(parts[0]) 
		null
	}
	Identifier exceptLeftMostPart() {
		def parts = text.split(Converter.SEPARATOR_PATTERN)
		if (parts.size() > 1) return new Identifier(parts[1..-1].join(Converter.SEPARATOR)) 
		else return new Identifier(text)
	}
	Identifier rightMostPart() {
		new Identifier(text.split(Converter.SEPARATOR_PATTERN)[-1])
	}
	Identifier plus(other) {
		new Identifier("${this}${Converter.SEPARATOR}$other")
	}
	boolean startsWith(Identifier other) {
		text.toLowerCase().startsWith(other.text.toLowerCase())
	}
	Identifier relativeTo(Identifier scope) {
		if (startsWith(scope)) return new Identifier(text[scope.text.size()+1..-1])
		else if (scope.startsWith(this)) return rightMostPart()
		else return this
	}
	int compareTo(other) {
		text.toLowerCase().compareTo(other.text.toLowerCase())
	}
	Identifier fullyQualified(Scope scope) {
		scope.fullNameOf(this)
	}
}