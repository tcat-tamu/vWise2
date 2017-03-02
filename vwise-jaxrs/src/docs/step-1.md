# Step 1. Initial Project Stub
The initial project stub sets up a working JAX-RS base project (currently without any resources)
and tooling to launch that application from Maven using the Grizzly server 
(see edu.tamu.tcat.vwise.internal.GrizzlyServerMain).

The initial project sets up a [configuration file](https://12factor.net/config). This is stored 
externally to the project that we will use to support deployment configurations. Configuration 
information is something we don't want to check into source code (no, not ever) because it 
tends to accumulate private information (passwords, etc), we'll keep this file outside of the 
source tree. 

Create a file named `config.vwise.properties` somewhere appropriate. For now, we just need to 
setup the hostname and path for the server. We can add to this as we go.

```properties
# The host URL of the REST API
grizzly.server.host=http://localhost:8080

# The path for the deployed service.
grizzly.server.approot=/vwise
```

Now, to start the application, type the following:

```bash
mvn exec:java -Dconfig.file=D:/Projects/vWise/config/config.demo.properties
```
