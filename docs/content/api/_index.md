---
title: "Java library"
weight: 1
---

_work-in-progress_

## Dependency graph

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
    end
    testing
    cli

    formats --> api
    providers --> formats
    testing ---> api
    cli --> providers & testing
```
