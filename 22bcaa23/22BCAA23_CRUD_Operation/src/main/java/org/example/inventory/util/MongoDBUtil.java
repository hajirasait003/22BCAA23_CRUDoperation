package org.example.inventory.util;

import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.core.json.JsonObject;

public class MongoDBUtil {

    public static MongoClient initializeMongoClient(Vertx vertx) {
        return MongoClient.createShared(vertx, new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "stationery_db"));
    }
}

