package com.hathor.daemon.data.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Address {
   @Id
   @GeneratedValue(strategy=GenerationType.AUTO)
   private Integer id;

   @Column(unique = true)
   private String address;

   private boolean taken;

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getAddress() {
      return address;
   }

   public void setAddress(String address) {
      this.address = address;
   }

   public boolean isTaken() {
      return taken;
   }

   public void setTaken(boolean taken) {
      this.taken = taken;
   }
}
