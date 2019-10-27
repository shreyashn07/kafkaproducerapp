package com.steplabs.kafkaproducer;
import com.typesafe.config.Config;

public class AppConfig {

    private final String bootstrapServers;
    private final String schemaRegistryUrl;
    private final String topicName;
    private final Integer queueCapacity;
    private final Integer producerFrequencyMs;
    private final String packageName;
    private final String serviceEmail;
    private final String appName;
    private final Integer retryBackoff;
    private final String securityProtocol;
    private final String sslEndpointIdentificationAlgorithm;
    private final String saslMechanism;
    private final String saslJaasConfig;






    public AppConfig(Config config) {
        this.bootstrapServers = config.getString("kafka.bootstrap.servers");
        this.schemaRegistryUrl = config.getString("kafka.schema.registry.url");
        this.topicName = config.getString("kafka.topic.name");
        this.queueCapacity = config.getInt("app.queue.capacity");
        this.producerFrequencyMs = config.getInt("app.producer.frequency.ms");
        this.packageName=config.getString("app.package.name");
        this.serviceEmail=config.getString("app.service.account.email");
        this.appName=config.getString("app.app.name");
        this.retryBackoff=config.getInt("kafka.retry.backoff.ms");
        this.securityProtocol=config.getString("kafka.security.protocol");
        this.sslEndpointIdentificationAlgorithm=config.getString("kafka.ssl.endpoint.identification.algorithm");
        this.saslMechanism=config.getString("kafka.sasl.mechanism");
        this.saslJaasConfig=config.getString("kafka.sasl.jaas.config");

    }


    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public String getTopicName() {
        return topicName;
    }

    public Integer getQueueCapacity() {
        return queueCapacity;
    }

    public Integer getProducerFrequencyMs() {
        return producerFrequencyMs;
    }

    public String getPackageName(){return packageName;}

    public String getServiceEmail(){return serviceEmail;}

    public String getAppName(){return appName;}

    public String getSecurityProtocol(){return securityProtocol;}

    public String getSslEndpointIdentificationAlgorithm(){return sslEndpointIdentificationAlgorithm;}

    public String getSaslMechanism(){return saslMechanism;}

    public String getSaslJaasConfig(){return saslJaasConfig;}

    public Integer getRetryBackoff(){return retryBackoff;}


}
