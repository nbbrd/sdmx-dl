---
title: "Java library"
weight: 1
---

![_work-in-progress_](https://img.shields.io/badge/-work_in_progress-E2BC4A)

**sdmx-dl API** is a library designed as a facade for the SDMX model and APIs.

## Structure overview

```mermaid
flowchart BT
    api
    subgraph formats
        format-base[base]
        csv
        kryo
        protobuf
        xml
    end
    subgraph providers
        provider-base[base]
        connectors
        ri
        dialects
        px
    end
    subgraph applications
        cli
        desktop
        grpc
    end
    testing

    formats --> api
    providers --> formats
    testing ---> api
    applications --> providers & testing
```

## Dependencies setup

sdmx-dl is **distributed in two flavours**: a **standard JAR hierarchy**, and a **standalone uber JAR** containing all the implementations and their dependencies.
Most of the standalone JAR’s dependencies are shaded i.e. they are hidden in alternative packages.
This allows sdmx-dl to be used in projects with conflicting versions of its dependencies.

Standard JAR hierarchy:

```xml
<dependencies>
  <dependency>
    <groupId>com.github.nbbrd.sdmx-dl</groupId>
    <artifactId>sdmx-dl-api</artifactId>
  </dependency>
  <dependency>
    <groupId>com.github.nbbrd.sdmx-dl</groupId>
    <artifactId>sdmx-dl-ri</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>com.github.nbbrd.sdmx-dl</groupId>
    <artifactId>sdmx-dl-dialects</artifactId>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```

Standalone uber JAR:

```xml
<dependencies>
  <dependency>
    <groupId>com.github.nbbrd.sdmx-dl</groupId>
    <artifactId>sdmx-dl-api</artifactId>
  <dependency>
    <groupId>com.github.nbbrd.sdmx-dl</groupId>
    <artifactId>sdmx-dl-standalone</artifactId>
    <scope>runtime</scope>
  </dependency>
</dependencies>
```
