package com.hathor.daemon.data.entities;

import javax.persistence.*;

@Entity
public class NftCityStreet {

   @Id
   @GeneratedValue(strategy= GenerationType.AUTO)
   private Integer id;

   @ManyToOne
   private Street street;

   private boolean sent;

   @ManyToOne
   private com.hathor.daemon.data.entities.NftCity nftCity;

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public Street getStreet() {
      return street;
   }

   public void setStreet(Street street) {
      this.street = street;
   }

   public com.hathor.daemon.data.entities.NftCity getNftCity() {
      return nftCity;
   }

   public void setNftCity(com.hathor.daemon.data.entities.NftCity nftCity) {
      this.nftCity = nftCity;
   }

   public boolean isSent() {
      return sent;
   }

   public void setSent(boolean sent) {
      this.sent = sent;
   }
}
