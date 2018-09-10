import akka.Done;
import com.balthus.item.ItemStatus;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ItemRepository extends ReadSideProcessor<MenuItemEvent> {
  private final CassandraSession session;
  private final CassandraReadSide reads;

  private PreparedStatement writeItemStatement;
  private PreparedStatement updateItemStatement;

  @Inject
  public ItemRepository(CassandraSession session, CassandraReadSide reads) {
    this.session = session;
    this.reads = reads;
  }

  @Override
  public ReadSideProcessor.ReadSideHandler<MenuItemEvent> buildHandler() {
    CassandraReadSide.ReadSideHandlerBuilder<MenuItemEvent> builder = reads.builder("Item_offset");
    builder.setGlobalPrepare(this::createTable);
    builder.setPrepare(evtTag -> prepareWriteItem());
    builder.setPrepare(evtTag -> prepareUpdateItem());
    builder.setEventHandler(MenuItemEvent.ItemUpdated.class, this::processItemUpdated);
    builder.setEventHandler(MenuItemEvent.ItemCreated.class, this::processItemCreated);
    return builder.build();
  }

  @Override
  public PSequence<AggregateEventTag<MenuItemEvent>> aggregateTags() {
    return MenuItemEvent.TAG.allTags();
  }

  private CompletionStage<Done> createTable() {
    return session.executeCreateTable("CREATE TABLE IF NOT EXISTS Items ( " +
      "itemId UUID, name TEXT, description TEXT, price DOUBLE, status , PRIMARY KEY (itemId))");
  }

  private Done setWriteItem(PreparedStatement ps) {
    this.writeItemStatement = ps;
    return Done.getInstance();
  }

  private CompletionStage<Done> prepareWriteItem() {
      return session
        .prepare("INSERT INTO Items(itemId, name, description, price, status) VALUES (?,?,?,?,?)")
      .thenApply(this::setWriteItem);
  }

  private CompletionStage<List<BoundStatement>> processItemCreated(MenuItemEvent.ItemCreated event) {
    BoundStatement bindWriteItem = writeItemStatement.bind();
    bindWriteItem.setUUID("itemId", event.getItemId());
    bindWriteItem.setString("name", event.getItem().getData().getName());
    bindWriteItem.setString("description", event.getItem().getData().getDescription());
    bindWriteItem.setDouble("price", event.getItem().getData().getPrice());
    bindWriteItem.set("status", event.getItem().getStatus(), ItemStatus.class);
    return CassandraReadSide.completedStatements(Collections.singletonList(bindWriteItem));
  }

  private Done setUpdateItem(PreparedStatement ps){
      this.updateItemStatement = ps;
      return Done.getInstance();
  }
  private CompletionStage<Done> prepareUpdateItem(){
    return session.prepare(
      "UPDATE Items " +
            "SET name = ? " +
            ", description = ? " +
            ", price = ? " +
            ", status = ? " +
            "WHERE itemId = ?")
      .thenApply(this::setUpdateItem);
  }

  private CompletionStage<List<BoundStatement>> processItemUpdated(MenuItemEvent.ItemUpdated event) {
    BoundStatement bindWriteItem = updateItemStatement.bind();
    bindWriteItem.setUUID("itemId", event.getItemId());
    bindWriteItem.setString("name", event.getData().getName());
    bindWriteItem.setString("description", event.getData().getDescription());
    bindWriteItem.setDouble("price", event.getData().getPrice());
    bindWriteItem.set("status", event.getStatus(), ItemStatus.class);
    return CassandraReadSide.completedStatements(Collections.singletonList(bindWriteItem));
  }

}
