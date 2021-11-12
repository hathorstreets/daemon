package com.hathor.daemon.services.image;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PinataResponse {

   @JsonProperty("IpfsHash")
   private String IpfsHash;
   @JsonProperty("PinSize")
   private String PinSize;
   @JsonProperty("TimeStamp")
   private String TimeStamp;

   public String getIpfsHash() {
      return IpfsHash;
   }

   public void setIpfsHash(String ipfsHash) {
      IpfsHash = ipfsHash;
   }

   public String getPinSize() {
      return PinSize;
   }

   public void setPinSize(String pinSize) {
      PinSize = pinSize;
   }

   public String getTimeStamp() {
      return TimeStamp;
   }

   public void setTimeStamp(String timeStamp) {
      TimeStamp = timeStamp;
   }
}
