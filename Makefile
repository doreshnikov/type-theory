.PHONY: all compile run pack clean out

all: out compile

compile:
	export JAVA_OPTS="-Xms8g -Xss512m"
	kotlinc src/parser/*.kt src/reduction/*.kt src/RMain.kt -d out/out.jar -include-runtime

run:
	java -XX:+UseSerialGC -jar out/out.jar

pack:
	zip out.zip -r Makefile src/RMain.kt src/TMain.kt src/parser/ src/reduction/ src/types/

clean:
	rm -rf out out.zip

out:
	mkdir -p out
