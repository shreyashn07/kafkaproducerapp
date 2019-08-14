# kafkaproducerapp
Microservice intended to pull reviews from the play store using publisher API and push the same to a Kafka broker.
This service acts as a Kafka producer, confluent is used in running the Kafka server and zookeeper.
This is one of the few components of a big data pipeline.

Story
There isn't a day which goes by without 100's of reviews for my application edge player in the play store, once what was a dream app turned out to be a seasonal hit.
The mistakes we committed are of a big list, we didn't understand what users want and we went to build features according to our intuition. The results were not so impressive.
When there was a slew of reviews coming every day, it was hard to go through every review, there is so much more to understand from the reviews than just the comments.
So the whole reason behind building this pipeline is to understand those hidden data in realtime and if it scales, it will unlock a lot of answers.
Some questions we are trying to understand
1.Whether the review is positive or negative.
2. Has the user changed the review for our new update, if yes, is it positive or negative?.
3.whether our release is working fine with every device or not.
and so on

This could also help us in stopping the menace of toxic comments in the real-world, I will be building this pipeline as a plug and play service where users can connect their comments stream and filter out all the comments which have abusive language in it.





