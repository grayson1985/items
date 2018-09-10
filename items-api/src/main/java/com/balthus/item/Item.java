package com.balthus.item;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.UUID;

@Value
public final class Item {
  UUID itemId;
  ItemData data;
  ItemStatus status;

  @JsonCreator
  private Item(UUID itemId, ItemData data, ItemStatus status){
    this.itemId = itemId;
    this.data = data;
    this.status = status;
  }

  public Item(UUID itemId, ItemData data){
    this.itemId = itemId;
    this.data = data;
    this.status = ItemStatus.ACTIVE;
  }

  public Item updateItemData(ItemData itemData){
    assert status == ItemStatus.ACTIVE;
    return new Item(itemId, itemData, status);
  }

  public Item deactivate(){
    assert status == ItemStatus.ACTIVE;
    return new Item(itemId, data, ItemStatus.INACTIVE);
  }

}