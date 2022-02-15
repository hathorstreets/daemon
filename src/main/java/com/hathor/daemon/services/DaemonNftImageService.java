package com.hathor.daemon.services;

import com.hathor.daemon.data.entities.NftCity;
import com.hathor.daemon.data.entities.NftCityStreet;
import com.hathor.daemon.data.entities.enums.MintState;
import com.hathor.daemon.data.repositories.CityRepository;
import com.hathor.daemon.data.repositories.NftCityRepository;
import com.hathor.daemon.data.repositories.NftCityStreetRepository;
import com.hathor.daemon.data.repositories.StreetRepository;
import com.hathor.daemon.services.image.NftProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DaemonNftImageService {

   private final CityRepository cityRepository;
   private final NftImageService nftImageService;
   private final NftCityRepository nftCityRepository;
   private final RetryTemplate retryTemplate;
   private final WalletService walletService;
   private final PinataService pinataService;
   private final NftCityStreetRepository nftCityStreetRepository;
   private final StreetRepository streetRepository;

   Logger logger = LoggerFactory.getLogger(DaemonImageService.class);

   public DaemonNftImageService(CityRepository cityRepository, NftImageService nftImageService,
           NftCityRepository nftCityRepository, RetryTemplate retryTemplate, WalletService walletService, PinataService pinataService, NftCityStreetRepository nftCityStreetRepository,
           StreetRepository streetRepository) {
      this.cityRepository = cityRepository;
      this.nftImageService = nftImageService;
      this.nftCityRepository = nftCityRepository;
      this.retryTemplate = retryTemplate;
      this.walletService = walletService;
      this.pinataService = pinataService;
      this.nftCityStreetRepository = nftCityStreetRepository;
      this.streetRepository = streetRepository;
   }

   @Scheduled(fixedDelay = 10000)
   public void loop(){
      //LOADING CITIES TO PROCESS
      final List<NftCity> cities = new ArrayList<>();
      try {
         retryTemplate.execute(context -> {
            cities.addAll(this.nftCityRepository.getAllByState(MintState.WAITING_FOR_DEPOSIT.ordinal()));
            return null;
         });
         retryTemplate.execute(context -> {
            cities.addAll(this.nftCityRepository.getAllByState(MintState.SENDING_NFT.ordinal()));
            return null;
         });
      } catch (Exception ex) {
         logger.error("Failed to get mints from database", ex);
      }

      final List<NftCity> citiesToBurn = new ArrayList<>();
      try {
         retryTemplate.execute(context -> {
            citiesToBurn.addAll(this.nftCityRepository.getAllByState(MintState.NFT_SENT.ordinal()));
            return null;
         });
      } catch (Exception ex) {
         logger.error("Failed to get mints from database", ex);
      }
      //END OF LOADING CITIES TO PROCESS

      for(NftCity city : citiesToBurn) {
         logger.info("Burning city " + city.getId());
         List<String> tokens = new ArrayList<>();
         for(NftCityStreet street : city.getStreets()) {
            String token = street.getStreet().getToken();
            tokens.add(token);
         }

         String hash = walletService.burnTokens(tokens);
         if(hash == null) {
            logger.error("Unable to burn tokens for city " + city.getId());
         }
         else{
            city.setState(MintState.TOKEN_BURNED.ordinal());
            city.setBurnTransaction(hash);
            try {
               retryTemplate.execute(context -> {
                  nftCityRepository.save(city);
                  return null;
               });
            } catch (Exception ex) {
               logger.error("Unable to save city after burn", ex);
            }
            for(NftCityStreet s : city.getStreets()) {
               s.getStreet().setBurned(true);
               try {
                  retryTemplate.execute(context -> {
                     streetRepository.save(s.getStreet());
                     return null;
                  });
               } catch (Exception ex) {
                  logger.error("Unable to save street to burn " + s.getStreet().getId(), ex);
               }
            }
         }
      }

      for (NftCity city : cities) {
         walletService.checkWallets();

         try {
            logger.info("========================================================================");
            logger.info("Processing city " + city.getId());
            if (city.getState() == MintState.WAITING_FOR_DEPOSIT.ordinal()) {
               if (checkForStreets(city)) {
                  logger.info("All streets received!");
                  city.setState(MintState.SENDING_NFT.ordinal());
                  retryTemplate.execute(context -> {
                     nftCityRepository.save(city);
                     return null;
                  });
               } else {
                  logger.info("Waiting for the streets");
               }
            }
            if (city.getState() == MintState.SENDING_NFT.ordinal()) {
               logger.info("Creating image with traits");
               Pair<String, NftProperties> nft = nftImageService.createImage(city.getCity(), true);
               logger.info("Creating image without traits");
               Pair<String, NftProperties> nftWithoutTraits = nftImageService.createImage(city.getCity(), false);
               if (nft != null && nft.getFirst() != null &&
                       nftWithoutTraits != null && nftWithoutTraits.getFirst() != null) {
                  logger.info("Successfully uploaded images!");
                  city.setIpfs("https://ipfs.io/ipfs/" + nft.getFirst());
                  city.setIpfsWithoutTraits("https://ipfs.io/ipfs/" + nftWithoutTraits.getFirst());

                  logger.info("Creating NFTs");
                  String name = city.getCity().getName() == null || city.getCity().getName().isEmpty() ? "No name city" : city.getCity().getName();
                  logger.info("Creating NFT with traits");

                  Iterable<NftCity> allCities = nftCityRepository.findAll();
                  List<NftCity> allCitiesList = new ArrayList<NftCity>();
                  allCities.forEach(allCitiesList::add);

                  allCitiesList.sort(new Comparator<NftCity>() {
                     @Override
                     public int compare(NftCity o1, NftCity o2) {
                        return o1.getCreated().compareTo(o2.getCreated());
                     }
                  });

                  int order = 1;
                  boolean found = false;
                  for(NftCity c : allCitiesList) {
                     if (c.getId().equals(city.getId())) {
                        found = true;
                        break;
                     }
                     order++;
                  }

                  String symbol = "HSC";
                  String symbolWoTraits = "HSCC";
                  if(found) {
                     symbol = "HSC" + order;
                     symbolWoTraits = "HSCC" + order;
                  }
                  String nftHashWithTraits = walletService.createNft(name, symbol, "ipfs://ipfs/" + nft.getFirst());
                  logger.info("Creating NFT without traits");
                  String nftHashWithoutTraits = walletService.createNft(name, symbolWoTraits, "ipfs://ipfs/" + nftWithoutTraits.getFirst());
                  city.setToken(nftHashWithTraits);
                  city.setTokenWithoutTraits(nftHashWithoutTraits);
                  retryTemplate.execute(context -> {
                     nftCityRepository.save(city);
                     return null;
                  });
                  logger.info("NFTs saved!");

                  logger.info("Sending NFTs");
                  String hash = walletService.sendTokens(city.getUserAddress(), Arrays.asList(nftHashWithTraits, nftHashWithoutTraits));
                  if(hash != null) {
                     logger.info("NFTs sent! " + hash);
                     city.setState(MintState.NFT_SENT.ordinal());
                     retryTemplate.execute(context -> {
                        nftCityRepository.save(city);
                        return null;
                     });
                  } else {
                     logger.error("Could not send NFTs");
                  }
               }
            }

            if(city.getState() != MintState.NFT_SENT.ordinal()) {
               Date created = city.getCreated();
               Date now = new Date();

               long diff = now.getTime() - created.getTime();
               long hours = TimeUnit.MILLISECONDS.toHours(diff);
               if(hours >= 3) {
                  logger.info("Sending NFTs back for city " + city.getId());
                  sendBack(city);
                  city.setState(MintState.HTR_SENT_BACK.ordinal());
                  retryTemplate.execute(context -> {
                     nftCityRepository.save(city);
                     return null;
                  });
               }
            }

            if(city.getState() == MintState.HTR_SENT_BACK.ordinal()) {
               if(city.getStreets().stream().anyMatch(it -> it.isSent())) {
                  sendBack(city);
               }
            }
         } catch (Exception ex) {
            logger.error("City failed " + city.getId(), ex);
         }
      }
   }

   private boolean checkForStreets(NftCity city) throws Exception {
      boolean result = true;

      for(NftCityStreet street : city.getStreets()) {
         String token = street.getStreet().getToken();
         if(!street.isSent()) {
            if (walletService.checkNftBalance(city.getDepositAddress().getAddress(), token)) {
               logger.info("Street " + street.getStreet().getId() + " received!");
               street.setSent(true);
               retryTemplate.execute(context -> {
                  nftCityStreetRepository.save(street);
                  return null;
               });
            } else {
               logger.info("Street " + street.getStreet().getId() + " NOT received yet!");
               result = false;
            }
         } else {
            logger.info("Street " + street.getStreet().getId() + " received!");
         }
      }

      return result;
   }

   private void sendBack(NftCity city) throws Exception {
      for(NftCityStreet street : city.getStreets()) {
         String token = street.getStreet().getToken();
         if(walletService.checkNftBalance(city.getDepositAddress().getAddress(), token)) {
            String hash = walletService.sendTokens(city.getUserAddress(), Arrays.asList(token));
            if(hash != null) {
               street.setSent(false);
               retryTemplate.execute(context -> {
                  nftCityStreetRepository.save(street);
                  return null;
               });
            }
         }
      }
   }
}
