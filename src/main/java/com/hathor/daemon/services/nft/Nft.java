package com.hathor.daemon.services.nft;

import java.util.List;

public class Nft {

   private String name;
   private String description;
   private String file;
   private List<Attribute> attributes;

   public String getAttributeValue(AttributeType type) {
      for (Attribute attribute : attributes) {
         if(attribute.getType().equals(type.getName())) {
            return attribute.getValue();
         }
      }
      return null;
   }

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

   public List<Attribute> getAttributes() {
      return attributes;
   }

   public void setAttributes(List<Attribute> attributes) {
      this.attributes = attributes;
   }
}
