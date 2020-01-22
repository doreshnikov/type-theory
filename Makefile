.PHONY: all compile run pack clean out

all: out compile

compile:
	export JAVA_OPTS="-Xms8g -Xss512m"
	kotlinc src/parser/*.kt src/types/*.kt src/Main.kt -d out/out.jar -include-runtime

run:
	java -XX:+UseSerialGC -jar out/out.jar

pack:
	zip out.zip -r Makefile src/Main.kt src/parser/ src/types/

clean:
	rm -rf out out.zip

out:
	mkdir -p out
