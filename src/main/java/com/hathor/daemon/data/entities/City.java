package com.hathor.daemon.data.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
public class City {

   @Id
   @GeneratedValue(generator = "uuid2")
   @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
   private String id;

   private String shareId;

   private String name;

   private Boolean deleted;

   private Boolean requestImage;

   private String ipfs;

   @OneToMany(mappedBy="city", fetch = FetchType.EAGER)
   private Set<CityStreet> streets;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getShareId() {
      return shareId;
   }

   public void setShareId(String shareId) {
      this.shareId = shareId;
   }

   public Set<CityStreet> getStreets() {
      return streets;
   }

   public void setStreets(Set<CityStreet> streets) {
      this.streets = streets;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Boolean getDeleted() {
      return deleted;
   }

   public void setDeleted(Boolean deleted) {
      this.deleted = deleted;
   }

   public Boolean getRequestImage() {
      return requestImage;
   }

   public void setRequestImage(Boolean requestImage) {
      this.requestImage = requestImage;
   }

   public String getIpfs() {
      return ipfs;
   }

   public void setIpfs(String ipfs) {
      this.ipfs = ipfs;
   }
}
