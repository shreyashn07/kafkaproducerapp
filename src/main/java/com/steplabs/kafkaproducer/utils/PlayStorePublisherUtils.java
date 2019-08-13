package com.steplabs.kafkaproducer.utils;

import avro.shaded.com.google.common.base.Preconditions;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import org.apache.avro.reflect.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class PlayStorePublisherUtils {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Log log = LogFactory.getLog(PlayStorePublisherUtils.class);

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;
    private static final String SRC_RESOURCES_KEY_P12 = "/Users/shreyashn/Documents/medium-blog-kafka-udemy-master/udemy-reviews-producer/src/main/resources/key.p12";

    private static Credential authorizeWithServiceAccount(String serviceAccountEmail)
            throws GeneralSecurityException, IOException {
        log.info(String.format("Authorizing using Service Account: %s", serviceAccountEmail));

        // Build service account credential.
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountScopes(
                        Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER))
                .setServiceAccountPrivateKeyFromP12File(new File(SRC_RESOURCES_KEY_P12))
                .build();
        return credential;
    }



    public static String init(String applicationName,
                              @Nullable String serviceAccountEmail) throws IOException, GeneralSecurityException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationName),
                "applicationName cannot be null or empty!");

        // Authorization.
        newTrustedTransport();
        Credential credential;

        credential = authorizeWithServiceAccount(serviceAccountEmail);
        credential.refreshToken();
        return credential.getAccessToken();




    }

    private static void newTrustedTransport() throws GeneralSecurityException,
            IOException {
        if (null == HTTP_TRANSPORT) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }
    }
}
