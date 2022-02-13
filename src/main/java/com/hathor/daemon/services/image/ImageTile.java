package com.hathor.daemon.services.image;

import java.awt.image.BufferedImage;

public class ImageTile {

   private Integer x;
   private Integer y;
   private BufferedImage image;
   private boolean sea;

   public ImageTile(Integer x, Integer y, BufferedImage image, boolean sea) {
      this.x = x;
      this.y = y;
      this.image = image;
      this.sea = sea;
   }

   public Integer getX() {
      return x;
   }

   public void setX(Integer x) {
      this.x = x;
   }

   public Integer getY() {
      return y;
   }

   public void setY(Integer y) {
      this.y = y;
   }

   public BufferedImage getImage() {
      return image;
   }

   public void setImage(BufferedImage image) {
      this.image = image;
   }

   public boolean isSea() {
      return sea;
   }

   public void setSea(boolean sea) {
      this.sea = sea;
   }
}
