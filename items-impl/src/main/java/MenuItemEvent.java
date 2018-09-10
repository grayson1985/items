import com.balthus.item.Item;
import com.balthus.item.ItemData;
import com.balthus.item.ItemStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.UUID;


public interface MenuItemEvent extends Jsonable, AggregateEvent<MenuItemEvent> {
  int NUM_SHARDS = 4;
  AggregateEventShards<MenuItemEvent> TAG = AggregateEventTag.sharded(MenuItemEvent.class, NUM_SHARDS);

  @Override
  default AggregateEventTagger<MenuItemEvent> aggregateTag() {
    return TAG;
  }

  UUID getItemId();


  @Value
  class ItemCreated implements MenuItemEvent {
    Item item;

    @JsonCreator
    ItemCreated(Item item) {
      this.item = item;
    }

    public UUID getItemId() {
      return item.getItemId();
    }
  }

  @Value
  class ItemUpdated implements MenuItemEvent {
    UUID itemId;
    ItemData data;
    ItemStatus status;

    @JsonCreator
    ItemUpdated(UUID itemId, ItemData data, ItemStatus status) {
      this.itemId = itemId;
      this.data = data;
      this.status = status;
    }
  }

  @Value
  class ItemDeleted implements MenuItemEvent {
    UUID itemId;
    ItemStatus itemStatus;

    @JsonCreator
    ItemDeleted(UUID itemId, ItemStatus itemStatus){
      this.itemId = itemId;
      this.itemStatus = itemStatus;
    }
  }


}
