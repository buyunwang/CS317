all: CSdict.jar
CSdict.jar: CSdict.java
	javac CSdict.java
	jar cvfe CSdict.jar CSdict *.class


run: CSdict.jar
	java -jar CSdict.jar

clean:
	rm -f *.class
	rm -f CSdict.jar
