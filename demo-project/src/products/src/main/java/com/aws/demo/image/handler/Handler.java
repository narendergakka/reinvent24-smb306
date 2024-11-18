package com.aws.demo.image.handler;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;


import com.aws.demo.db.ProductRepository;
import com.aws.demo.util.JsonExtractor;

import java.io.IOException;
import java.util.Base64;

public class Handler implements RequestHandler<S3Event, String> {

    private final Region region = Region.US_EAST_1;

    private final S3Client s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();


    private final BedrockRuntimeClient bedrockClient = BedrockRuntimeClient.builder()
            .region(region)
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .build();

    private ProductRepository productRepository = new ProductRepository();

    
    public String handleRequest(S3Event event, Context context) {
        String jsonResponse = null;
        
        try {
           
            String bucket = event.getRecords().get(0).getS3().getBucket().getName();
            String key = event.getRecords().get(0).getS3().getObject().getKey();

            // Download the image from S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            byte[] imageBytes = s3Client.getObject(getObjectRequest).readAllBytes();

            // Encode the image to Base64
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Prepare the request payload for Claude 3 Sonnet
            String payload = String.format(
                    "{" +
                            "  \"anthropic_version\": \"bedrock-2023-05-31\"," +
                            "  \"max_tokens\": 1000," +
                            "  \"messages\": [" +
                            "    {" +
                            "      \"role\": \"user\"," +
                            "      \"content\": [" +
                            "        {" +
                            "          \"type\": \"image\"," +
                            "          \"source\": {" +
                            "            \"type\": \"base64\"," +
                            "            \"media_type\": \"image/jpeg\"," +
                            "            \"data\": \"%s\"" +
                            "          }" +
                            "        }," +
                            "        {" +
                            "          \"type\": \"text\"," +
//                            "          \"text\": \"Extract all the possible attributes in key value pair format including the product name, brand, category, description and provide results in json format. Provide description in 50 words.\"" +
                            "          \"text\": \"SystemPrompt: you are a description generator for amazon.com retail website,keep the descriptions without any opinions, so only fact based descriptions. UserPrompt: Extract the attributes in key value pair format including the product_name, brand, category, description (in 50 words) and remaining attributes in json format in feature section. Provide results in json format.\"" +
                            "        }" +
                            "      ]" +
                            "    }" +
                            "  ]" +
                            "}", base64Image);


            // Model Request
            InvokeModelRequest invokeModelRequest = InvokeModelRequest.builder()
                    .modelId("anthropic.claude-3-sonnet-20240229-v1:0")
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(payload))
                    .build();

            // Invoke model
            InvokeModelResponse response = bedrockClient.invokeModel(invokeModelRequest);

             // Model response
            jsonResponse = response.body().asUtf8String();

            // Log the response
            context.getLogger().log("Bedrock Response : " + jsonResponse);
            
            // Store product specs in DB
            productRepository.storeProduct(key, new JsonExtractor().extractPayload(jsonResponse));

        } catch (Exception e) {
            System.err.println("Error processing image : " + e);
        }

         return jsonResponse;

    }
}

