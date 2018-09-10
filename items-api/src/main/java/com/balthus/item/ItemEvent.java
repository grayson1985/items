package com.balthus.item;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Value;
import java.util.UUID;

/**
 * An Item Event.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
  @JsonSubTypes.Type(ItemEvent.ItemDeactivated.class),
  @JsonSubTypes.Type(ItemEvent.ItemCreated.class),
  @JsonSubTypes.Type(ItemEvent.ItemUpdated.class)
})
public interface ItemEvent {
  UUID getItemId();

  @JsonTypeName("item-created")
  @Value
  final class ItemCreated implements ItemEvent {
    UUID itemId;
    ItemData itemData;
    ItemStatus status;

    public ItemCreated(UUID itemId, ItemData itemData, ItemStatus status){
      this.itemId = itemId;
      this.itemData = itemData;
      this.status = status;
    }
  }

  @JsonTypeName("item-updated")
  @Value
  final class ItemUpdated implements ItemEvent {
    UUID itemId;
    ItemData itemData;
    ItemStatus status;

    public ItemUpdated(UUID itemId, ItemData itemData, ItemStatus status){
      this.itemId = itemId;
      this.itemData = itemData;
      this.status = status;
    }
  }

  @JsonTypeName("item-deactivated")
  @Value
  final class ItemDeactivated implements ItemEvent {
    UUID itemId;
    ItemStatus status;

    public ItemDeactivated(UUID itemId, ItemStatus status){
      this.itemId = itemId;
      this.status = status;
    }
  }



}
