package com.grafjo.rundeck.plugin.influxdb.annotation

import com.dtolabs.rundeck.core.common.INodeEntry
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.core.plugins.configuration.Describable
import com.dtolabs.rundeck.core.plugins.configuration.Description
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.step.NodeStepPlugin
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder
import com.dtolabs.rundeck.plugins.util.PropertyBuilder
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point

import static com.dtolabs.rundeck.core.plugins.configuration.PropertyScope.Project

@Plugin(name = "influxdb-annotation-step", service = ServiceNameConstants.WorkflowNodeStep)
class InfluxDBAnnotationStep implements NodeStepPlugin, Describable {

    private static final Integer DEBUG = 5

    @Override
    Description getDescription() {

        return DescriptionBuilder.builder()
                .name('influxdb-annotation-step')
                .title('InfluxDB: Annotation Step')
                .description('Send an annotations to a given InfluxDB.')
                .property(
                    PropertyBuilder.builder()
                            .required(true)
                            .values('http', 'https')
                            .select('protocol')
                            .description('InfluxDB HTTP API communication protocol')
                            .defaultValue('http')
                            .scope(Project)
                            .build()
                )
                .property(
                    PropertyBuilder.builder()
                            .required(true)
                            .string('host')
                            .description('InfluxDB HTTP API hostname or ip')
                            .defaultValue('127.0.0.1')
                            .scope(Project)
                            .build()
                )
                .property(
                    PropertyBuilder.builder()
                            .required(true)
                            .integer('port')
                            .description('InfluxDB HTTP API port')
                            .defaultValue('8086')
                            .scope(Project)
                            .build()
                )
                .property(
                    PropertyBuilder.builder()
                            .required(true)
                            .string('database')
                            .description('Name of InfluxDB database')
                            .scope(Project)
                            .build()
                )
                .property(
                    PropertyBuilder.builder()
                            .required(true)
                            .string('retention_policy')
                            .description('The retention policy of the annotations')
                            .defaultValue('autogen')
                            .scope(Project)
                            .build()
                )
                .property(
                    PropertyBuilder.builder()
                            .required(true)
                            .string('measurement')
                            .description('Name of the InfluxDB measurement - where the annotations are stored')
                            .defaultValue('events')
                            .scope(Project)
                            .build()
                )
                .property(
                    PropertyBuilder.builder()
                            .string('username')
                            .description('InfluxDB HTTP API username')
                            .scope(Project)
                            .build()
                )
                .property(
                    PropertyBuilder.builder()
                            .string('password')
                            .description('InfluxDB HTTP API password')
                            .renderingAsPassword()
                            .scope(Project)
                            .build()
                )
                .build()
    }

    @Override
    void executeNodeStep(PluginStepContext context, Map<String, Object> configuration, INodeEntry entry) throws NodeStepException {

        def LOGGER = context.getLogger()

        LOGGER.log(DEBUG, 'Parsing plugin configuration')
        def pluginConfiguration = PluginConfiguration.fromMap(configuration)

        LOGGER.log(DEBUG, 'Parsing executionStep metadata')
        Point.Builder builder = withPluginConfiguration(pluginConfiguration)
        builder = withNodeEntry(builder, entry)
        if(context.getDataContext().containsKey('job')) {
            builder = withJobContext(builder, context.getDataContext().get('job'))
        }

        LOGGER.log(DEBUG, 'Connection to InfluxDB')
        InfluxDB influxDB = createInfluxDBConnection(pluginConfiguration)

        LOGGER.log(DEBUG, 'Writing event to InfluxDB')
        influxDB.write(pluginConfiguration.getDatabase(), pluginConfiguration.getRetentionPolicy(), builder.build())

        LOGGER.log(DEBUG, 'Event written to InfluxDB')

        influxDB.close();
        LOGGER.log(DEBUG, 'Connection to InfluxDB closed')
    }


    private Point.Builder withPluginConfiguration(PluginConfiguration pluginConfiguration) {
        return Point.measurement(pluginConfiguration.getMeasurement())
    }

    private Point.Builder withJobContext(Point.Builder builder, Map<String, String> jobContext) {

        return builder
                .addField('rundeck_execution_id', jobContext.get('execid'))
                .addField('rundeck_execution_url', jobContext.get('url'))
                .tag('rundeck_job_id', jobContext.get('id'))
                .tag('rundeck_job_name', jobContext.get('name'))
                .tag('rundeck_job_group', jobContext.getOrDefault('group','unavailable'))
                .tag('rundeck_project', jobContext.get('project'))


    }

    private Point.Builder withNodeEntry(Point.Builder builder, INodeEntry node) {
        return builder
                .tag('rundeck_target_hostname', node.extractHostname())
                .tag('rundeck_target_nodename', node.getNodename())
    }

    private InfluxDB createInfluxDBConnection(PluginConfiguration pluginConfiguration) {

        if(pluginConfiguration.getUsername() && pluginConfiguration.getPassword()) {
            return InfluxDBFactory.connect(pluginConfiguration.getConnectionUrl(), pluginConfiguration.getUsername(), pluginConfiguration.getPassword())
        } else {
            return InfluxDBFactory.connect(pluginConfiguration.getConnectionUrl())
        }
    }
}
