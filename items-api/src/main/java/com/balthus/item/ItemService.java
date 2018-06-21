package com.balthus.item;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static com.lightbend.lagom.javadsl.api.transport.Method.*;

/**
 * The items service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the ItemsService.
 */
public interface ItemService extends Service {
  ServiceCall<NotUsed, Optional<Item>> getItem(String id);

  ServiceCall<Item, Done> createItem();

  ServiceCall<Item, Done> updateItem(String id);

  ServiceCall<NotUsed, Done> deleteItem(String id);

  @Override
  default Descriptor descriptor() {
    return named("items").withCalls(
      restCall(GET, "/api/item/:id", this::getItem),
      restCall(POST, "/api/item", this::createItem),
      restCall(PUT, "/api/item/:id", this::updateItem),
      restCall(DELETE, "/api/item/:id", this::deleteItem)
    ).withAutoAcl(true);
  }
}
