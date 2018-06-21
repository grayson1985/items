package events;

import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;

public class ItemEventTag {

  public static final AggregateEventShards<ItemEvent> TAG = AggregateEventTag.sharded(ItemEvent.class, 4);

}
