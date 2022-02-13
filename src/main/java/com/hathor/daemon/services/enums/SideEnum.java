package com.hathor.daemon.services.enums;

public enum SideEnum {

   LEFT("left"),
   TOP("top"),
   RIGHT("right"),
   BOTTOM("bottom");

   private String side;

   private SideEnum(String side) {
      this.side = side;
   }

   public String getSide() {
      return side;
   }
}
