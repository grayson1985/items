import akka.Done;
import akka.NotUsed;
import akka.persistence.cassandra.session.javadsl.CassandraSession;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;

import javax.inject.Inject;
import com.balthus.item.Item;
import com.balthus.item.ItemService;
import commands.ItemCommand;
import events.ItemEventProcessor;

import java.util.Optional;
import java.util.concurrent.CompletionStage;


public class ItemServiceImpl implements ItemService {
  private final PersistentEntityRegistry persistentEntityRegistry;
  private final CassandraSession cassandraSession;

  @Inject
  public ItemServiceImpl(final PersistentEntityRegistry registry, ReadSide readSide, CassandraSession session) {
    this.persistentEntityRegistry = registry;
    this.cassandraSession = session;


    persistentEntityRegistry.register(ItemEntity.class);
    readSide.register(ItemEventProcessor.class);
  }

  @Override
  public ServiceCall<NotUsed, Optional<Item>> getItem(String id) {
    return request -> {
      String stmt = "SELECT * FROM Items WHERE id=?";
      CompletionStage<Optional<Item>> itemFut =
        cassandraSession.selectAll(stmt, id).thenApply(
          rows -> rows.stream().map(row ->
            Item.builder()
              .id(row.getString("id"))
              .name(row.getString("name"))
              .description(row.getString("description"))
              .price(row.getString("price"))
              .build()
          ).findFirst()
        );
      return itemFut;
    };
  }

  @Override
  public ServiceCall<Item, Done> createItem() {
    return item -> {
      PersistentEntityRef<ItemCommand> ref = itemEntityRef(item);
      return ref.ask(ItemCommand.CreateItem.builder().item(item).build());
    };
  }

  @Override
  public ServiceCall<Item, Done> updateItem(String id) {
    return item -> {
      PersistentEntityRef<ItemCommand> ref = itemEntityRef(item);
      return ref.ask(ItemCommand.UpdateItem.builder().item(item).build());
    };
  }

  @Override
  public ServiceCall<NotUsed, Done> deleteItem(String id) {
    return request -> {
      Item item = Item.builder().id(id).build();
      PersistentEntityRef<ItemCommand> ref = itemEntityRef(item);
      return ref.ask(ItemCommand.DeleteItem.builder().item(item).build());
    };
  }

  private PersistentEntityRef<ItemCommand> itemEntityRef(Item item) {
    return persistentEntityRegistry.refFor(ItemEntity.class, item.getId());
  }
}
