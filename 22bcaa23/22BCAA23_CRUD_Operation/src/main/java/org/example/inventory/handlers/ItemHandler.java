package org.example.inventory.handlers;
import org.example.inventory.models.Item;
import org.example.inventory.util.MongoDBUtil;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.mongo.MongoClient;

public class ItemHandler {

    private final MongoClient mongoClient;

    public ItemHandler(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void handleCreate(RoutingContext ctx) {
        JsonObject newItemJson = ctx.getBodyAsJson();
        Item newItem = Json.decodeValue(newItemJson.encode(), Item.class);

        mongoClient.save("items", JsonObject.mapFrom(newItem), ar -> {
            if (ar.succeeded()) {
                ctx.response()
                        .setStatusCode(201) // Created
                        .end();
            } else {
                ctx.response()
                        .setStatusCode(500) // Internal Server Error
                        .end(ar.cause().getMessage());
            }
        });
    }

    public void handleRead(RoutingContext ctx) {
        mongoClient.find("items", new JsonObject(), ar -> {
            if (ar.succeeded()) {
                ctx.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(200) // OK
                        .end(Json.encodePrettily(ar.result()));
            } else {
                ctx.response()
                        .setStatusCode(500) // Internal Server Error
                        .end(ar.cause().getMessage());
            }
        });
    }

    public void handleUpdate(RoutingContext ctx) {
        String itemId = ctx.pathParam("id");
        JsonObject updatedItemJson = ctx.getBodyAsJson();
        Item updatedItem = Json.decodeValue(updatedItemJson.encode(), Item.class);

        JsonObject query = new JsonObject().put("_id", itemId);
        JsonObject update = new JsonObject().put("$set", JsonObject.mapFrom(updatedItem));

        mongoClient.updateCollection("items", query, update, ar -> {
            if (ar.succeeded()) {
                ctx.response()
                        .setStatusCode(204) // No Content
                        .end();
            } else {
                ctx.response()
                        .setStatusCode(500) // Internal Server Error
                        .end(ar.cause().getMessage());
            }
        });
    }

    public void handleDelete(RoutingContext ctx) {
        String itemId = ctx.pathParam("id");

        JsonObject query = new JsonObject().put("_id", itemId);

        mongoClient.removeDocument("items", query, ar -> {
            if (ar.succeeded()) {
                ctx.response()
                        .setStatusCode(204) // No Content
                        .end();
            } else {
                ctx.response()
                        .setStatusCode(500) // Internal Server Error
                        .end(ar.cause().getMessage());
            }
        });
    }
}
