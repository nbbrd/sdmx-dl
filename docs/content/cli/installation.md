---
title: "Installation"
weight: 1
---

{{< toc >}}

## Requirements

**sdmx-dl CLI** runs on any operating systems that support the Java VM (Virtual Machine) such as Microsoft **Windows**, **Solaris** OS, Apple **macOS**, **Ubuntu** and other various **Linux** distributions.

It requires a **Java SE Runtime Environment (JRE) version 8 or later** to run on such as **OpenJDK**. You can download a free, binary distribution of the OpenJDK at one of the following provider:
[Zulu JDK](https://www.azul.com/downloads/zulu/),
[Adoptium](https://adoptium.net/),
[Amazon Corretto](https://aws.amazon.com/corretto/).

## Automatic installation

The easiest way of installing the CLI is to use a package manager.  
Each operating system has its own manager. See the list below for specific instructions.

{{< tabs "uniqueid" >}}

{{< tab "Windows" >}}

#### <a href="https://github.com/nbbrd/scoop-nbbrd">Scoop bucket</a>
```shell
scoop bucket add nbbrd https://github.com/nbbrd/scoop-nbbrd.git
scoop install sdmx-dl
```
<br>

#### <a href="https://github.com/nbbrd/jbang-catalog">JBang catalog</a>
```shell
jbang sdmx-dl@nbbrd <command> [<args>]
```
<br>

#### <a href="https://search.maven.org/artifact/com.github.nbbrd.sdmx-dl/sdmx-dl-cli">JBang Maven coordinate</a>
```shell
jbang com.github.nbbrd.sdmx-dl:sdmx-dl-cli:RELEASE:bin <command> [<args>]
```
<br>

{{< /tab >}}

{{< tab "macOS" >}} 

#### <a href="https://github.com/nbbrd/homebrew-tap">Homebrew tap</a>
```shell
brew install nbbrd/tap/sdmx-dl
```
<br>

#### <a href="https://github.com/nbbrd/jbang-catalog">JBang catalog</a>
```shell
jbang sdmx-dl@nbbrd <command> [<args>]
```
<br>

#### <a href="https://search.maven.org/artifact/com.github.nbbrd.sdmx-dl/sdmx-dl-cli">JBang Maven coordinate</a>
```shell
jbang com.github.nbbrd.sdmx-dl:sdmx-dl-cli:RELEASE:bin <command> [<args>]
```
<br>

{{< /tab >}}

{{< tab "Linux" >}} 

#### <a href="https://github.com/nbbrd/homebrew-tap">Homebrew tap</a>
```shell
brew install nbbrd/tap/sdmx-dl
```
<br>

#### <a href="https://github.com/nbbrd/jbang-catalog">JBang catalog</a>
```shell
jbang sdmx-dl@nbbrd <command> [<args>]
```
<br>

#### <a href="https://search.maven.org/artifact/com.github.nbbrd.sdmx-dl/sdmx-dl-cli">JBang Maven coordinate</a>
```shell
jbang com.github.nbbrd.sdmx-dl:sdmx-dl-cli:RELEASE:bin <command> [<args>]
```
<br>

{{< /tab >}}

{{< tab "Docker" >}}
#### <a href="https://hub.docker.com/repository/docker/jbangdev/jbang-action">JBang container on dockerhub</a>
```shell
docker run -v `pwd`:/ws --workdir=/ws jbangdev/jbang-action sdmx-dl@nbbrd <command> [<args>]
```
<br>

#### <a href="https://quay.io/repository/jbangdev/jbang-action">JBang container on quay.io</a>
```shell
docker run -v `pwd`:/ws --workdir=/ws quay.io/jbangdev/jbang-action sdmx-dl@nbbrd <command> [<args>]
```
<br>

{{< /tab >}}

{{< tab "GitHub Actions" >}}
#### <a href="https://github.com/marketplace/actions/java-scripting-w-jbang">JBang action</a>&nbsp;<a href="https://github.com/nbbrd/jbang-catalog">with catalog</a>
```yml
- uses: jbangdev/jbang-action@v0.98.0
  with:
    trust: https://github.com/nbbrd/jbang-catalog
    script: sdmx-dl@nbbrd
    scriptargs: "<command> [<args>]"
```
<br>

#### <a href="https://github.com/marketplace/actions/java-scripting-w-jbang">JBang action</a>&nbsp;<a href="https://search.maven.org/artifact/com.github.nbbrd.sdmx-dl/sdmx-dl-cli">with Maven coordinate</a>
```yml
- uses: jbangdev/jbang-action@v0.98.0
  with:
    script: com.github.nbbrd.sdmx-dl:sdmx-dl-cli:RELEASE:bin
    scriptargs: "<command> [<args>]"
```
<br>

{{< /tab >}}

{{< tab "Maven" >}}
#### <a href="https://github.com/jbangdev/jbang-maven-plugin/">JBang Maven plugin</a>&nbsp;<a href="https://github.com/nbbrd/jbang-catalog">with catalog</a>
```xml
<plugin>
  <groupId>dev.jbang</groupId>
  <artifactId>jbang-maven-plugin</artifactId>
  <version>0.0.7</version>
  <executions>
    <execution>
      <id>run</id>
      <phase>process-resources</phase>
      <goals>
        <goal>run</goal>
      </goals>
      <configuration>
        <trusts>
          <trust>https://github.com/nbbrd/jbang-catalog</trust>
        </trusts>
        <script>sdmx-dl@nbbrd</script>
        <args>
          <arg>...</arg>
        </args>
      </configuration>
    </execution>
  </executions>
</plugin>
```
<br>

#### <a href="https://github.com/jbangdev/jbang-maven-plugin/">JBang Maven plugin</a>&nbsp;<a href="https://search.maven.org/artifact/com.github.nbbrd.sdmx-dl/sdmx-dl-cli">with Maven coordinate</a>
```xml
<plugin>
  <groupId>dev.jbang</groupId>
  <artifactId>jbang-maven-plugin</artifactId>
  <version>0.0.7</version>
  <executions>
    <execution>
      <id>run</id>
      <phase>process-resources</phase>
      <goals>
        <goal>run</goal>
      </goals>
      <configuration>
        <script>com.github.nbbrd.sdmx-dl:sdmx-dl-cli:RELEASE:bin</script>
        <args>
          <arg>...</arg>
        </args>
      </configuration>
    </execution>
  </executions>
</plugin>
```
<br>

#### <a href="">Maven command-line</a>
```shell
mvn dependency:copy -Dartifact=com.github.nbbrd.sdmx-dl:sdmx-dl-cli:RELEASE:jar:bin -DoutputDirectory=. -Dmdep.stripVersion -q
java -jar sdmx-dl-cli-bin.jar <command> [<args>]
```

{{< /tab >}}

{{< tab "Gradle" >}}
#### <a href="https://github.com/jbangdev/jbang-gradle-plugin/">JBang Gradle plugin<a>
```groovy
plugins {
  id 'dev.jbang' version '0.2.0'
}
```
<br>

{{< /tab >}}

{{< /tabs >}}

## Manual installation

The manual installation of the CLI is straighforward:

1. Download the latest jar binary (`sdmx-dl-cli-_VERSION_-bin.jar`) at:  
   [https://github.com/nbbrd/sdmx-dl/releases/latest](https://github.com/nbbrd/sdmx-dl/releases/latest)
2. Copy this jar (i.e. `_JAR_`) to any folder on your system (i.e. `_DIR_`)
3. Create launchers in the installation directory:  
   `java -jar "_DIR_\_JAR_" setup launcher -t BASH -o "_DIR_\sdmx-dl"`  
   `java -jar "_DIR_\_JAR_" setup launcher -t CMD -o "_DIR_\sdmx-dl.bat"`  
   `java -jar "_DIR_\_JAR_" setup launcher -t PS1 -o "_DIR_\sdmx-dl.ps1"`
4. Add launchers directory (`_DIR_`) in system path:  
   https://gist.github.com/nex3/c395b2f8fd4b02068be37c961301caa7

## Zero installation

The CLI is a single executable jar, so it doesn't need to be installed to be used.  
To use the CLI without installing it:

1. Download the latest jar binary (`sdmx-dl-cli-_VERSION_-bin.jar`) at:  
   [https://github.com/nbbrd/sdmx-dl/releases/latest](https://github.com/nbbrd/sdmx-dl/releases/latest)
2. Run this jar by calling:  
   `java -jar sdmx-dl-cli-_VERSION_-bin.jar <command> [<args>]`

## Troubleshooting

If the launching of sdmx-dl fails, you can try the following operations in a terminal:

1. Check if Java is properly installed:  
   `java -version`
2. Check if sdmx-dl is available:  
   `sdmx-dl --version`
