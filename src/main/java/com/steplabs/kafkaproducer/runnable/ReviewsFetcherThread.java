package com.steplabs.kafkaproducer.runnable;

import com.steplabs.kafkaproducer.AppConfig;

import com.steplabs.kafkaproducer.client.PlayStoreRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.steplabs.kafkaproducer.avro.Device;
import com.steplabs.kafkaproducer.avro.Review;
import com.steplabs.kafkaproducer.avro.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ReviewsFetcherThread implements Runnable{
    private Logger log = LoggerFactory.getLogger(ReviewsFetcherThread.class.getSimpleName());

    private final AppConfig appConfig;
    private final ArrayBlockingQueue<Review> reviewsQueue;
    private final CountDownLatch latch;
    private PlayStoreRestClient playStoreClient;


    public ReviewsFetcherThread(AppConfig appConfig,ArrayBlockingQueue<Review> reviewsQueue, CountDownLatch latch){
       this.appConfig= appConfig;
       this.reviewsQueue=reviewsQueue;
       this.latch=latch;
       playStoreClient=new PlayStoreRestClient(appConfig.getPackageName(),appConfig.getAppName(),appConfig.getServiceEmail());

    }

    @Override
    public void run(){

        try{
            Boolean keepOnRunning= true;
            while(keepOnRunning){
                List<Review> reviews = new ArrayList<>();

                try {
                    reviews.addAll(playStoreClient.getNextReviews());
                    log.info("Fetched " + reviews.size() + " reviews");
                    if (reviews.size() == 0) {
                        keepOnRunning = false;
                    } else {
                        // this may block if the queue is full - this is flow control
                        log.info("Queue size :" + reviewsQueue.size());
                        for (Review review : reviews) {
                            reviewsQueue.put(review);
                        }

                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    Thread.sleep(500);
                } finally {
                    Thread.sleep(50);
                }

            }
        } catch (InterruptedException e) {
            log.warn("REST Client interrupted");
        } finally {
            this.close();
        }


    }

    private void close(){
        log.info("Queue size after insertion:" + reviewsQueue.size());
        log.info("closing");
        playStoreClient.close();
        latch.countDown();
        log.info("Closed");

    }




}










