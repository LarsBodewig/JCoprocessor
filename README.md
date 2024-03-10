[![Available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.jcoprocessor/jcoprocessor?label=Available%20on%20Maven%20Central)](https://central.sonatype.com/artifact/dev.bodewig.jcoprocessor/jcoprocessor)

# JCoprocessor

A JCoprocess is a separate Java Process that can be used to delegate specific tasks to. JCoprocesses are started using the same Java Runtime and classpath the main application is running in, but not the same Java Virtual Machine instance. In order to communicate tasks between the processes the [ProcBridge](https://github.com/gongzhang/procbridge) protocol is used in a client-server architecture.

## Usage

```xml
<dependency>
    <groupId>dev.bodewig.jcoprocessor</groupId>
    <artifactId>jcoprocessor</artifactId>
    <version>2.0.0</version>
</dependency>
```

A minimal `Server` implementation looks like the following:

```java
public class MyServer extends Server {

	public MyServer(int port) { super(port); }

	public static void main(String[] args) {
		if (args.length <= 0) {
			throw new IllegalArgumentException("Missing argument: port");
		}
		int port = Integer.valueOf(args[0]); // read the port from the args
		new MyServer(port).start(); // start the Server
	}

	@Override public Object handleRequest(String method, Object payload) {
		return method + " " + payload;
	}
}
```

You can then run the Server in a new `JCoprocess` and send requests:

```java
JCoprocess myProcess = JCoprocessManager.spawn(MyServer.class);
String result = myprocess.request("Hello", "World");
System.out.println(result); // prints "Hello World"
```

Check out the `JCoprocessTest` for another example.

---

This project is based on a fork of the [Java implementation of ProcBridge](https://github.com/gongzhang/procbridge-java).

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
