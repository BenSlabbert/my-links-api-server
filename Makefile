#!make

.PHONY: install run package test verify compile fmt clean

install: fmt
	@ mvn install
	@ docker build . -t my-links/api-server

package: fmt
	@ mvn package
	@ docker build . -t my-links/api-server

run: install
	@ docker container run --rm -it -p 8080:8080 my-links/api-server:latest

test: fmt
	@ mvn test

verify: fmt
	@ mvn verify

compile: fmt
	@ mvn compile

fmt:
	@ mvn spotless:apply

clean:
	@ mvn clean
