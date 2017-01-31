package com.grafjo.rundeck.plugin.influxdb.annotation

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


@ToString(includeNames = true)
@EqualsAndHashCode
class PluginConfiguration {

    String connectionUrl
    String database
    String retentionPolicy
    String measurement
    String username
    String password

    static fromMap = { Map<String, Object> configuration ->

        String connectionUrl = "${configuration.get('protocol')}://${configuration.get('host')}:${configuration.get('port')}"

        def currentConfiguration = new PluginConfiguration(
                connectionUrl: connectionUrl,
                database: configuration.get('database'),
                retentionPolicy: configuration.get('retention_policy'),
                measurement: configuration.get('measurement'),
                username: configuration.get('username'),
                password: configuration.get('password')
        )

        return currentConfiguration
    }

}
