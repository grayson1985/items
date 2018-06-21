package com.balthus.items.impl;

import com.balthus.item.ItemService;
import org.junit.Test;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static org.junit.Assert.assertEquals;

public class ItemsServiceTest {
    @Test
    public void shouldStoreItem() {
        withServer(defaultSetup().withCassandra(), server -> {
            ItemService service = server.client(ItemService.class);
        });
    }
}
