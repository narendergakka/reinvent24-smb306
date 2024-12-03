package com.aws.demo.product.handler;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.aws.demo.product.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private String tableName = null;
    private DynamoDbClient ddb = null;

    public Handler() {
        Region region = Region.US_EAST_1;

        String regionStr = System.getenv("AWS_REGION");
        if (regionStr != null && !regionStr.trim().isEmpty()) {
            region = Region.of(regionStr);
        }
        System.out.println("Selected Region : " + region.toString());
        
        ddb = DynamoDbClient.builder()
                .region(region)
                .build();

        tableName = System.getenv("PRODUCTSPECIFICATIONS_TABLE_NAME");
    }

    // handle request implementation
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        String items = getAllItemsFromDynamoDB();
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setBody(items);
        responseEvent.setStatusCode(200);
        return responseEvent;
    }


    // get all items from DynamodDB table
    public String getAllItemsFromDynamoDB() {
        Gson gson = new Gson();
        ScanResponse response = null;
        List<Product> products = new ArrayList<>();

        try {
            // Create scan request
            ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

            // Query DynamoDB table
            response = ddb.scan(scanRequest);
        
            // Iterate over response items and Build product List 
            for (Map<String, AttributeValue> item : response.items()) {
                Product product = new Product();
                product.setProductId(item.get("product_id").s());
                product.setCategory(item.get("category").s());    
                product.setProductName(item.get("product_name").s());
                product.setProductDescription(item.get("description").s());    
                product.setBrand(item.get("brand").s());  
                product.setImageName(item.get("image_name").s());
                product.setFeatures(item.get("features").s());  
                product.setCreationTime(item.get("creation_time").s()); 

                products.add(product);
            }

        } catch (DynamoDbException e) {
            System.err.println("Error scanning the table:" + e);
        } 


       // return product list as JSON in the response
        return gson.toJson(products);
    }

}
