# mu-server-docs

Documentation site for <http://muserver.io>

To run locally, execute the main method in `RunLocal.java`

Alternatively, with Java 17 or later:

```sh
mvn package
java -jar target/docs-1.0-SNAPSHOT.jar local
```

Open <https://localhost:8443> (the local HTTPS certificate is self-signed).

The site currently runs on Mu Server `0.0.3.7`, the v3 prerelease, while advertising
the stable `2.4.2` release. The optional `mu-server.display-version` setting in
`src/main/resources/site.properties` controls the version shown in the header,
dependency snippets and download links. Remove it or leave it blank to use the
Mu Server version from the Maven dependency. A JVM system property takes precedence:

```sh
java -Dmu-server.display-version=2.4.2 -jar target/docs-1.0-SNAPSHOT.jar local
```

Use `-Dmu-server.display-version=` to display the actual dependency version.
