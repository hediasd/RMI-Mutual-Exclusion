all:
	javac HelloClient.java HelloServer.java Hello.java HelloInterface.java


run: all
	java HelloServer &
	sleep 1
	java HelloClient

clean:
	rm -f *.class *~
