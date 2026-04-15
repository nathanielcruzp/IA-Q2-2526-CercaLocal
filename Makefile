run:
	javac -cp "AIMA.jar:Desastres.jar" src/*.java Main.java
	java -cp ".:AIMA.jar:Desastres.jar" Main
	rm -f *.class src/*.class

clean:
	rm -f *.class src/*.class
