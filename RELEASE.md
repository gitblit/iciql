## Releasing

These are quick notes on using Maven to release artifacts to Sonatype.

### Snapshots

To release a snapshot to [Sonatype OSS Snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/):

    mvn clean deploy

### Releases

Releases are deployed to [Sonatype OSSRH Staging](http://central.sonatype.org/pages/releasing-the-deployment.html) and then manually synced to Central.

To release with automated versioning and SCM integration:

    mvn release:clean release:prepare
    mvn release:perform

To release manually:

    mvn versions:set -DnewVersion=1.2.3
    mvn clean deploy -P release

### Settings.xml

Sonatype uploads are authenticated and require credentials.
GPG signing requires a passphrase to unlock the signing key.
Both of these secrets can be stored in `~/.m2/settings.xml`.

```xml
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>jira-username</username>
      <password>jira-password</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>gpg-password</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```
