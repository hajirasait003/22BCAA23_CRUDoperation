package org.example.inventory;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.mongo.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

    private MongoClient mongoClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start() {
        mongoClient = MongoClient.createShared(vertx, new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "stationery_db"));

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.post("/items").handler(this::addItem);
        router.get("/items").handler(this::getItems);
        router.post("/items/:id/update").handler(this::updateItem);
        router.post("/items/:id/delete").handler(this::deleteItem);

        vertx.createHttpServer().requestHandler(router).listen(8080, http -> {
            if (http.succeeded()) {
                LOGGER.info("Server started on port 8080");
            } else {
                LOGGER.error("Failed to start server: ", http.cause());
            }
        });
    }

    private void addItem(RoutingContext context) {
        JsonObject newItem = context.getBodyAsJson();
        LOGGER.info("Adding item: {}", newItem.encodePrettily());
        mongoClient.save("items", newItem, res -> {
            if (res.succeeded()) {
                context.response()
                        .setStatusCode(201)
                        .end("Item added");
            } else {
                LOGGER.error("Failed to add item: ", res.cause());
                context.response()
                        .setStatusCode(500)
                        .end("Failed to add item");
            }
        });
    }

    private void getItems(RoutingContext context) {
        mongoClient.find("items", new JsonObject(), res -> {
            if (res.succeeded()) {
                context.response()
                        .putHeader("content-type", "application/json")
                        .end(res.result().toString());
            } else {
                LOGGER.error("Failed to retrieve items: ", res.cause());
                context.response()
                        .setStatusCode(500)
                        .end("Failed to retrieve items");
            }
        });
    }

    private void updateItem(RoutingContext context) {
        String itemId = context.pathParam("id");
        JsonObject updatedItem = context.getBodyAsJson();
        LOGGER.info("Updating item with id {}: {}", itemId, updatedItem.encodePrettily());
        mongoClient.updateCollection("items", new JsonObject().put("itemId", itemId), new JsonObject().put("$set", updatedItem), res -> {
            if (res.succeeded()) {
                context.response()
                        .setStatusCode(200)
                        .end("Item updated");
            } else {
                LOGGER.error("Failed to update item: ", res.cause());
                context.response()
                        .setStatusCode(500)
                        .end("Failed to update item");
            }
        });
    }

    private void deleteItem(RoutingContext context) {
        String itemId = context.pathParam("id");
        LOGGER.info("Deleting item with id {}", itemId);
        mongoClient.removeDocument("items", new JsonObject().put("itemId", itemId), res -> {
            if (res.succeeded()) {
                context.response()
                        .setStatusCode(200)
                        .end("Item deleted");
            } else {
                LOGGER.error("Failed to delete item: ", res.cause());
                context.response()
                        .setStatusCode(500)
                        .end("Failed to delete item");
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }
}

