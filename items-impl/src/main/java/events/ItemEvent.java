package events;

import com.balthus.item.Item;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;


public interface ItemEvent extends Jsonable, AggregateEvent<ItemEvent> {

  @Override
  default AggregateEventTagger<ItemEvent> aggregateTag() {
    return ItemEventTag.TAG;
  }

  @Value
  @Builder
  @AllArgsConstructor
  @JsonDeserialize
  final class ItemCreated implements ItemEvent, CompressedJsonable {
    Item item;
    String entityId;
  }

  @Value
  @Builder
  @AllArgsConstructor
  @JsonDeserialize
  final class ItemUpdated implements ItemEvent, CompressedJsonable {
    Item item;
    String entityId;
  }

  @Value
  @Builder
  @AllArgsConstructor
  @JsonDeserialize
  final class ItemDeleted implements ItemEvent, CompressedJsonable {
    Item item;
    String entityId;
  }

}
