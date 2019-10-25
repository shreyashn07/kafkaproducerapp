package com.steplabs.kafkaproducer.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.steplabs.kafkaproducer.model.ReviewApiResponse;
import com.steplabs.kafkaproducer.utils.PlayStorePublisherUtils;
import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.steplabs.kafkaproducer.avro.Device;
import com.steplabs.kafkaproducer.avro.Review;
import com.steplabs.kafkaproducer.avro.User;



import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayStoreRestClient {

    private String packageName;
    private String applicationName;
    private int pageSize;
    private String nextPageToken;
    private String serviceAccountEmail;
    private String token;


    public PlayStoreRestClient(String packageName, String applicationName, String serviceAccountEmail){
        this.packageName=packageName;
        this.pageSize=1;
        this.applicationName=applicationName;
        this.nextPageToken="1";
        this.serviceAccountEmail=serviceAccountEmail;
        this.token="1";


    }

    private void init() throws IOException, GeneralSecurityException, HttpException {
        //nextPageToken=reviewApi(packageName,pageSize,applicationName);
        token= PlayStorePublisherUtils.init(applicationName,serviceAccountEmail);//getAccessToken();
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("access_token",token);
        //parameters.put("token","1");
        parameters.put("maxResults","100");
        try{
            HttpResponse<JsonNode> jsonResponse= Unirest.get("https://www.googleapis.com/androidpublisher/v3/applications/com.fourhorsemen.musicvault/reviews")
                    .queryString(parameters)
                    .asJson();
            if(jsonResponse.getStatus()==200) {
                JSONObject body = jsonResponse.getBody().getObject();
                nextPageToken = body.getJSONObject("tokenPagination").getString("nextPageToken");
            }
            else{
                throw new HttpException("Playstore API gone nuts");}

        }
        catch (UnirestException ex)
        {
            System.out.println(ex);
        }

    }


    public List<Review> getNextReviews() throws HttpException,IOException,GeneralSecurityException{
        if(nextPageToken=="1")init();
        List<Review> result=new ArrayList<>();
        while(nextPageToken!= null) {

        result.addAll(reviewApi(packageName,pageSize,applicationName).getReviewList());
        }
        return result;


    }
    public ReviewApiResponse reviewApi(String packageName, int pageSize, String applicationName) throws HttpException{

        //String url="https://www.googleapis.com/androidpublisher/v3/applications/com.fourhorsemen.musicvault/reviews";
        HttpResponse<JsonNode> jsonResponse = null;
        Map<String,Object> parameters = new HashMap<>();
        parameters.put("access_token",token);
        parameters.put("token",nextPageToken);
        parameters.put("maxResults","100");
        try{
            jsonResponse= Unirest.get("https://www.googleapis.com/androidpublisher/v3/applications/com.fourhorsemen.musicvault/reviews")
                    .queryString(parameters)
                    .asJson();
            JSONObject body=jsonResponse.getBody().getObject();


        }
        catch (UnirestException ex) {
            throw new HttpException(ex.getMessage()); }
        if(jsonResponse.getStatus()==200){
            JSONObject body = jsonResponse.getBody().getObject();
            if(!(body.isNull("tokenPagination")))
            nextPageToken=body.getJSONObject("tokenPagination").getString("nextPageToken");
            else
                nextPageToken=null;
            System.out.println(body.getJSONArray("reviews").length());
            List<Review> reviews= this.parseResults(body.getJSONArray("reviews"));

            ReviewApiResponse reviewApiResponse= new ReviewApiResponse(reviews);
            return reviewApiResponse;



        }

        throw new HttpException("Playstore API Unavailable");

    }


    public List<Review> parseResults(JSONArray resultJsonArray){
        List<Review> results = new ArrayList<>();
        for(int i =0;i<resultJsonArray.length();i++)
        {
            JSONObject reviewJson=resultJsonArray.getJSONObject(i);
            Review review=jsonToPreview(reviewJson);
            results.add(review);



        }
        return results;
    }

    public Review jsonToPreview(JSONObject reviewJson){
        Review.Builder reviewBuilder=Review.newBuilder();
        reviewBuilder.setReviewId(reviewJson.getString("reviewId"));
        reviewBuilder.setComments(reviewJson.getJSONArray("comments").getJSONObject(0).getJSONObject("userComment").getString("text"));
        reviewBuilder.setStarRating(reviewJson.getJSONArray("comments").getJSONObject(0).getJSONObject("userComment").getLong("starRating"));
        reviewBuilder.setModified(reviewJson.getJSONArray("comments").getJSONObject(0).getJSONObject("userComment").getJSONObject("lastModified").getLong("seconds"));
        reviewBuilder.setUser(jsonToPuser(reviewJson));
        reviewBuilder.setDevice(jsonToDevice(reviewJson));
        return reviewBuilder.build();


    }

    public com.steplabs.kafkaproducer.avro.User jsonToPuser(JSONObject reviewJson){
        com.steplabs.kafkaproducer.avro.User.Builder puserBuilder= com.steplabs.kafkaproducer.avro.User.newBuilder();
        puserBuilder.setName(reviewJson.getString("authorName"));
        puserBuilder.setReviewerLanguage(reviewJson.getJSONArray("comments").getJSONObject(0).getJSONObject("userComment").getString("reviewerLanguage"));
        return puserBuilder.build();



    }

    public Device jsonToDevice(JSONObject reviewJson){
        Device.Builder deviceBuilder=Device.newBuilder();
        deviceBuilder.setProductName(reviewJson.getJSONArray("comments").getJSONObject(0).getJSONObject("userComment").getJSONObject("deviceMetadata").getString("productName"));
        deviceBuilder.setManufacturer(reviewJson.getJSONArray("comments").getJSONObject(0).getJSONObject("userComment").getJSONObject("deviceMetadata").isNull("manufacturer")?"some":reviewJson.getJSONArray("comments").getJSONObject(0).getJSONObject("userComment").getJSONObject("deviceMetadata").getString("manufacturer"));
        deviceBuilder.setRamMb(reviewJson.getJSONArray("comments").getJSONObject(0).getJSONObject("userComment").getJSONObject("deviceMetadata").isNull("ramMb")?1000:reviewJson.getJSONArray("comments").getJSONObject(0).getJSONObject("userComment").getJSONObject("deviceMetadata").getLong("ramMb"));
        return deviceBuilder.build();



    }


    public void close() {
        try {
            Unirest.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
