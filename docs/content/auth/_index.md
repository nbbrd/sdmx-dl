---
title: "Authentication"
weight: 4
---

**sdmx-dl** support multiple authentication schemes:

- [BASIC](#basic-scheme): username/password pair
- [MSAL](#msal-scheme): Microsoft Authentication Library (OAuth2)

The authentication scheme is defined in the data source configuration using the `sdmxdl.driver.authScheme` property.

## BASIC scheme

The BASIC scheme is used for data sources that require a username/password pair to access their data.
When using this scheme, the credentials are prompted when accessing the data for the first time. 
These credentials are stored securely for future use in the [OS keystore](#native-os-keystore).

## MSAL scheme

The MSAL scheme is used for data sources that require OAuth2 authentication using the [Microsoft Authentication Library](https://docs.azure.cn/en-us/entra/identity-platform/authentication-flows-app-scenarios).
When using this scheme, the user is prompted to authenticate via a web browser when accessing the data for the first time.
The authentication tokens are stored securely for future use in the [OS keystore](#native-os-keystore).

{{< hint type="info" >}}
The current implementation only supports the Authorization Code Flow with PKCE (Proof Key for Code Exchange) for public client applications.
{{< /hint >}}

This scheme requires additional configuration properties:

| Property                                                                                                                                                                                                                                                                                   | Type                      | Default            |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------|--------------------|
| `sdmxdl.authenticator.clientId` <br><small style="padding: .25rem .5rem; display: inline-block;"><i> Client ID \(Application ID\) of the application as registered in the application registration portal.                                                                                                                                       | `String`                  | _required_         |
| `sdmxdl.authenticator.authority` <br><small style="padding: .25rem .5rem; display: inline-block;"><i> URL of the authenticating authority or security token service \(STS\) from which MSAL will acquire security tokens.                                                                                                                        | `URL`                     | _required_         |
| `sdmxdl.authenticator.scopes` <br><small style="padding: .25rem .5rem; display: inline-block;"><i> Scopes application is requesting access to.                                                                                                                                                                                                   | `Comma-separated Strings` | _required_         |
| `sdmxdl.authenticator.redirectUri` <br><small style="padding: .25rem .5rem; display: inline-block;"><i> Redirect URI where MSAL will listen to for the authorization code returned by Azure AD. Should be a loopback address with a port specified \(for example, http://localhost:3671\). If no port is specified, MSAL will find an open port. | `URI`                     | `http://localhost` |
| `sdmxdl.authenticator.uid` <br><small style="padding: .25rem .5rem; display: inline-block;"><i> An optional UID used to store tokens.                                                                                                                                                                                                            | `String`                  |                    |

## Native OS keystore

**sdmx-dl** uses native OS keystores to store credentials and tokens securely.

{{< hint type="info" >}}
The current implementation only supports Windows.
{{< /hint >}}
