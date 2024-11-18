package com.aws.demo.db;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import com.aws.demo.product.model.Product;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class ProductRepository {

    private final Region region = Region.US_EAST_1;
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(region)
                .build();
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    private String tableName = null;
    
    public ProductRepository() {
        tableName = System.getenv("PRODUCTSPECIFICATIONS_TABLE_NAME");
    }

    public void storeProduct(String imageName, String productDetails) throws Exception {

        JsonObject jsonObject = JsonParser.parseString(productDetails).getAsJsonObject();

        String productId = generateProductId();
        String productCategory = jsonObject.get("category").getAsString();
        String productName = jsonObject.get("product_name").getAsString();  
        String productDescription = jsonObject.get("description").getAsString();    
        String brand = jsonObject.get("brand").getAsString(); 
        JsonObject features = jsonObject.get("features").getAsJsonObject();    
        String currentTimestamp = ZonedDateTime.now(ZoneId.of("America/New_York")).format(FORMATTER);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("product_id", AttributeValue.builder().s(productId).build());
        item.put("category", AttributeValue.builder().s(productCategory).build());
        item.put("product_name", AttributeValue.builder().s(productName).build());
        item.put("description", AttributeValue.builder().s(productDescription).build());
        item.put("brand", AttributeValue.builder().s(brand).build());
        item.put("features", AttributeValue.builder().s(features.toString()).build());
        item.put("image_name", AttributeValue.builder().s(imageName).build());
        item.put("creation_time", AttributeValue.builder().s(currentTimestamp).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(putItemRequest);
    }


    

    private String generateProductId() {
            // Generate a random UUID
            UUID uuid = UUID.randomUUID();
            return uuid.toString();
    }
       
    }

