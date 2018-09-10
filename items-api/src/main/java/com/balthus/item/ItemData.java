package com.balthus.item;

  import com.fasterxml.jackson.annotation.JsonCreator;
  import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
  import lombok.Value;

@Value
public final class ItemData {
  String name;
  String description;
  Double price;

  @JsonCreator
  public ItemData(String name, String description, Double price){
    this.name = name;
    this.description = description;
    this.price = price;
  }
}