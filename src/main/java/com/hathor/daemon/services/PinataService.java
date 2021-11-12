package com.hathor.daemon.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hathor.daemon.services.dto.Output;
import com.hathor.daemon.services.dto.Response;
import com.hathor.daemon.services.dto.SendResponse;
import com.hathor.daemon.services.dto.SendTransaction;
import com.hathor.daemon.services.image.PinataResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Service
public class PinataService {

   private final RestTemplate restTemplate;

   private static final String URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";

   @Value("${pinata.key}")
   private String key;

   @Value("${pinata.secret}")
   private String secret;

   public PinataService(RestTemplate restTemplate) {
      this.restTemplate = restTemplate;
   }

   public String uploadFile(String name, BufferedImage image) throws Exception {
      HttpHeaders headers = new HttpHeaders();
      headers.add("pinata_api_key", key);
      headers.add("pinata_secret_api_key", secret);
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);

      FileSystemResource fsr = getFile(image, name);

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
