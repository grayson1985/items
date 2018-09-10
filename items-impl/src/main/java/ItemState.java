import com.balthus.item.ItemData;
import com.balthus.item.ItemStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.balthus.item.Item;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

import java.util.Optional;
import java.util.function.Function;

@Value
@JsonDeserialize
public class ItemState implements CompressedJsonable {
  Optional<Item> item;

  @JsonCreator
  private ItemState(Optional<Item> item) {
    this.item = item;
  }

  public static ItemState empty() {
    return new ItemState(Optional.empty());
  }

  public static ItemState create(Item item) {
    return new ItemState(Optional.of(item));
  }

  public ItemState deactivate() {
    return update(i -> i.deactivate());
  }

  public ItemState updateData(ItemData data) {
    return update(i -> i.updateItemData(data));
  }

  public ItemStatus getStatus() {
    return item.map(Item::getStatus).orElse(ItemStatus.NOT_CREATED);
  }


  private ItemState update(Function<Item, Item> updateFunct) {
    if (item.isPresent())
      return new ItemState(item.map(updateFunct));
    else
      throw new IllegalStateException("Attempting to update the state of a non existent item." + item.toString());
  }
}
