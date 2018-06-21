package events;

import akka.Done;
import akka.persistence.cassandra.session.javadsl.CassandraSession;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import org.pcollections.PSequence;
import events.ItemEvent.*;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ItemEventProcessor extends ReadSideProcessor<ItemEvent> {
  private final CassandraSession cassandraSession;
  private final CassandraReadSide cassandraReadSide;

  private PreparedStatement writeItem;
  private PreparedStatement deleteItem;

  @Inject
  public ItemEventProcessor(CassandraSession cassandraSession, CassandraReadSide cassandraReadSide) {
    this.cassandraSession = cassandraSession;
    this.cassandraReadSide = cassandraReadSide;
  }

  @Override
  public ReadSideProcessor.ReadSideHandler<ItemEvent> buildHandler() {
    CassandraReadSide.ReadSideHandlerBuilder<ItemEvent> builder = cassandraReadSide.builder("Item_offset");
    builder.setGlobalPrepare(this::createTable);
    builder.setPrepare(evtTag -> prepareWriteItem()
      .thenCombine(prepareDeleteItem(), (d1, d2) -> Done.getInstance())
    );
    builder.setEventHandler(ItemCreated.class, this::processItemCreated);
    builder.setEventHandler(ItemUpdated.class, this::processItemUpdated);
    builder.setEventHandler(ItemDeleted.class, this::processItemDeleted);
    return builder.build();
  }

  @Override
  public PSequence<AggregateEventTag<ItemEvent>> aggregateTags() {
    return ItemEventTag.TAG.allTags();
  }

  private CompletionStage<Done> createTable() {
    return cassandraSession.executeCreateTable("CREATE TABLE IF NOT EXISTS Items ( " +
      "id TEXT, name TEXT, description TEXT, price TEXT, PRIMARY KEY (id))");
  }

  private Done setWriteItem(PreparedStatement ps) {
    this.writeItem = ps;
    return Done.getInstance();
  }

  private CompletionStage<Done> prepareWriteItem() {
    return cassandraSession.prepare("INSERT INTO Items(id, name, description, price) VALUES (?,?,?,?)")
      .thenApply(this::setWriteItem);
  }

  private Done setDeleteItem(PreparedStatement ps) {
    this.deleteItem = ps;
    return Done.getInstance();
  }

  private CompletionStage<Done> prepareDeleteItem() {
    return cassandraSession.prepare("DELETE FROM Items WHERE id=?")
      .thenApply(this::setDeleteItem);
  }

  private CompletionStage<List<BoundStatement>> processItemCreated(ItemCreated event) {
    BoundStatement bindWriteItem = writeItem.bind();
    bindWriteItem.setString("id", event.getItem().getId());
    bindWriteItem.setString("name", event.getItem().getName());
    bindWriteItem.setString("description", event.getItem().getDescription());
    bindWriteItem.setString("price", event.getItem().getPrice());
    return CassandraReadSide.completedStatements(Arrays.asList(bindWriteItem));
  }

  private CompletionStage<List<BoundStatement>> processItemUpdated(ItemUpdated event) {
    BoundStatement bindWriteItem = writeItem.bind();
    bindWriteItem.setString("id", event.getItem().getId());
    bindWriteItem.setString("name", event.getItem().getName());
    bindWriteItem.setString("description", event.getItem().getDescription());
    bindWriteItem.setString("price", event.getItem().getPrice());
    return CassandraReadSide.completedStatements(Arrays.asList(bindWriteItem));
  }

  private CompletionStage<List<BoundStatement>> processItemDeleted(ItemDeleted event) {
    BoundStatement bindDeleteItem = deleteItem.bind();
    bindDeleteItem.setString("id", event.getItem().getId());
    return CassandraReadSide.completedStatements(Arrays.asList(bindDeleteItem));
  }

}
