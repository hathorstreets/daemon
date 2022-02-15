package com.hathor.daemon.data.entities;

import javax.persistence.*;

@Entity
public class Street {

   @Id
   private Integer id;

   @Column(unique = true)
   private String picture;

   private String ipfs;

   @Column(unique = true)
   private String token;

   private boolean taken;

   private boolean burned;

   private String burnTransaction;

   @ManyToOne
   private Mint mint;

   @OneToOne
   @JoinColumn(name = "street_attributes_id", referencedColumnName = "id")
   private StreetAttributes streetAttributes;

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getPicture() {
      return picture;
   }

   public void setPicture(String picture) {
      this.picture = picture;
   }

   public boolean isTaken() {
      return taken;
   }

   public void setTaken(boolean taken) {
      this.taken = taken;
   }

   public String getToken() {
      return token;
   }

   public void setToken(String token) {
      this.token = token;
   }

   public Mint getMint() {
      return mint;
   }

   public void setMint(Mint mint) {
      this.mint = mint;
   }

   public String getIpfs() {
      return ipfs;
   }

   public void setIpfs(String ipfs) {
      this.ipfs = ipfs;
   }

   public StreetAttributes getStreetAttributes() {
      return streetAttributes;
   }

   public void setStreetAttributes(StreetAttributes streetAttributes) {
      this.streetAttributes = streetAttributes;
   }

   public boolean isBurned() {
      return burned;
   }

   public void setBurned(boolean burned) {
      this.burned = burned;
   }

   public String getBurnTransaction() {
      return burnTransaction;
   }

   public void setBurnTransaction(String burnTransaction) {
      this.burnTransaction = burnTransaction;
   }
}
