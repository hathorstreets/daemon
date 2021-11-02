package com.hathor.daemon.data.entities;

import com.hathor.daemon.services.nft.AttributeType;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class StreetAttributes {

   @Id
   @GeneratedValue(strategy= GenerationType.AUTO)
   private Integer id;

   private String topQuad;

   private String leftQuad;

   private String rightQuad;

   private String bottomQuad;

   private String road;

   private String billboard;

   private String special;

   @OneToOne(mappedBy = "streetAttributes")
   private Street street;

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getTopQuad() {
      return topQuad;
   }

   public void setTopQuad(String topQuad) {
      this.topQuad = topQuad;
   }

   public String getLeftQuad() {
      return leftQuad;
   }

   public void setLeftQuad(String leftQuad) {
      this.leftQuad = leftQuad;
   }

   public String getRightQuad() {
      return rightQuad;
   }

   public void setRightQuad(String rightQuad) {
      this.rightQuad = rightQuad;
   }

   public String getBottomQuad() {
      return bottomQuad;
   }

   public void setBottomQuad(String bottomQuad) {
      this.bottomQuad = bottomQuad;
   }

   public String getRoad() {
      return road;
   }

   public void setRoad(String road) {
      this.road = road;
   }

   public String getBillboard() {
      return billboard;
   }

   public void setBillboard(String billboard) {
      this.billboard = billboard;
   }

   public String getSpecial() {
      return special;
   }

   public void setSpecial(String special) {
      this.special = special;
   }

   public Street getStreet() {
      return street;
   }

   public void setStreet(Street street) {
      this.street = street;
   }
}
