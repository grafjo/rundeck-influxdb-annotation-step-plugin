# Rundeck InfluxDB Annotation Step Plugin

This plugin will send an annotation to your InfluxDB to track the event of a Rundeck job execution in your InfluxDB.

See [Grafana](http://docs.grafana.org/reference/annotations/) how to display use annotations from InfluxDB with Grafana.


## Usage

To use this module with the default configuration, just start with this:

```
./gradlew build
```

Copy  `build/libs/rundeck-influxdb-annotation-plugin-*.jar` to your `libext` folder (e.g. /var/lib/rundeck/libext)

Restart rundeck

Go to `Installed and Bundled Plugins` and see ` Workflow Steps `


## Authors
* Johannes Graf ([@grafjo](https://github.com/grafjo))


## License

rundeck-influxdb-annotation-step-plugin is released under the MIT License.
See the bundled LICENSE file for details.
