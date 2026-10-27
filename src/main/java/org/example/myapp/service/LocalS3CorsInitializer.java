package org.example.myapp.service;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CORSConfiguration;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.model.PutBucketCorsRequest;

import java.util.List;

@ApplicationScoped
@IfBuildProfile("dev")
public class LocalS3CorsInitializer {

    @ConfigProperty(name = "bucket.name")
    String bucketName;

    @Inject
    S3Client s3;

    void configureCors(@Observes StartupEvent event) {
        CORSRule localFrontendRule = CORSRule.builder()
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")
                .allowedMethods("PUT", "GET", "HEAD")
                .allowedHeaders("*")
                .exposeHeaders("ETag")
                .maxAgeSeconds(3600)
                .build();

        s3.putBucketCors(PutBucketCorsRequest.builder()
                .bucket(bucketName)
                .corsConfiguration(CORSConfiguration.builder()
                        .corsRules(List.of(localFrontendRule))
                        .build())
                .build());
    }
}
