import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import com.balthus.item.*;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.*;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class ItemServiceImpl implements ItemService {
  private final PersistentEntityRegistry persistentEntityRegistry;
  private final CassandraSession cassandraSession;

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemServiceImpl.class);


  @Inject
  public ItemServiceImpl(CassandraSession session, PersistentEntityRegistry registry, ReadSide readSide) {
    this.persistentEntityRegistry = registry;
    this.cassandraSession = session;
    persistentEntityRegistry.register(MenuItemEntity.class);
    readSide.register(ItemRepository.class);
  }

  @Override
  public ServiceCall<NotUsed, Optional<Item>> getItem(UUID id) {
    return request -> itemEntityRef(id).ask(ItemCommand.GetItem.INSTANCE);
  }

  @Override
  public ServiceCall<ItemData, Item> createItem() {
    return data -> {
      UUID id = UUID.randomUUID();
      Item item = new Item(id, data);
      ItemCommand.CreateItem command = new ItemCommand.CreateItem(item);
      return itemEntityRef(id).ask(command).thenApply(done -> item);
    };
  }

  @Override
  public ServiceCall<ItemData, Item> updateItem(UUID id) {
    return itemData -> {
      ItemCommand.UpdateItem command = new ItemCommand.UpdateItem(id, itemData);
      return itemEntityRef(id).ask(command);
    };
  }

  @Override
  public Topic<ItemEvent> itemsTopic() {
    return TopicProducer.taggedStreamWithOffset(MenuItemEvent.TAG.allTags(), (tag, offset) ->
      persistentEntityRegistry.eventStream(tag, offset).mapAsync(1, eventAndOffset ->
        convertMenuItem(eventAndOffset.first()).thenApply(event ->
          Pair.create(event, eventAndOffset.second())
        )
      ));
  }

  @Override
  public ServiceCall<NotUsed, Item> deleteItem(UUID id) {
    ItemCommand.DeleteItem deleteItem = new ItemCommand.DeleteItem(id, ItemStatus.INACTIVE);
    return request -> itemEntityRef(id).ask(deleteItem);
  }

  private CompletionStage<ItemEvent> convertMenuItem(MenuItemEvent event) {
    if (event instanceof MenuItemEvent.ItemUpdated) {
      MenuItemEvent.ItemUpdated e = (MenuItemEvent.ItemUpdated) event;
      return CompletableFuture.completedFuture(
        new ItemEvent.ItemUpdated(
          e.getItemId(),
          e.getData(),
          e.getStatus())
      );
    } else if (event instanceof MenuItemEvent.ItemCreated) {
      MenuItemEvent.ItemCreated e = (MenuItemEvent.ItemCreated) event;
      return CompletableFuture.completedFuture(
        new ItemEvent.ItemCreated(e.getItemId(), e.getItem().getData(), e.getItem().getStatus())
      );
    } else if (event instanceof MenuItemEvent.ItemDeleted) {
      MenuItemEvent.ItemDeleted e = (MenuItemEvent.ItemDeleted) event;
      return CompletableFuture.completedFuture(
        new ItemEvent.ItemDeactivated(e.getItemId(), e.getItemStatus())
      );
    } else {
      throw new IllegalArgumentException("ItemServiceImpl: No matching MenuItemEvent found.");
    }
  }

  private PersistentEntityRef<ItemCommand> itemEntityRef(UUID id) {
    return persistentEntityRegistry.refFor(MenuItemEntity.class, id.toString());
  }
}
