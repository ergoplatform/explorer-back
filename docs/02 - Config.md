## Config

For config we are using `pureconfig` library.

All corresponding case classes are placed in `org.ergoplatform.explorer.config` package.

* `ExplorerConfig.scala` is just a top level holder for other configs.
* `DbCobfig.scala` database config. Contains all db related info.
* `HttpConfig` web service config. Contains port and host info.
* `GrabberConfig` grabber config. See [here](06%20-%20Data%20grabber.md)