package commands;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import java.util.Optional;
import com.balthus.item.Item;

public interface ItemCommand extends Jsonable {

  @Value
  @Builder
  @JsonDeserialize
  @AllArgsConstructor
  final class CreateItem implements ItemCommand, PersistentEntity.ReplyType<Done> {
    Item item;
  }

  @Value
  @Builder
  @JsonDeserialize
  @AllArgsConstructor
  final class UpdateItem implements ItemCommand, PersistentEntity.ReplyType<Done> {
    Item item;
  }

  @Value
  @Builder
  @JsonDeserialize
  @AllArgsConstructor
  final class DeleteItem implements ItemCommand, PersistentEntity.ReplyType<Done> {
    Item item;
  }

  @JsonDeserialize
  final class GetCurrentStateItem implements ItemCommand, PersistentEntity.ReplyType<Optional<Item>> {}
}
