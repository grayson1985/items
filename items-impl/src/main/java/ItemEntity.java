import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import commands.ItemCommand;
import events.ItemEvent;
import states.ItemState;

import java.time.LocalDateTime;
import java.util.Optional;


public class ItemEntity extends PersistentEntity<ItemCommand, ItemEvent, ItemState> {

  @Override
  public Behavior initialBehavior(Optional<ItemState> itemState) {
    BehaviorBuilder behaviorBuilder = newBehaviorBuilder(
      ItemState.builder().item(Optional.empty())
        .timestamp(LocalDateTime.now().toString()).build()
    );
    behaviorBuilder.setCommandHandler(ItemCommand.CreateItem.class, (cmd, ctx) -> ctx.thenPersist(
      ItemEvent.ItemCreated.builder().item(cmd.getItem()).entityId(entityId()).build(),
      evt -> ctx.reply(Done.getInstance()))
    );

    behaviorBuilder.setEventHandler(ItemEvent.ItemCreated.class, evt ->
      ItemState.builder().item(Optional.of(evt.getItem())).timestamp(LocalDateTime.now().toString()).build()
    );

    behaviorBuilder.setCommandHandler(ItemCommand.CreateItem.class, (cmd, ctx) -> ctx.thenPersist(
      ItemEvent.ItemUpdated.builder().item(cmd.getItem()).entityId(entityId()).build(),
      evt -> ctx.reply(Done.getInstance()))
    );

    behaviorBuilder.setEventHandler(ItemEvent.ItemUpdated.class, evt ->
      ItemState.builder().item(Optional.of(evt.getItem())).timestamp(LocalDateTime.now().toString()).build()
    );

    behaviorBuilder.setCommandHandler(ItemCommand.DeleteItem.class, (cmd, ctx) ->
      ctx.thenPersist(ItemEvent.ItemDeleted.builder().item(cmd.getItem()).entityId(entityId()).build(),
        evt -> ctx.reply(Done.getInstance()))
    );

    behaviorBuilder.setEventHandler(ItemEvent.ItemDeleted.class, evt ->
      ItemState.builder().item(Optional.empty())
        .timestamp(LocalDateTime.now().toString()).build()
    );

    behaviorBuilder.setReadOnlyCommandHandler(ItemCommand.GetCurrentStateItem.class, (cmd, ctx) ->
      ctx.reply(state().getItem())
    );

    return behaviorBuilder.build();

  }
}
