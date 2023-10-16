---
title: "Options"
weight: 4
---

An option has the following caracteristics:
- it has a name prefixed by two hyphens (e.g. `--version`).
- it may have a shortcut prefixed by one hyphen (e.g. `-h` is a shortcut of `--help`).
- it is case-sensitive

Shortcuts can be grouped behind one hyphen (e.g. `-vh` is equivalent to `-v -h`).

The options default values are system-independent.

## Common

General-purpose options used by various commands.

| Name                                                        | Shortcut | Parameter | Description                         |
|-------------------------------------------------------------|----------|-----------|-------------------------------------|
| <a id="verbose" href="#verbose">`--verbose`</a>             | `-v`     | -         | Enable verbose mode.                |
| <a id="help" href="#help">`--help`</a>                      | `-h`     | -         | Show this help message and exit.    |
| <a id="version" href="#version">`--version`</a>             | `-V`     | -         | Print version information and exit. |
| <a id="no-parallel" href="#no-parallel">`--no-parallel`</a> | -        | -         | Disable parallel execution.         |
| <a id="sort" href="#sort">`--sort`</a>                      | -        | -         | Sort output by natural order.       |

## SDMX

SDMX-specific options.

| Name                                                  | Shortcut | Parameter                       | Description                                 |
|-------------------------------------------------------|----------|---------------------------------|---------------------------------------------|
| <a id="sources" href="#sources">`--sources`</a>       | `-s`     | [`<file>`](../datatypes#file)   | File that provides data source definitions. |
| <a id="languages" href="#languages">`--languages`</a> | `-l`     | [`<langs>`](../datatypes#langs) | Language priority list.                     |

## CSV

CSV options used to output content.  
The format is [RFC4180](https://tools.ietf.org/html/rfc4180).

| Name                                               | Shortcut | Parameter                             | Description                           |
|----------------------------------------------------|----------|---------------------------------------|---------------------------------------|
| <a id="output" href="#output">`--output`</a>       | `-o`     | [`<file>`](../datatypes#file)         | Output to a file instead of stdout.   |
| <a id="gzipped" href="#gzipped">`--gzipped`</a>    | `-z`     | -                                     | Compress the output file with gzip.   |
| <a id="append" href="#append">`--append`</a>       | -        | -                                     | Append to the end of the output file. |
| <a id="encoding" href="#encoding">`--encoding`</a> | `-e`     | [`<encoding>`](../datatypes#encoding) | Charset used to encode text.          |

## Network

Network-related options used to deal with performance, proxies, security and authentication.

| Name                                                                   | Shortcut | Parameter                              | Description                                                     |
|------------------------------------------------------------------------|----------|----------------------------------------|-----------------------------------------------------------------|
| <a id="auto-proxy" href="#auto-proxy">`--[no-]auto-proxy`</a>          | -        | -                                      | Enable automatic proxy detection.                               |
| <a id="curl" href="#curl">`--[no-]curl`</a>                            | -        | -                                      | Use curl backend instead of JDK.                                |
| <a id="no-default-ssl" href="#no-default-ssl">`--[no-]default-ssl`</a> | -        | -                                      | Disable default truststore.                                     |
| <a id="no-system-ssl" href="#no-system-ssl">`--[no-]system-ssl`</a>    | -        | -                                      | Disable system truststore.                                      |
| <a id="no-cache" href="#no-cache">`--no-cache`</a>                     | -        | -                                      | Disable caching.                                                |
| <a id="no-system-auth" href="#no-system-auth">`--system-auth`</a>      | -        | -                                      | Disable system authentication.                                  |
| <a id="user" href="#user">`--user`</a>                                 | -        | [`<user:password>`](../datatypes#user) | Specify the user and password to use for server authentication. |
