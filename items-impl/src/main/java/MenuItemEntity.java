import akka.Done;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import com.balthus.item.Item;
import com.balthus.item.ItemData;
import com.balthus.item.ItemStatus;


public class MenuItemEntity extends PersistentEntity<ItemCommand, MenuItemEvent, ItemState> {

  @Override
  public Behavior initialBehavior(Optional<ItemState> itemStateSnapshot) {
    ItemStatus status = itemStateSnapshot.map(ItemState::getStatus).orElse(ItemStatus.NOT_CREATED);

    switch (status) {
      case NOT_CREATED:
        return empty();
      case ACTIVE:
        return active(itemStateSnapshot.get());
      case INACTIVE:
        return inactive(itemStateSnapshot.get());
      default:
        throw new IllegalArgumentException("Unknown Item Status: " + status);
    }
  }

  //Current state is that the item has not been created yet.
  private Behavior empty() {
    BehaviorBuilder builder = newBehaviorBuilder(ItemState.empty());
    //set command handler for a GetItem command.
    builder.setReadOnlyCommandHandler(ItemCommand.GetItem.class, this::getItem);

    //set command handler for a create item command.
    builder.setCommandHandler(ItemCommand.CreateItem.class, (cmd, ctx) ->
      ctx.thenPersist(new MenuItemEvent.ItemCreated(cmd.getItem()), evt -> ctx.reply(Done.getInstance()))
    );

    //set event handler for an item created event.
    builder.setEventHandlerChangingBehavior(MenuItemEvent.ItemCreated.class, evt -> active(ItemState.create(evt.getItem())));
    builder.setReadOnlyCommandHandler(ItemCommand.UpdateItem.class, (cmd, ctx) ->
      ctx.commandFailed(new NotFound(entityId()))
    );
    return builder.build();
  }

  //Current state is that the item has been activated.
  private Behavior active(ItemState state) {
    BehaviorBuilder builder = newBehaviorBuilder(state);
    //set command handler for a GetItem command.
    builder.setReadOnlyCommandHandler(ItemCommand.GetItem.class, this::getItem);

    builder.setReadOnlyCommandHandler(ItemCommand.CreateItem.class, (cmd, ctx) ->
      ctx.invalidCommand("item has already been created.")
    );

    //Set command handler for Update Item Command.
    builder.setCommandHandler(ItemCommand.UpdateItem.class, (cmd, ctx) -> {
      Item item = state().getItem().get();
      return ctx.thenPersist(
        new MenuItemEvent.ItemUpdated(item.getItemId(), cmd.getItemData(), item.getStatus()),
        evt -> ctx.reply(item.updateItemData(cmd.getItemData()))
      );
    });
    //Set event handler for Item Updated Event.
    builder.setEventHandler(MenuItemEvent.ItemUpdated.class, evt -> state().updateData(evt.getData()));
    //Set command handler for Delete Item Command.
    builder.setCommandHandler(ItemCommand.DeleteItem.class, (cmd, ctx) -> {
      Item item = state().deactivate().getItem().get();
      return ctx.thenPersist(
        new MenuItemEvent.ItemUpdated(item.getItemId(), item.getData(), cmd.getStatus())
      );
    });
    //Set event handler for Item Deleted Event.
    builder.setEventHandler(MenuItemEvent.ItemDeleted.class, evt -> state().deactivate());

    return builder.build();
  }

  //Current state is that the item has been deactivated
  private Behavior inactive(ItemState state) {
    BehaviorBuilder builder = newBehaviorBuilder(state);
    builder.setReadOnlyCommandHandler(ItemCommand.GetItem.class, this::getItem);
    builder.setReadOnlyCommandHandler(ItemCommand.CreateItem.class, (cmd, ctx) ->
      ctx.invalidCommand("cannot issue create command on a deactivated item.")
    );
    builder.setReadOnlyCommandHandler(ItemCommand.UpdateItem.class, (cmd, ctx) ->
      ctx.invalidCommand("deactivated items cannot be updated.")
    );

    builder.setReadOnlyCommandHandler(ItemCommand.DeleteItem.class, (cmd, ctx) ->
      ctx.reply(state().getItem().get()) //Delete has already happened.
    );
    return builder.build();
  }

  private void getItem(ItemCommand.GetItem get, ReadOnlyCommandContext<Optional<Item>> ctx) {
    ctx.reply(state().getItem());
  }
}
