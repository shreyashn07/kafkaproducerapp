package com.steplabs.kafkaproducer.runnable;

import com.steplabs.kafkaproducer.AppConfig;
import com.steplabs.kafkaproducer.avro.Review;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ReviewsProducerThread implements Runnable {

    private Logger log = LoggerFactory.getLogger(ReviewsProducerThread.class.getSimpleName());

    private final AppConfig appConfig;
    private final ArrayBlockingQueue<Review> reviewsQueue;
    private final CountDownLatch latch;
    private final KafkaProducer<Long, Review> kafkaProducer;
    private final String targetTopic;

    public ReviewsProducerThread(AppConfig appConfig,
                                     ArrayBlockingQueue<Review> reviewsQueue,
                                     CountDownLatch latch) {
        this.appConfig = appConfig;
        this.reviewsQueue = reviewsQueue;
        this.latch = latch;
        this.kafkaProducer = createKafkaProducer(appConfig);
        this.targetTopic = appConfig.getTopicName();
    }

    public KafkaProducer<Long, Review> createKafkaProducer(AppConfig appConfig) {
        Properties props = new Properties();
        String fin="org.apache.kafka.common.security.plain.PlainLoginModule required username='xxx' password='xxx';";
        props.put("bootstrap.servers","xxx.aws.confluent.cloud:9092");
        props.put("ssl.endpoint.identification.algorithm","https");
        props.put("security.protocol","SASL_SSL");
        props.put("sasl.mechanism","PLAIN");
        props.put("sasl.jaas.config", fin);
        props.put("basic.auth.credentials.source","USER_INFO");
        props.put("schema.registry.basic.auth.user.info","xxx");
        props.put("schema.registry.url","https://xxx.aws.confluent.cloud");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        return new KafkaProducer<>(props);
    }

    @Override
    public void run() {
        int reviewCount = 0;
        try {
            while (latch.getCount() > 1 || reviewsQueue.size() > 0){
                Review review = reviewsQueue.poll();
                if (review == null) {
                    Thread.sleep(200);
                } else {
                    reviewCount += 1;
                    log.info("Sending review " + reviewCount + ": " + review);
                    kafkaProducer.send(new ProducerRecord<>(targetTopic, review), (recordMetadata, e) -> {
                        if(e!=null)
                        {
                            e.printStackTrace();
                        }else{

                            System.out.printf("Produced record to topic %s partition [%d] @ offset %d%n", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
                        }

                    });
                    // sleeping to slow down the pace a bit
                    Thread.sleep(appConfig.getProducerFrequencyMs());
                }
            }
        } catch (InterruptedException e) {
            log.warn("Avro Producer interrupted");
        } finally {
            close();
        }
    }

    public void close() {
        log.info("Closing Producer");
        kafkaProducer.close();
        latch.countDown();
    }
}
