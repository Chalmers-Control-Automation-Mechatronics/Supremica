all: lexer parser

lexer:
	jflex ST.flex

parser:	
	java java_cup.Main -parser Parser -symbols Symbols ST.cup

compile: compile_parser compile_lexer

compile_parser:
	javac -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/tools/java_cup-10k -d /home/cengic/devel/workspace/Supremica/build /home/cengic/devel/workspace/Supremica/src/org/supremica/functionblocks/model/interpreters/st/Symbols.java
	javac -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/tools/java_cup-10k -d /home/cengic/devel/workspace/Supremica/build /home/cengic/devel/workspace/Supremica/src/org/supremica/functionblocks/model/interpreters/st/Parser.java

compile_lexer:
	javac -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/tools/java_cup-10k -d /home/cengic/devel/workspace/Supremica/build /home/cengic/devel/workspace/Supremica/src/org/supremica/functionblocks/model/interpreters/st/Lexer.java
