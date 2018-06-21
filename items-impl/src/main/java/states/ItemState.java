package states;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.balthus.item.Item;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import java.util.Optional;

@Value
@Builder
@JsonDeserialize
@AllArgsConstructor
public class ItemState implements CompressedJsonable {
    Optional<Item> item;
    String timestamp;
}
