package com.chickentest.farm.domain;

public enum MovementType {
  BUY("buy"),
  SALE("sale"),
  SYSTEM("system");

  private final String description;

  MovementType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
