package com.steplabs.kafkaproducer.runnable;

import com.steplabs.kafkaproducer.avro.Device;
import com.steplabs.kafkaproducer.avro.Review;
import com.steplabs.kafkaproducer.avro.User;
import com.steplabs.kafkaproducer.model.DataRecord;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.TopicExistsException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;






public class ProducerAvroExample {
    // Create topic in Confluent Cloud
    public static void createTopic(final String topic,
                                   final int partitions,
                                   final int replication,
                                   final Properties cloudConfig) {
        final NewTopic newTopic = new NewTopic(topic, partitions, (short) replication);
        try (final AdminClient adminClient = AdminClient.create(cloudConfig)) {
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        } catch (final InterruptedException | ExecutionException e) {
            // Ignore if TopicExistsException, which may be valid if topic exists
            if (!(e.getCause() instanceof TopicExistsException)) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(final String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Please provide command line arguments: configPath topic");
            System.exit(1);
        }

        // Load properties from a configuration file
        // The configuration properties defined in the configuration file are assumed to include:
        //   ssl.endpoint.identification.algorithm=https
        //   sasl.mechanism=PLAIN
        //   bootstrap.servers=<CLUSTER_BOOTSTRAP_SERVER>
        //   sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="<CLUSTER_API_KEY>" password="<CLUSTER_API_SECRET>";
        //   security.protocol=SASL_SSL
        //   basic.auth.credentials.source=USER_INFO
        //   schema.registry.basic.auth.user.info=<SR_API_KEY>:<SR_API_SECRET>
        //   schema.registry.url=https://<SR ENDPOINT>
        //final Properties props = loadConfig(args[0]);
        String fin="org.apache.kafka.common.security.plain.PlainLoginModule required username='2WZINDSMSH4GYR6Q' password='9w6mBQH8G5qKC6bPetBQZzWv8a8AtzEmGp8lXwrVUqCOVb76iihU//TFsOiyA0KB';";
        final Properties props= new Properties();
        System.out.println(args[0]);
        props.put("bootstrap.servers","pkc-4nym6.us-east-1.aws.confluent.cloud:9092");
        props.put("ssl.endpoint.identification.algorithm","https");
        props.put("security.protocol","SASL_SSL");
        props.put("sasl.mechanism","PLAIN");
        props.put("sasl.jaas.config", fin);
        props.put("basic.auth.credentials.source","USER_INFO");
        props.put("schema.registry.basic.auth.user.info","F5DXHWW6RA54EFXI:efGvDsZLSkXquhH00e4qtaWRG4Eikh/YV5Ix/xZPmQlJNvn8TgFql42RuE44h9mY");
        props.put("schema.registry.url","https://psrc-lo3do.us-east-2.aws.confluent.cloud");

        // Create topic if needed
        final String topic = "test2";
        createTopic(topic, 1, 3, props);


        // Add additional properties.

        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);


        Producer<String, Review> producer = new KafkaProducer<String, Review>(props);

        // Produce sample data
        final Long numMessages = 10L;
        for (Long i = 0L; i < numMessages; i++) {
            String key = "alice";
            User user=new User();
            Device device=new Device();
            user.setName("shreyas");
            user.setReviewerLanguage("kannada");
            device.setManufacturer("Apple");
            device.setProductName("iphone");
            device.setRamMb(2l);
            Review record = new Review();
            record.setDevice(device);
            record.setUser(user);
            record.setModified(3l);
            record.setReviewId("133");
            record.setStarRating(5l);
            record.setComments("hello"+i);

            System.out.printf("Producing record: %s\t%s%n", key, record);
            producer.send(new ProducerRecord<String, Review>(topic, key, record), new Callback() {
                @Override
                public void onCompletion(RecordMetadata m, Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                    } else {
                        System.out.printf("Produced record to topic %s partition [%d] @ offset %d%n", m.topic(), m.partition(), m.offset());
                    }
                }
            });
        }

        producer.flush();

        System.out.printf("10 messages were produced to topic %s%n", topic);

        producer.close();
    }

    public static Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }
}
