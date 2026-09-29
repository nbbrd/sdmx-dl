---
title: "Use in jbang script"
weight: 8
aliases:
  - /orchestration/use-in-jbang/
---

Automate **sdmx-dl** from a single Java script using [jbang](https://www.jbang.dev/), leveraging the Java API without Maven/Gradle setup.

A simple jbang Java script that uses the sdmx-dl API to print the last 12 observations as a CSV table:

```java
//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25+
//DEPS com.github.nbbrd.sdmx-dl:sdmx-dl-standalone:{{< sdmx-dl-version >}}

import sdmxdl.DataRequest;
import sdmxdl.web.SdmxWebManager;

void main() throws Exception {
    var data = SdmxWebManager.ofServiceLoader()
        .usingName("ECB")
        .getData(DataRequest.builder()
            .flowOf("EXR")
            .keyOf("M.CHF.EUR.SP00.A")
            .lastNObservations(12)
            .build());

    IO.println("Series,ObsPeriod,ObsValue");
    data.forEach(series -> series.getObs().forEach(obs ->
        IO.println(series.getKey() + "," + obs.getPeriod().getStart() + "," + obs.getValue())));
}
```

Save as `FetchSdmx.java` and run:

```bash
jbang FetchSdmx.java
```

## Notes

- **jbang setup**: Download jbang from https://www.jbang.dev/ or install via your package manager (`brew install jbang`, `choco install jbang`, etc.). After install, the first line (`//usr/bin/env jbang "$0" "$@" ; exit $?`) makes the script directly executable on Linux/macOS (`chmod +x FetchSdmx.java && ./FetchSdmx.java`).
- Everything in a single `.java` file: no project setup, no build tools needed.
- The `//JAVA 25+` line tells jbang which Java level the script expects.
- The `//DEPS` line declares the sdmx-dl dependency; jbang downloads it automatically on first run.
- Use jbang when you want to use the full sdmx-dl Java API without setting up a Maven/Gradle project.

## Related features

- [Discover sources]({{< relref "/usage/discover#sources" >}})
- [Retrieve data]({{< relref "/usage/retrieve#data" >}})
- [Web service]({{< relref "/ws" >}})





