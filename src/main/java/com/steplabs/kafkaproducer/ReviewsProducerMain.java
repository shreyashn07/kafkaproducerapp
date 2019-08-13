package com.steplabs.kafkaproducer;



import com.steplabs.kafkaproducer.runnable.ReviewsFetcherThread;
import com.steplabs.kafkaproducer.runnable.ReviewsProducerThread;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.steplabs.kafkaproducer.avro.Device;
import com.steplabs.kafkaproducer.avro.Review;
import com.steplabs.kafkaproducer.avro.User;


import java.util.List;
import java.util.concurrent.*;

public class ReviewsProducerMain {

    private Logger log= LoggerFactory.getLogger(ReviewsProducerMain.class.getSimpleName());

    private ExecutorService executor;
    private CountDownLatch latch;
    private ReviewsFetcherThread playStoreRestClient;
    private ReviewsProducerThread reviewsProducer;


    public static void main(String args[]){

        ReviewsProducerMain reviewsProducerMain=new ReviewsProducerMain();
        reviewsProducerMain.start();


    }
    private ReviewsProducerMain(){
        AppConfig appConfig=new AppConfig(ConfigFactory.load());
        latch =new CountDownLatch(2);
        executor= Executors.newFixedThreadPool(2);
        ArrayBlockingQueue<Review> reviewsQueue= new ArrayBlockingQueue<>(appConfig.getQueueCapacity());
        playStoreRestClient = new ReviewsFetcherThread(appConfig,reviewsQueue,latch);
        reviewsProducer= new ReviewsProducerThread(appConfig,reviewsQueue,latch);

    }


    private void start(){

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!executor.isShutdown()){
                log.info("Shutdown requested");
                shutdown();
            }
        }));

        log.info("Application started!");
        //executor.submit(udemyRESTClient);
        executor.submit(playStoreRestClient);
        executor.submit(reviewsProducer);
        log.info("Stuff submit");
        try {
            log.info("Latch await");
            latch.await();
            log.info("Threads completed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            shutdown();
            log.info("Application closed succesfully");
        }

    }

    private void shutdown(){
        if(!executor.isShutdown()){
            log.info("shutting down");
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(2000, TimeUnit.MILLISECONDS)) { //optional *
                    log.warn("Executor did not terminate in the specified time."); //optional *
                    List<Runnable> droppedTasks = executor.shutdownNow(); //optional **
                    log.warn("Executor was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed."); //optional **
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }






    }
}
