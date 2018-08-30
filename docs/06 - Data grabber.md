## Grabber

To find blockchain node grabber will use config.
Corresponding config class called `GrabberConfig.scala`. This config being read from `application.conf`
file from the `graaber` part. 
It consists of two fields
1. *nodes* is array of links to the real blockchain node api (currently grabber is using only first one)
2. *poll-delay* is just a polling dealy in a finite duration format.

After start polling grabber will grab all the blocks between blockchain's height and db height.
Eventually this job will be done, and grabber will take a `poll-delay` timeout and then ask blockchain node if there is a new blocks to grab.
If there is new blocks - it will grab them, if not - it will take another `poll-delay` timeout.

Grabber based in a package `org.ergoplatform.explorer.grabber.*`.
There is couple of sub packages.
* `db` - all classes that responsible for writing grabbed data into database
* `http` - all classes responsible for polling blockchain node api.
* `protocol` - all classes that describes json from blockchain node api.

Grabber service is being started as an async task in `App.scala`