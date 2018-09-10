package com.balthus.item;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.*;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import static com.lightbend.lagom.javadsl.api.Service.*;
import static com.lightbend.lagom.javadsl.api.transport.Method.*;
import java.util.Optional;
import java.util.UUID;


/**
 * The items service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the ItemsService.
 */
public interface ItemService extends Service {
  String itemsTopic = "items";
  @Override
  default Descriptor descriptor() {
    return named("items").withCalls(
      restCall(GET, "/api/item/:id", this::getItem),
      restCall(POST, "/api/item", this::createItem),
      restCall(PUT, "/api/item/:id", this::updateItem),
      restCall(DELETE, "/api/item/:id", this::deleteItem)

    ).withTopics(
      topic(itemsTopic, this::itemsTopic)
    ).withAutoAcl(true);
  }
  ServiceCall<NotUsed, Optional<Item>> getItem(UUID id);
  ServiceCall<ItemData, Item> createItem();
  ServiceCall<ItemData, Item> updateItem(UUID id);
  ServiceCall<NotUsed, Item> deleteItem(UUID id);
  Topic<ItemEvent> itemsTopic();
}
