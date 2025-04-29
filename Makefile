JAVAC = javac
JAVA = java
SRC = Server.java Client.java UserInfo.java
CLASSES=$(SRC:.java=.class)

all: compile

compile:
	$(JAVAC) $(SRC)
Server: compile
	$(JAVA) Server
Client: compile
	$(JAVA) Client $(HOST)
clean:
	-rm -f *.class