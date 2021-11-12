package com.hathor.daemon.services;

import com.hathor.daemon.data.entities.City;
import com.hathor.daemon.data.repositories.CityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DaemonImageService {

   private final CityRepository cityRepository;
   private final ImageService imageService;

   Logger logger = LoggerFactory.getLogger(DaemonImageService.class);

   public DaemonImageService(CityRepository cityRepository, ImageService imageService) {
      this.cityRepository = cityRepository;
      this.imageService = imageService;
   }

   @Scheduled(fixedDelay = 10000)
   public void loop(){
      List<City> cities = cityRepository.findAllByRequestImage(true);
      for(City city : cities) {
         logger.info("Processing full size image for city " + city.getId());
         String hash = null;
         try {
            hash = imageService.createImage(city);
            if(hash != null) {
               city.setIpfs(hash);
               city.setRequestImage(false);
               cityRepository.save(city);
            }
         } catch (Exception ex) {
            logger.error("Could not create image for city " + city.getId(), ex);
         }
      }
   }
}
