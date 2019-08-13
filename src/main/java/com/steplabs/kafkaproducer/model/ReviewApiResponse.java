package com.steplabs.kafkaproducer.model;

import com.steplabs.kafkaproducer.avro.Review;

import java.util.List;

public class ReviewApiResponse {

    private Integer count;
    private String next;
    private String previous;
    private List<Review> reviewList;

    public ReviewApiResponse(List<Review> reviewList) {

        this.reviewList = reviewList;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public List<Review> getReviewList() {
        return reviewList;
    }

    public void setReviewList(List<Review> reviewList) {
        this.reviewList = reviewList;
    }
}
