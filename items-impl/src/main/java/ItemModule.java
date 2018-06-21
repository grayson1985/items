import com.balthus.item.ItemService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * The module that binds the ItemsService so that it can be served.
 */
public class ItemModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(ItemService.class, ItemServiceImpl.class);
  }
}
