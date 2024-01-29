---
title: "Configuration"
weight: 2
---

**sdmx-dl CLI** is designed to work out-of-the-box but can be configured if needed by using several [properties files](#properties-files).

{{< toc >}}

## Properties files

The [properties files](https://en.wikipedia.org/wiki/.properties) are just text files that contains a list of key-value pairs.  
All these files are named `sdmx-dl.properties` and are located in 3 different folders which are parsed in a specific order to define a scope:
1. `SYSTEM`: the installation folder (where the binary is located)
2. `GLOBAL`: the user profile folder (`%userprofile%` on Windows)
3. `LOCAL`: the execution folder (where the CLI is called)

Each of these scopes overwrites properties defined in the previous scope; for example, properties in execution folder overrides those in user profile folder.

## Command options

The default values of [command options](../options) can be overridden in two ways:

- **Widely** by targeting any command using their option names.  
    ```properties
    # enable verbose mode on all commands
    verbose=true
    # use UNIX new-line type on all commands
    new-line=UNIX
    ```
- **Narrowly** by targeting a specific command using their option names prefixed by the command full path.  
    ```properties
    # enable verbose mode on status command
    sdmx-dl.check.status.verbose=true
    # use UNIX new-line type on status command
    sdmx-dl.check.status.new-line=UNIX
    ```

## Execution properties

Execution properties can be customized using [Java system property](https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html).  
There are several types of system properties: some are defined by the platform, others by the libraries.

{{< expand "sdmx-dl properties" >}}

| Property                   | Description                               |
|----------------------------|-------------------------------------------|
| `sdmxdl.driver.dumpFolder` | Folder used to dump body of web responses |

{{< /expand >}}

{{< expand "Command-line properties" >}}

| Property                            | Description                      |
|-------------------------------------|----------------------------------|
| `picocli.ansi`                      | Disables ANSI colors on output   |
| `org.fusesource.jansi.Ansi.disable` | Disables ANSI support of Windows |

{{< /expand >}}

{{< expand "Networking properties" >}}

| Property             | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `https.proxyHost`    | The hostname, or address, of the proxy server                               |
| `https.proxyPort`    | The port number of the proxy server                                         |
| `http.proxyHost`     | The hostname, or address, of the proxy server                               |
| `http.proxyPort`     | The port number of the proxy server                                         |
| `http.nonProxyHosts` | Indicates the hosts that should be accessed without going through the proxy |

More on https://docs.oracle.com/javase/8/docs/api/java/net/doc-files/net-properties.html

{{< /expand >}}

{{< expand "Security properties" >}}

| Property                           | Description          |
|------------------------------------|----------------------|
| `javax.net.ssl.trustStore`         | Trust Store Path     |
| `javax.net.ssl.trustStorePassword` | Trust Store Password |
| `javax.net.ssl.trustStoreType`     | Trust Store Type     |
| `javax.net.ssl.keyStore`           | Key Store Path       |
| `javax.net.ssl.keyStorePassword`   | Key Store Password   |
| `javax.net.ssl.keyStoreType`       | Key Store Type       |

More on https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#InstallationAndCustomization

{{< /expand >}}

## Troubleshooting

Using properties files is usually error-prone.  
Fortunately, sdmx-dl CLI includes a [command to pinpoint the problems](../usage#check-properties): `sdmx-dl check properties`

Common problems:

| Problem                                 | Cause                                                                | 
|-----------------------------------------|----------------------------------------------------------------------|
| Property is not available in the output | The property file is either not in the right folder or is misspelled | 
| Property doesn't have the right type    | The property name is misspelled                                      | 
