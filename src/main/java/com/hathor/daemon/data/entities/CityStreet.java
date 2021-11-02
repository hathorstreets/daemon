package com.hathor.daemon.data.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity
public class CityStreet {

   @Id
   @GeneratedValue(strategy= GenerationType.AUTO)
   private Integer id;

   private BigDecimal x;

   private BigDecimal y;

   private int streetId;

   @ManyToOne
   private City city;

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public int getStreetId() {
      return streetId;
   }

   public void setStreetId(int streetId) {
      this.streetId = streetId;
   }

   public City getCity() {
      return city;
   }

   public void setCity(City city) {
      this.city = city;
   }

   public BigDecimal getX() {
      return x;
   }

   public void setX(BigDecimal x) {
      this.x = x;
   }

   public BigDecimal getY() {
      return y;
   }

   public void setY(BigDecimal y) {
      this.y = y;
   }
}
