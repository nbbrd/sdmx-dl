---
title: "Authentication and credentials"
weight: 12
---

Configure how sdmx-dl authenticates against protected data sources — credentials are configured once, in a source config file shared by every flavor.

{{< tabs "auth-and-credentials" >}}

{{< tab "API" >}}
{{< feature-status "auth-and-credentials" "api" >}}

```properties
sdmxdl.driver.authScheme=BASIC
```

For OAuth2/Msal-backed sources, the same source configuration can also define:

```properties
sdmxdl.authenticator.clientId=...
sdmxdl.authenticator.authority=...
sdmxdl.authenticator.scopes=...
```
{{< /tab >}}

{{< tab "CLI" >}}
{{< feature-status "auth-and-credentials" "cli" >}}

The CLI reads the same source configuration file, so the same authentication properties apply when running commands such as:

```shell
sdmx-dl fetch data ECB EXR M.CHF.EUR.SP00.A --sources sources.csv
```
{{< /tab >}}

{{< tab "WS" >}}
{{< feature-status "auth-and-credentials" "ws" >}}

The web service uses the same source configuration model.

When a protected source is accessed through REST or gRPC, the credential flow is triggered on first use and secrets are stored in the native OS keystore when available.

### REST
```shell
curl -X POST -H "Content-Type: application/json" localhost:4559/sdmx-dl/data --data "{\"source\":\"ECB\",\"flow\":\"EXR\",\"key\":\"M.CHF.EUR.SP00.A\"}"
```

### gRPC
```shell
grpcurl -d '{"source":"ECB","flow":"EXR","key":"M.CHF.EUR.SP00.A"}' -plaintext localhost:4557 sdmxdl.grpc.SdmxWebManager.GetData
```
{{< /tab >}}

{{< /tabs >}}

## Notes

- Authentication is configuration, not code: the same `sdmxdl.driver.authScheme`/`sdmxdl.authenticator.*` properties apply whether the source config is read by the Java library, the CLI, or the web service.
- See [Authentication]({{< relref "/auth" >}}) for the full list of supported schemes (BASIC, MSAL) and their properties.
- Credentials/tokens are cached in the native OS keystore; the current implementation only supports Windows.

## Related features

- [Discover sources]({{< relref "/features/discover-sources" >}})
- [Retrieve data]({{< relref "/features/retrieve-data" >}})
