package com.hathor.daemon.services;

import com.hathor.daemon.data.entities.Street;
import com.hathor.daemon.data.entities.enums.MintState;
import com.hathor.daemon.data.repositories.StreetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DaemonBurner {

   Logger logger = LoggerFactory.getLogger(DaemonBurner.class);

   private final StreetRepository streetRepository;
   private final WalletService walletService;
   private final RetryTemplate retryTemplate;


   public DaemonBurner(StreetRepository streetRepository, WalletService walletService, RetryTemplate retryTemplate) {
      this.streetRepository = streetRepository;
      this.walletService = walletService;
      this.retryTemplate = retryTemplate;
   }

   //@PostConstruct
   public void burn(){
      //for(int i = 0; i < 859; i++) {
         //logger.info("Burning streets " + i);
         List<Street> streets = streetRepository.findNotTaken(5);
         List<String> tokens = streets.stream().map(it -> it.getToken()).collect(Collectors.toList());
         String hash = walletService.sendTokens("HDeadDeadDeadDeadDeadDeadDeagTPgmn", tokens);
         if(hash != null) {
            streets.forEach(it -> {
               it.setBurnTransaction(hash);
               it.setTaken(true);
               it.setBurned(true);
            });
            try {
               retryTemplate.execute(context -> {
                  streetRepository.saveAll(streets);
                  return null;
               });
            } catch (Exception ex) {
               logger.error("could not save streets");
               logger.error(hash);
               for (Street s : streets) {
                  logger.error("Street " + s.getId() + " not saved");
               }
            }
         } else {
            logger.error("Could not burn streets!");
         }

      //}
   }
}
