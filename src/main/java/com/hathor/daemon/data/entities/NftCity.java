package com.hathor.daemon.data.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
public class NftCity {
   @Id
   @GeneratedValue(generator = "uuid2")
   @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
   private String id;

   private Date created;

   private int state;

   private String token;

   private String tokenWithoutTraits;

   private String ipfs;

   private String ipfsWithoutTraits;

   private String jsonIpfs;

   private String jsonIpfsWithoutTraits;

   private String transaction;

   @ManyToOne
   private Address depositAddress;

   private String userAddress;

   private String burnTransaction;

   @ManyToOne
   private City city;

   @OneToMany(mappedBy="nftCity", fetch = FetchType.EAGER)
   private Set<NftCityStreet> streets;

   private boolean dead;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public Date getCreated() {
      return created;
   }

   public void setCreated(Date created) {
      this.created = created;
   }

   public int getState() {
      return state;
   }

   public void setState(int state) {
      this.state = state;
   }

   public String getToken() {
      return token;
   }

   public void setToken(String token) {
      this.token = token;
   }

   public String getTransaction() {
      return transaction;
   }

   public void setTransaction(String transaction) {
      this.transaction = transaction;
   }

   public City getCity() {
      return city;
   }

   public void setCity(City city) {
      this.city = city;
   }

   public Address getDepositAddress() {
      return depositAddress;
   }

   public void setDepositAddress(Address depositAddress) {
      this.depositAddress = depositAddress;
   }

   public String getUserAddress() {
      return userAddress;
   }

   public void setUserAddress(String userAddress) {
      this.userAddress = userAddress;
   }

   public boolean isDead() {
      return dead;
   }

   public void setDead(boolean dead) {
      this.dead = dead;
   }

   public Set<NftCityStreet> getStreets() {
      return streets;
   }

   public void setStreets(Set<NftCityStreet> streets) {
      this.streets = streets;
   }

   public String getTokenWithoutTraits() {
      return tokenWithoutTraits;
   }

   public void setTokenWithoutTraits(String tokenWithoutTraits) {
      this.tokenWithoutTraits = tokenWithoutTraits;
   }

   public String getIpfs() {
      return ipfs;
   }

   public void setIpfs(String ipfs) {
      this.ipfs = ipfs;
   }

   public String getIpfsWithoutTraits() {
      return ipfsWithoutTraits;
   }

   public void setIpfsWithoutTraits(String ipfsWithoutTraits) {
      this.ipfsWithoutTraits = ipfsWithoutTraits;
   }

   public String getBurnTransaction() {
      return burnTransaction;
   }

   public void setBurnTransaction(String burnTransaction) {
      this.burnTransaction = burnTransaction;
   }

   public String getJsonIpfs() {
      return jsonIpfs;
   }

   public void setJsonIpfs(String jsonIpfs) {
      this.jsonIpfs = jsonIpfs;
   }

   public String getJsonIpfsWithoutTraits() {
      return jsonIpfsWithoutTraits;
   }

   public void setJsonIpfsWithoutTraits(String jsonIpfsWithoutTraits) {
      this.jsonIpfsWithoutTraits = jsonIpfsWithoutTraits;
   }
}
