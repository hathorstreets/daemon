package com.hathor.daemon.services;

import com.hathor.daemon.data.entities.Street;
import com.hathor.daemon.data.repositories.StreetRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SpecialCasesService {

   private final WalletService walletService;
   private final StreetRepository streetRepository;


   public SpecialCasesService(WalletService walletService, StreetRepository streetRepository) {
      this.walletService = walletService;
      this.streetRepository = streetRepository;
   }

   public void createSpecialNft() {
      String hash = walletService.createNft("Halloween Hathor Street", "HSS1", "https://ipfs.io/ipfs/QmbRTsuXcd2hMbPigXkcfn3T7eGP9WjbhbeRC9Mb5NK8YZ");

      Street s = streetRepository.findById(12001).get();

      s.setToken(hash);
      s.setIpfs("https://ipfs.io/ipfs/QmZYexZmezMoKfMxQ96b4wANXN8CbcpCDyPC8ekpBps1xx");
      streetRepository.save(s);

//      Street street = new Street();
//      street.setId(12001);
//      street.setToken(hash);
//      street.setPicture("HalloweenHathorStreet.png");
//      street.setTaken(true);
//      street.setIpfs("https://ipfs.io/ipfs/QmUwQCbJpA7pBhwyVW8jvmYcjh54xNznsQDrZMn9xE3BPt");
//
//      StreetAttributes attributes = new StreetAttributes();
//      attributes.setTopQuad("Halloween Hathor HQ");
//      attributes.setLeftQuad("Halloween Church & Graveyard");
//      attributes.setRightQuad("Halloween House on Fire");
//      attributes.setBottomQuad("Halloween Military Base");
//      attributes.setBillboard("Halloween Abandoned");
//      attributes.setRoad("Halloween");
//      attributes.setSpecial("None");
//
//      streetAttributesRepository.save(attributes);
//
//      street.setStreetAttributes(attributes);
      //streetRepository.save(street);
   }

   public void sendMultipleNfts(String address, List<Integer> numbers, boolean checkTaken) {
      Iterable<Street> streets = streetRepository.findAllById(numbers);
      List<Street> streetList = new ArrayList<>();
      for(Street s : streets) {
         if(checkTaken) {
            if (!s.isTaken()) {
               streetList.add(s);
            }
         } else {
            streetList.add(s);
         }
      }

      List<String> tokens = streetList.stream().map(street -> street.getToken()).collect(Collectors.toList());
      String hash = walletService.sendTokens(address, tokens);
      if(hash != null) {
         System.out.println(hash);
         for(Street street : streetList) {
            System.out.println("https://www.hathorstreets.com/explorer.html?id=" + street.getId());
            street.setTaken(true);
         }
         streetRepository.saveAll(streetList);
      }
   }

   public void sendOneNft(String address, int number) {
      Optional<Street> street = streetRepository.findById(number);
      if (street.isPresent() && !street.get().isTaken()) {
         List<String> tokens = Arrays.asList(street.get().getToken());
         String hash = walletService.sendTokens(address, tokens);
         if (hash != null) {
            System.out.println(hash);
            System.out.println(street.get().getIpfs());
            System.out.println("https://explorer.hathor.network/token_detail/" + street.get().getToken());
            street.get().setTaken(true);
            streetRepository.save(street.get());
         }
      }
   }

   public void sendNft(String name, String address) {
      List<Street> streets = streetRepository.findByTakenAndStreetAttributesBillboard(false, name);
      List<Street> withoutSpecials = streets.stream().filter(street -> street.getStreetAttributes().getSpecial().equals("None")).collect(Collectors.toList());

//      List<Street> withoutSpecials = new ArrayList<Street>();
//      Iterable<Street> streets = streetRepository.findAllById(Arrays.asList(102, 104, 120, 180, 190, 210, 68));
//      for(Street  s : streets) {
//         withoutSpecials.add(s);
//      }

      //List<Street> special = streets.stream().filter(street -> street.getStreetAttributes().getSpecial().equals("Satellite")).collect(Collectors.toList());
      withoutSpecials = withoutSpecials.subList(0, 5);
      //withoutSpecials.add(special.get(0));
      List<String> tokens = withoutSpecials.stream().map(street -> street.getToken()).collect(Collectors.toList());
      String hash = walletService.sendTokens(address, tokens);
      if(hash != null) {
         System.out.println(hash);
         for(Street street : withoutSpecials) {
            //System.out.println(street.getId().toString());
            System.out.println(street.getIpfs());
            System.out.println("https://explorer.hathor.network/token_detail/" + street.getToken());
            street.setTaken(true);
         }
         streetRepository.saveAll(withoutSpecials);
      }
   }

}
