package com.hathor.daemon.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;
import com.hathor.daemon.services.dto.Output;
import com.hathor.daemon.services.dto.Response;
import com.hathor.daemon.services.dto.SendResponse;
import com.hathor.daemon.services.dto.SendTransaction;
import com.hathor.daemon.services.image.NftAttribute;
import com.hathor.daemon.services.image.NftProperties;
import com.hathor.daemon.services.image.PinataResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

@Service
public class PinataService {

   private final RestTemplate restTemplate;

   private static final String URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";

   @Value("${pinata.key}")
   private String key;

   @Value("${pinata.secret}")
   private String secret;

   private final Gson gson;

   public PinataService(RestTemplate restTemplate) {
      this.restTemplate = restTemplate;
      this.gson = new Gson();
   }

//   @PostConstruct
//   public void init() throws Exception {
//      NftProperties properties = new NftProperties();
//      properties.setName("Maros City");
//      properties.setDescription("Maros City");
//      properties.setFile("Test");
//      properties.getAttributes().add(new NftAttribute("Hathor Street", "#1"));
//      uploadJson(properties);
//   }

   public String uploadFile(String name, BufferedImage image) throws Exception {
      return uploadFile(name, getFile(image, name));
   }

   public String uploadJson(NftProperties nft) throws Exception {
      String name = nft.getName() + UUID.randomUUID() + ".json";
      String json = gson.toJson(nft);
      File file = new File(name);
      FileWriter fileWriter = new FileWriter(file);
      fileWriter.write(json);
      fileWriter.flush();
      fileWriter.close();

      FileSystemResource fsr = new FileSystemResource(file);
      return uploadFile(name, fsr);
   }

   public String uploadFile(String name, FileSystemResource fsr) throws Exception {
      HttpHeaders headers = new HttpHeaders();
      headers.add("pinata_api_key", key);
      headers.add("pinata_secret_api_key", secret);
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("file", fsr);
      map.add("pinataMetadata", "{\"name\": \"" + name + "\" }");

      HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);

      ResponseEntity<PinataResponse> response = restTemplate.postForEntity(URL, request, PinataResponse.class );

      if (response.getBody() != null && response.getBody().getIpfsHash() != null) {
         fsr.getFile().delete();
         return response.getBody().getIpfsHash();
      }

      return null;
   }

   private FileSystemResource getFile(BufferedImage image, String name) throws Exception {
      File cityImageFile = new File(name);
      ImageIO.write(image, "png", cityImageFile);
      return new FileSystemResource(cityImageFile);

   }

}
