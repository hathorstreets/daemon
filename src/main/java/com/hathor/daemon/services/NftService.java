package com.hathor.daemon.services;

import com.google.gson.Gson;
import com.hathor.daemon.data.entities.Street;
import com.hathor.daemon.data.entities.StreetAttributes;
import com.hathor.daemon.data.repositories.StreetAttributesRepository;
import com.hathor.daemon.data.repositories.StreetRepository;
import com.hathor.daemon.services.dto.Addresses;
import com.hathor.daemon.services.nft.AttributeType;
import com.hathor.daemon.services.nft.Nft;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NftService {

   private static final String JSON_IPFS = "QmXaY8iC2bXdohi9gY7K5HFjJj3aDfKssRWHHVV4rBYNh5";
   private static final String IMAGE_IPFS = "QmZfUe56NDsrGG2chWnf2pMXkM9PiN7uTmo3nuwuGkY8iv";
   private static final int NFT_COUNT = 11111;

   private final WalletService walletService;
   private final StreetRepository streetRepository;
   private final StreetAttributesRepository streetAttributesRepository;
   private final Gson gson;

   Logger logger = LoggerFactory.getLogger(NftService.class);

   public NftService(WalletService walletService, StreetRepository streetRepository,
                     StreetAttributesRepository streetAttributesRepository) {
      this.walletService = walletService;
      this.streetRepository = streetRepository;
      this.streetAttributesRepository = streetAttributesRepository;

      this.gson = new Gson();
   }

   private String createNft(int i, Street s) throws Exception {
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream("generator/metadata/" + i + ".json");
      String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
      Nft nft = gson.fromJson(text, Nft.class);

      String ipfsJsonUrl = "ipfs://ipfs/" + JSON_IPFS + "/" + i + ".json";

      String hash = null;
      String tokenSymbol = "S" + i;
      if(i >= 10000) {
         tokenSymbol = "H" + (i - 9999);
      }

      while(hash == null) {
         hash = walletService.createNft("Hathor Street " + i, tokenSymbol, ipfsJsonUrl);
         if(hash == null) {
            try {
               walletService.checkWallets();
               Thread.sleep(10000);
            } catch (Exception ignored) {

            }
         }
      }
      return hash;
   }

   public void generateNftsUFO() {
      List<Integer> ufos = Arrays.asList(374, 2250, 2741, 4556, 8929, 9527, 10248, 10332, 10517);
      for(int i : ufos) {
         try {
            logger.info("Creating NFT " + i);

            Optional<Street> st = streetRepository.findById(i);
            if (st.isPresent()) {
               if (st.get().getStreetAttributes().getSpecial().equals("UFO")) {
                  Street street = st.get();
                  String hash = createNft(i, street);
                  street.setToken(hash);
                  String ipfsPublicUrl = "https://ipfs.io/ipfs/" + IMAGE_IPFS + "/" + i + ".png";
                  street.setIpfs(ipfsPublicUrl);
                  streetRepository.save(street);
               } else {
                  logger.error("THIS IS NOT UFO STREET!");
               }
            }
         } catch (Exception ex) {
            logger.error("Error", ex);
         }
      }
   }

   public void generateNfts() {
      List<Integer> failed = new ArrayList<>();
      for (int i = 1; i <= NFT_COUNT; i++) {
         try {
            logger.info("Creating NFT " + i);
            try {
               walletService.checkWallets();
            } catch (Exception ex) {
               logger.error("Unable to check wallets, exiting!", ex);
               break;
            }

            Integer htrBalance = walletService.checkHtrBalance(true);
            if (htrBalance != null) {
               logger.info("HTR Balance is " + htrBalance);
               if(htrBalance == 0) {
                  logger.info("Balance is 0, exiting");
                  break;
               }
            }

            Optional<Street> st = streetRepository.findById(i);
            if (st.isPresent()) {
               Street street = st.get();
               String hash = createNft(i, street);
               street.setToken(hash);
               streetRepository.save(street);
            }

            logger.info("Successfully created NFT " + i);
            Thread.sleep(3000);
         } catch (Exception ex) {
            logger.error("Could not create NFT " + i, ex);
            failed.add(i);
         }
      }

      if(failed.size() > 0) {
         logger.error("FAILED STREETS");
         for(Integer i : failed) {
            logger.info(i.toString());
         }
      }
   }

   public void generateDatabaseNfts() {
      for(int i = 1; i <= NFT_COUNT; i++) {
         try {
            Optional<Street> st = streetRepository.findById(i);
            if(st.isPresent()) {
               logger.info("NFT " + i + " already exists");
               continue;
            }

            logger.info("Creating NFT " + i);

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("generator/metadata/" + i + ".json");
            String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            Nft nft = gson.fromJson(text, Nft.class);

            String ipfsJsonUrl = "ipfs://ipfs/" + JSON_IPFS + "/" + i + ".json";
            String ipfsPublicUrl = "https://ipfs.io/ipfs/" + IMAGE_IPFS + "/" + i + ".png";

            String hash = null;
            String tokenSymbol = "S" + i;
            if(i >= 10000) {
               tokenSymbol = "H" + (i - 9999);
            }

            hash = "hash" + i;

            Street street = new Street();
            street.setId(i);
            street.setToken(hash);
            street.setPicture(i + ".png");
            street.setTaken(false);
            street.setIpfs(ipfsPublicUrl);

            StreetAttributes attributes = new StreetAttributes();
            attributes.setTopQuad(nft.getAttributeValue(AttributeType.TOP));
            attributes.setLeftQuad(nft.getAttributeValue(AttributeType.LEFT));
            attributes.setRightQuad(nft.getAttributeValue(AttributeType.RIGHT));
            attributes.setBottomQuad(nft.getAttributeValue(AttributeType.BOTTOM));
            attributes.setBillboard(nft.getAttributeValue(AttributeType.BILLBOARD));
            attributes.setRoad(nft.getAttributeValue(AttributeType.ROAD));
            attributes.setSpecial(nft.getAttributeValue(AttributeType.SPECIAL));

            streetAttributesRepository.save(attributes);

            street.setStreetAttributes(attributes);
            streetRepository.save(street);

            logger.info("Successfully created NFT " + i);

         } catch (Exception ex) {
            logger.error("Could not create NFT " + i, ex);
         }
      }
   }
}
