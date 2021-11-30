package com.hathor.daemon.services.image;

import java.util.ArrayList;
import java.util.List;

public class NftProperties {

   private String name;
   private String description;
   private String file;

   private List<NftAttribute> attributes = new ArrayList<>();

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getFile() {
      return file;
   }

   public void setFile(String file) {
      this.file = file;
   }

   public List<NftAttribute> getAttributes() {
      return attributes;
   }

   public void setAttributes(List<NftAttribute> attributes) {
      this.attributes = attributes;
   }
}
