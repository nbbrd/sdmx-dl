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
        format-util[util]
        csv
        xml
        kryo
    end
    subgraph providers
        provider-util[util]
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
