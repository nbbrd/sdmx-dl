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
#### Scoop
```bat
scoop bucket add nbbrd https://github.com/nbbrd/scoop-nbbrd.git
scoop install sdmx-dl
```
https://github.com/nbbrd/scoop-nbbrd

#### JBang
```
// Download, cache, and run
jbang sdmx-dl@nbbrd <command> [<args>]
```
https://github.com/nbbrd/jbang-catalog
{{< /tab >}}

{{< tab "macOS" >}} 
#### Homebrew
```bash
brew install nbbrd/tap/sdmx-dl
```
https://github.com/nbbrd/homebrew-tap

#### JBang
```
// Download, cache, and run
jbang sdmx-dl@nbbrd <command> [<args>]
```
https://github.com/nbbrd/jbang-catalog
{{< /tab >}}

{{< tab "Linux" >}} 
#### Homebrew
```bash
brew install nbbrd/tap/sdmx-dl
```
https://github.com/nbbrd/homebrew-tap

#### JBang
```
// Download, cache, and run
jbang sdmx-dl@nbbrd <command> [<args>]
```
https://github.com/nbbrd/jbang-catalog
{{< /tab >}}

{{< tab "Docker" >}}
#### docker.io
```bash
docker run -v `pwd`:/ws --workdir=/ws jbangdev/jbang-action sdmx-dl@nbbrd --version
```
https://hub.docker.com/repository/docker/jbangdev/jbang-action

#### quay.io
```bash
docker run -v `pwd`:/ws --workdir=/ws quay.io/jbangdev/jbang-action sdmx-dl@nbbrd --version
```
https://quay.io/repository/jbangdev/jbang-action
{{< /tab >}}

{{< tab "GitHub Actions" >}}
#### JBang GitHub Action
```yml
- name: Print latest stable version of sdmx-dl
  uses: jbangdev/jbang-action@v0.81.2
  with:
    script: sdmx-dl@nbbrd
    scriptargs: "--version"
```
https://github.com/marketplace/actions/java-scripting-w-jbang
{{< /tab >}}

{{< tab "Maven" >}}
#### JBang Maven plugin
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
        <script>sdmx-dl@nbbrd</script>
        <args>
          <arg>--version</arg>
        </args>
      </configuration>
    </execution>
  </executions>
</plugin>
```
https://github.com/jbangdev/jbang-maven-plugin/
{{< /tab >}}

{{< tab "Gradle" >}}
#### JBang Gradle plugin
```groovy
plugins {
  id 'dev.jbang' version '0.2.0'
}
```
https://github.com/jbangdev/jbang-gradle-plugin/
{{< /tab >}}

{{< /tabs >}}

## Manual installation

The manual installation of the CLI is straighforward:

1. Download the latest jar binary (`sdmx-dl*-bin.jar`) at:  
   [https://github.com/nbbrd/sdmx-dl/releases/latest](https://github.com/nbbrd/sdmx-dl/releases/latest)
2. Copy this jar (i.e. `_JAR_`) to any folder on your system (i.e. `_DIR_`)
3. Create launchers in the install directory:  
   `java -jar "_DIR_\_JAR_" setup generate-launcher -t BASH -o "_DIR_\sdmx-dl"`  
   `java -jar "_DIR_\_JAR_" setup generate-launcher -t CMD -o "_DIR_\sdmx-dl.bat"`  
   `java -jar "_DIR_\_JAR_" setup generate-launcher -t PS1 -o "_DIR_\sdmx-dl.ps1"`
4. Add launchers directory (`_DIR_`) in system path:  
   https://gist.github.com/nex3/c395b2f8fd4b02068be37c961301caa7

## Zero installation

The CLI is a single executable jar, so it doesn't need to be installed to be used.  
To use the CLI without installing it:

1. Download the latest jar binary (`sdmx-dl*-bin.jar`) at:  
   [https://github.com/nbbrd/sdmx-dl/releases/latest](https://github.com/nbbrd/sdmx-dl/releases/latest)
2. Run this jar by calling:  
   `java -jar sdmx-dl-X.Y.Z-bin.jar <command> [<args>]`

## Troubleshooting

If the launching of sdmx-dl fails, you can try the following operations in a terminal:

1. Check if Java is properly installed:  
`java -version`
2. Check if sdmx-dl is available:  
`sdmx-dl --version`
