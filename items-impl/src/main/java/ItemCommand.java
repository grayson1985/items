import akka.Done;
import com.balthus.item.ItemData;
import com.balthus.item.ItemStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Builder;
import lombok.Value;
import java.util.Optional;
import java.util.UUID;

import com.balthus.item.Item;

public interface ItemCommand extends Jsonable {

  @Value
  @JsonDeserialize
  final class CreateItem implements ItemCommand, PersistentEntity.ReplyType<Done> {
    Item item;

    @JsonCreator
    CreateItem(Item item){
      this.item = item;
    }

  }

  @Value
  @JsonDeserialize
  final class UpdateItem implements ItemCommand, PersistentEntity.ReplyType<Item> {
    UUID itemId;
    ItemData itemData;


    @JsonCreator
    UpdateItem(UUID itemId, ItemData itemData){
      this.itemId = itemId;
      this.itemData = itemData;
    }

  }

  @Value
  @JsonDeserialize
  final class DeleteItem implements ItemCommand, PersistentEntity.ReplyType<Item> {
    UUID itemId;
    ItemStatus status;

    @JsonCreator
    DeleteItem(UUID itemId, ItemStatus itemStatus){
      this.itemId = itemId;
      this.status = itemStatus;
    }

  }

  @JsonDeserialize
  enum GetItem implements ItemCommand, PersistentEntity.ReplyType<Optional<Item>> {
    INSTANCE
  }
}
