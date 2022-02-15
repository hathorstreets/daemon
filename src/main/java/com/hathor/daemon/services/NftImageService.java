package com.hathor.daemon.services;

import com.google.gson.Gson;
import com.hathor.daemon.data.entities.City;
import com.hathor.daemon.data.entities.CityStreet;
import com.hathor.daemon.data.entities.Street;
import com.hathor.daemon.data.entities.StreetAttributes;
import com.hathor.daemon.data.repositories.CityRepository;
import com.hathor.daemon.data.repositories.StreetRepository;
import com.hathor.daemon.services.enums.SideEnum;
import com.hathor.daemon.services.image.ImageMaps;
import com.hathor.daemon.services.image.ImageTile;
import com.hathor.daemon.services.image.NftAttribute;
import com.hathor.daemon.services.image.NftProperties;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NftImageService {

   private final CityRepository cityRepository;
   private final StreetRepository streetRepository;
   private final PinataService pinataService;
   private final SeaItemProvider seaItemProvider;

   private final BigDecimal STREET_WIDTH = new BigDecimal("1720");
   private final BigDecimal STREET_WIDTH_SNAP = new BigDecimal("859");

   private final BigDecimal STREET_HEIGHT = new BigDecimal("1445");
   private final BigDecimal STREET_HEIGHT_SNAP = new BigDecimal("496");

   private final BigDecimal STREET_X_SNAP = new BigDecimal("249.6");
   private final BigDecimal STREET_Y_SNAP = new BigDecimal("144");


   private final int imageBorder = 600;

   public NftImageService(CityRepository cityRepository, StreetRepository streetRepository, PinataService pinataService, SeaItemProvider seaItemProvider) {
      this.cityRepository = cityRepository;
      this.streetRepository = streetRepository;
      this.pinataService = pinataService;
      this.seaItemProvider = seaItemProvider;
   }

   //@PostConstruct
   public void init() throws Exception {
      //male 5c5bcd89-b639-4bea-8852-285eed08dd53
      //velke 7a35e4c6-fe79-4ae9-9978-b75608635f1f
      //pokazene 0841b880-1533-43cf-98e1-f94c552db1e1

      //deanland dobre 0202eae0-6f6d-4de8-8bcc-9c6ba3eb19d2
      // test zle 03317008-7784-46bf-b796-f47f9914befe

     City c = cityRepository.findById("f9b0a598-fe7c-497c-95df-98d86b8324cc").get();
     createImage(c, true);
      createImage(c, false);
//      try {
//         for(int i = 0; i < 20; i++) {
//            createImage(c, true);
//         }
//      } catch (Exception ex) {
//         ex.printStackTrace();
//      }


//      int i = 1;
//      for(City c : cityRepository.findAll()) {
//         System.out.println("Creating city " + c.getName() + " " + c.getId());
//         try {
//            createImage(c, true);
//         } catch (Exception ex) {
//            ex.printStackTrace();
//         }
//         i++;
//     }
   }

   public Pair<String, String> createImage(City city, boolean withTraits) throws Exception {
      List<CityStreet> streets = new ArrayList<>(city.getStreets());

      BigDecimal minX = null;
      BigDecimal maxX = null;
      BigDecimal minY = null;
      BigDecimal maxY = null;
      for(CityStreet street : streets) {
         BigDecimal x = street.getX().divide(STREET_X_SNAP, RoundingMode.HALF_UP).multiply(STREET_WIDTH_SNAP);
         BigDecimal y = street.getY().divide(STREET_Y_SNAP, RoundingMode.HALF_UP).multiply(STREET_HEIGHT_SNAP);

         if(minX == null || minX.compareTo(x) > 0) {
            minX = x;
         }
         if(maxX == null || maxX.compareTo(x) < 0) {
            maxX = x;
         }

         if(minY == null || minY.compareTo(y) > 0) {
            minY = y;
         }
         if(maxY == null || maxY.compareTo(y) < 0) {
            maxY = y;
         }
      }

      BigDecimal width = maxX.subtract(minX).add(STREET_WIDTH);
      BigDecimal height = maxY.subtract(minY).add(STREET_HEIGHT);

      streets = streets.stream().sorted((o1, o2) -> o1.getY().compareTo(o2.getY())).collect(Collectors.toList());

      BufferedImage cityImage = new BufferedImage(width.intValue() + imageBorder * 2, height.intValue() + imageBorder * 2, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graph = cityImage.createGraphics();

      List<ImageTile> tiles = new ArrayList<>();

      for(CityStreet cs : streets) {
         Street s = streetRepository.findById(cs.getStreetId()).get();
         StreetAttributes sa = s.getStreetAttributes();

         BigDecimal x = cs.getX().divide(STREET_X_SNAP, RoundingMode.HALF_UP).multiply(STREET_WIDTH_SNAP).subtract(minX);
         BigDecimal y = cs.getY().divide(STREET_Y_SNAP, RoundingMode.HALF_UP).multiply(STREET_HEIGHT_SNAP).subtract(minY);

         BufferedImage streetImage = getStreetImage(sa);

         tiles.add(new ImageTile(x.intValue(), y.intValue(), streetImage, false));
      }

      int snapW = STREET_WIDTH_SNAP.intValue();
      int snapH = STREET_HEIGHT_SNAP.intValue();

      List<ImageTile> newTiles = new ArrayList<>();
      for(ImageTile tile : tiles) {
         for(SideEnum side : SideEnum.values()) {
            if(!isOn(tile, tiles, side, false)) {
               int x = 0;
               int y = 0;
               switch (side) {
                  case LEFT:
                     x = tile.getX() - snapW;
                     y = tile.getY() - snapH;
                     break;
                  case TOP:
                     x = tile.getX() + snapW;
                     y = tile.getY() - snapH;
                     break;
                  case RIGHT:
                     x = tile.getX() + snapW;
                     y = tile.getY() + snapH;
                     break;
                  case BOTTOM:
                     x = tile.getX() - snapW;
                     y = tile.getY() + snapH;
                     break;
               }
               ImageTile newTile = new ImageTile(x, y, null, true);

               if(checkIfInside(x, y, maxX.intValue(), maxY.intValue(), minX.intValue(), minY.intValue(), tiles, new ArrayList<>())) {
                  newTile.setSea(false);
                  newTile.setImage(getEmptyTile());
                  newTiles.add(newTile);
               }
               else {
                  String imageName = "";
                  for (SideEnum side2 : SideEnum.values()) {
                     if (isOn(newTile, tiles, side2, false)) {
                        imageName += side2.getSide();
                     }
                  }
                  if (imageName.equals("")) {
                     imageName = "none";
                  }

                  imageName += ".png";
                  newTile.setImage(getSeaTile(imageName));
                  newTiles.add(newTile);
               }
            }
         }
      }

      tiles.addAll(newTiles);

      int snapWidth = STREET_WIDTH_SNAP.intValue();
      int snapHeight = STREET_HEIGHT_SNAP.divide(new BigDecimal("2"), RoundingMode.HALF_UP).intValue();

      boolean offset = !offset(width, height, tiles);
      for(int y = -(snapHeight * 8); y <= height.intValue() + (snapHeight * 2); y += (snapHeight * 2)) {
         for (int x = -(snapWidth * 4); x <= width.intValue() + (snapWidth * 2); x += (snapWidth * 2)) {
            if(offset) {
               int xx = x - snapWidth;
               int yy = y;
               if(!tilesContains(tiles, xx, yy, false, false)) {
                  tiles.add(new ImageTile(xx, yy, getSeaTile(), true));
               }
            }
            else {
               if(!tilesContains(tiles, x, y, false, false)) {
                  tiles.add(new ImageTile(x, y, getSeaTile(), true));
               }
            }
         }
         offset = !offset;
      }

      tiles = tiles.stream().sorted((o1, o2) -> o1.getY().compareTo(o2.getY())).collect(Collectors.toList());

      for(ImageTile tile : tiles) {
         if(tile.isSea()) {
            Graphics2D tileGraph = tile.getImage().createGraphics();
            if(isCityOnCorner(tile, tiles, SideEnum.LEFT) &&
                    isOn(tile, tiles, SideEnum.LEFT, true) &&
                    isOn(tile, tiles, SideEnum.BOTTOM, true)){
               tileGraph.drawImage(getCornerImage(SideEnum.LEFT), 0, 0, null);
            }
            if(isCityOnCorner(tile, tiles, SideEnum.RIGHT) &&
                    isOn(tile, tiles, SideEnum.RIGHT, true) &&
                    isOn(tile, tiles, SideEnum.TOP, true)){
               tileGraph.drawImage(getCornerImage(SideEnum.RIGHT), 0, 0, null);
            }
            if(isCityOnCorner(tile, tiles, SideEnum.TOP) &&
                    isOn(tile, tiles, SideEnum.LEFT, true) &&
                    isOn(tile, tiles, SideEnum.TOP, true)){
               tileGraph.drawImage(getCornerImage(SideEnum.TOP), 0, 0, null);
            }
            if(isCityOnCorner(tile, tiles, SideEnum.BOTTOM) &&
                    isOn(tile, tiles, SideEnum.RIGHT, true) &&
                    isOn(tile, tiles, SideEnum.BOTTOM, true)){
               tileGraph.drawImage(getCornerImage(SideEnum.BOTTOM), 0, 0, null);
            }
         }
      }

      for(ImageTile tile : tiles) {
         graph.drawImage(tile.getImage(), tile.getX() + imageBorder, tile.getY() + imageBorder, null);
      }

      File itemsFolder = new File(getClass().getClassLoader().getResource("sea_tiles/sea_items").getFile());
      List<File> files = Arrays.asList(itemsFolder.listFiles());
      files = files.stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).collect(Collectors.toList());
      int i = files.size();
      List<SeaItemProvider.Item> items = new ArrayList<>();
      for(File f : files) {
         SeaItemProvider.Item item = new SeaItemProvider.Item(i, f);
         items.add(item);
         i--;
      }

      List<SeaItemProvider.Item> selectedItems = new ArrayList<>();

      if(withTraits) {
         List<ImageTile> seaTiles = new ArrayList<>();
         for (ImageTile tile : tiles) {
            if (tile.isSea() && tileIsInImage(tile, width.intValue(), height.intValue()) &&
                !alreadyContains(tile, seaTiles)) {
               seaTiles.add(tile);
            }
         }
         Collections.shuffle(seaTiles);
         List<ImageTile> filtered = new ArrayList<>();
         for(ImageTile sea : seaTiles) {
            if(!alreadyContains(sea, filtered)) {
               filtered.add(sea);
            }
         }

         filtered = filtered.subList(0, Math.min(Math.max(city.getStreets().size() / 2, 1), 30));

         Set<String> usedItems = new HashSet<>();
         for (ImageTile tile : filtered) {
            Pair<BufferedImage, SeaItemProvider.Item> image = getRandomSeaItem(items);
            String fileName = image.getSecond().getFile().getName();
            while(usedItems.contains(fileName)) {
               image = getRandomSeaItem(items);
               fileName = image.getSecond().getFile().getName();
            }
            usedItems.add(image.getSecond().getFile().getName());
            selectedItems.add(image.getSecond());
            System.out.println("Drawing " + image.getSecond() + " to " + tile.getX() + ", " + tile.getY());
            graph.drawImage(image.getFirst(), tile.getX() + imageBorder, tile.getY() + imageBorder, null);
         }
      }

      String cityName = city.getName();
      if(cityName == null || cityName.trim().isEmpty()) {
         cityName = "No Name";
      }
      cityName = cityName + UUID.randomUUID() + ".png";
      String hash = pinataService.uploadFile(cityName, cityImage);

      //String hash = "hash";
      //File cityImageFile = new File("mesta/" + cityName);
      //ImageIO.write(cityImage, "png", cityImageFile);

      NftProperties nft = createJson(hash, city, selectedItems);

      String jsonHash = pinataService.uploadJson(nft);

      return Pair.of(hash, jsonHash);
   }

   private NftProperties createJson(String hash, City city, List<SeaItemProvider.Item> items) {
      NftProperties nft = new NftProperties();
      nft.setFile("ipfs://ipfs/" + hash);
      nft.setDescription("Hathor Streets City " + city.getName());
      nft.setName("Hathor Streets City " + city.getName());
      nft.setAttributes(new ArrayList<>());

      for(CityStreet street : city.getStreets()) {
         NftAttribute attr = new NftAttribute("Hathor Street", "Number " + street.getStreetId());
         nft.getAttributes().add(attr);
      }

      for(SeaItemProvider.Item item : items) {
         NftAttribute attr = new NftAttribute("Sea Item", item.getName());
         nft.getAttributes().add(attr);
      }

      return nft;
   }

   private boolean alreadyContains(ImageTile tile, List<ImageTile> tiles) {
      boolean contains = false;
      for(ImageTile t : tiles) {
         if(t.getX().equals(tile.getX()) && t.getY().equals(tile.getY())){
            contains = true;
            break;
         }
      }
      return contains;
   }

   private boolean tileIsInImage(ImageTile tile, int width, int height) {
      return tile.getX() >= -imageBorder && tile.getY() >= -imageBorder &&
              tile.getX() <= ((width + imageBorder * 2) - STREET_WIDTH.intValue()) &&
              tile.getY() <= ((height + imageBorder * 2) - STREET_HEIGHT.intValue());
   }
   private Pair<BufferedImage, SeaItemProvider.Item> getRandomSeaItem(List<SeaItemProvider.Item> items) throws Exception {
      SeaItemProvider.Item item = seaItemProvider.chooseOnWeight(items);

      BufferedImage image = new BufferedImage(STREET_WIDTH.intValue(), STREET_HEIGHT.intValue(), BufferedImage.TYPE_INT_ARGB);
      InputStream inputStream = new FileInputStream(item.getFile());
      Graphics2D graph = image.createGraphics();
      graph.drawImage(ImageIO.read(inputStream), 0, 0, null);

      return Pair.of(image, item);
   }

   private boolean checkIfInside(int x, int y, int maxX, int maxY, int minX, int minY, List<ImageTile> tiles, List<ImageTile> processed) {
      int snapW = STREET_WIDTH_SNAP.intValue();
      int snapH = STREET_HEIGHT_SNAP.intValue();

      for(ImageTile tile : processed) {
         if(tile.getX() == x && tile.getY() == y) {
            return true;
         }
      }

      boolean result = true;
      ImageTile tile = new ImageTile(x, y, null, false);
      if(x >= maxX || y >= maxY || x <= minX || y <= minY) {
         return false;
      }

      processed.add(new ImageTile(x, y, null, false));

      if(!isOn(tile, tiles, SideEnum.LEFT, false)) {
         result = result && checkIfInside(x - snapW, y - snapH, maxX, maxY, minX, minY, tiles, processed);
      }
      if(!isOn(tile, tiles, SideEnum.TOP, false)) {
         result = result && checkIfInside(x + snapW, y - snapH, maxX, maxY, minX, minY, tiles, processed);
      }
      if(!isOn(tile, tiles, SideEnum.RIGHT, false)) {
         result = result && checkIfInside(x + snapW, y + snapH, maxX, maxY, minX, minY, tiles, processed);
      }
      if(!isOn(tile, tiles, SideEnum.BOTTOM, false)) {
         result = result && checkIfInside(x - snapW, y + snapH, maxX, maxY, minX, minY, tiles, processed);
      }
      return result;
   }

   private BufferedImage getSeaTile() throws Exception {
      BufferedImage empty = new BufferedImage(STREET_WIDTH.intValue(), STREET_HEIGHT.intValue(), BufferedImage.TYPE_INT_ARGB);
      String fileName = "sea_tiles/none.png";
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
      Graphics2D graph = empty.createGraphics();
      graph.drawImage(ImageIO.read(inputStream), 0, 0, null);

      return empty;
   }

   private boolean offset(BigDecimal width, BigDecimal height, List<ImageTile> tiles){
      int snapWidth = STREET_WIDTH_SNAP.intValue();
      int snapHeight = STREET_HEIGHT_SNAP.divide(new BigDecimal("2"), RoundingMode.HALF_UP).intValue();

      for(boolean offset : new boolean[] {true, false}) {
         for (int y = -(snapHeight * 4); y <= height.intValue(); y += (snapHeight * 2)) {
            for (int x = 0; x <= width.intValue(); x += (snapWidth * 2)) {
               if (offset) {
                  int xx = x - snapWidth;
                  int yy = y;
                  if (tilesContains(tiles, xx, yy, false, false)) {
                     return offset;
                  }
               } else {
                  if (tilesContains(tiles, x, y, false, false)) {
                     return offset;
                  }
               }
            }
            offset = !offset;
         }
      }

      throw new IllegalArgumentException("Error");
   }

   private BufferedImage getCornerImage(SideEnum side) throws Exception {
      String imageName = "none.png";

      switch (side) {
         case LEFT:
            imageName = "left_corner.png";
            break;
         case TOP:
            imageName = "top_corner.png";
            break;
         case RIGHT:
            imageName = "right_corner.png";
            break;
         case BOTTOM:
            imageName = "bottom_corner.png";
            break;
      }

      BufferedImage corner = new BufferedImage(STREET_WIDTH.intValue(), STREET_HEIGHT.intValue(), BufferedImage.TYPE_INT_ARGB);
      String fileName = "sea_tiles/" + imageName;
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
      Graphics2D graph = corner.createGraphics();
      graph.drawImage(ImageIO.read(inputStream), 0, 0, null);

      return corner;
   }

//   private BufferedImage getSeaTile(List<ImageTile> tiles, int x, int y) throws Exception {
//      ImageTile tile = new ImageTile(x, y, null);
//      String imageName = "";
//      int sides = 0;
//      for(SideEnum side : SideEnum.values()) {
//         if (isOn(tile, tiles, side)) {
//            imageName += side.getSide();
//            sides++;
//         }
//      }
//
//      if(sides >= 2){
//         imageName = "small" + imageName + ".png";
//      }
//      else {
//         imageName = "none.png";
//      }
//
//      BufferedImage empty = new BufferedImage(STREET_WIDTH.intValue(), STREET_HEIGHT.intValue(), BufferedImage.TYPE_INT_ARGB);
//      String fileName = "sea_tiles/" + imageName;
//      InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
//      Graphics2D graph = empty.createGraphics();
//      graph.drawImage(ImageIO.read(inputStream), 0, 0, null);
//
//      return empty;
//   }

   private boolean isOn(ImageTile tile, List<ImageTile> tiles, SideEnum side, boolean checkSea) {
      int snapW = STREET_WIDTH_SNAP.intValue();
      int snapH = STREET_HEIGHT_SNAP.intValue();

      int x = 0;
      int y = 0;
      switch (side) {
         case LEFT:
            x = tile.getX() - snapW;
            y = tile.getY() - snapH;
            break;
         case TOP:
            x = tile.getX() + snapW;
            y = tile.getY() - snapH;
            break;
         case RIGHT:
            x = tile.getX() + snapW;
            y = tile.getY() + snapH;
            break;
         case BOTTOM:
            x = tile.getX() - snapW;
            y = tile.getY() + snapH;
            break;
      }

      return tilesContains(tiles, x, y, false, checkSea);
   }

   private boolean isCityOnCorner(ImageTile tile, List<ImageTile> tiles, SideEnum side) {
      int snapW = STREET_WIDTH_SNAP.intValue() * 2;
      int snapH = STREET_HEIGHT_SNAP.intValue() * 2;

      int x = 0;
      int y = 0;
      switch (side) {
         case LEFT:
            x = tile.getX() - snapW;
            y = tile.getY();
            break;
         case TOP:
            x = tile.getX();
            y = tile.getY() - snapH;
            break;
         case RIGHT:
            x = tile.getX() + snapW;
            y = tile.getY();
            break;
         case BOTTOM:
            x = tile.getX();
            y = tile.getY() + snapH;
            break;
      }

      return tilesContains(tiles, x, y, true, false);
   }

   private boolean tilesContains(List<ImageTile> tiles, int x, int y, boolean checkCity, boolean checkSea) {
      for(ImageTile tile : tiles) {
         if(tile.getX() == x && tile.getY() == y) {
            if(checkCity) {
               if (!tile.isSea()) {
                  return true;
               }
            }
            else if(checkSea){
               if(tile.isSea()) {
                  return true;
               }
            }
            else {
               return true;
            }
         }
      }
      return false;
   }

   private BufferedImage getEmptyTile() throws Exception {
      Random rand = new Random();
      float f = rand.nextFloat();

      BufferedImage empty = new BufferedImage(STREET_WIDTH.intValue(), STREET_HEIGHT.intValue(), BufferedImage.TYPE_INT_ARGB);
      String fileName = "sea_tiles/trees1.png";
      if(f < 0.5) {
         fileName = "sea_tiles/trees2.png";
      }
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
      Graphics2D graph = empty.createGraphics();
      graph.drawImage(ImageIO.read(inputStream), 0, 0, null);

      return empty;
   }

   private BufferedImage getSeaTile(String name) throws Exception {
      BufferedImage empty = new BufferedImage(STREET_WIDTH.intValue(), STREET_HEIGHT.intValue(), BufferedImage.TYPE_INT_ARGB);
      String fileName = "sea_tiles/" + name;

      InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
      Graphics2D graph = empty.createGraphics();
      graph.drawImage(ImageIO.read(inputStream), 0, 0, null);

      return empty;
   }

   private BufferedImage getStreetImage(StreetAttributes sa) throws Exception {
      String topImage = ImageMaps.mapTop.get(sa.getTopQuad()) + ".png";
      String roadImage = ImageMaps.mapRoad.get(sa.getRoad()) + ".png";
      String leftImage = ImageMaps.mapLeft.get(sa.getLeftQuad()) + ".png";
      String rightImage = ImageMaps.mapRight.get(sa.getRightQuad()) + ".png";
      String bottomImage = ImageMaps.mapBottom.get(sa.getBottomQuad()) + ".png";
      String billboardImage = ImageMaps.mapBillboard.get(sa.getBillboard()) + ".png";
      String specialImage = ImageMaps.mapSpecial.get(sa.getSpecial()) + ".png";
      List<String> layers = new ArrayList<>();
      layers.add("top/" + topImage);
      layers.add("road/" + roadImage);
      layers.add("left/" + leftImage);
      layers.add("right/" + rightImage);
      layers.add("bottom/" + bottomImage);
      layers.add("billboard/" + billboardImage);
      layers.add("special/" + specialImage);

      BufferedImage street = new BufferedImage(STREET_WIDTH.intValue(), STREET_HEIGHT.intValue(), BufferedImage.TYPE_INT_ARGB);

      Graphics2D graph = street.createGraphics();

      for(String layer : layers) {
         InputStream inputStream = getClass().getClassLoader().getResourceAsStream("generator/layers/" + layer);
         graph.drawImage(ImageIO.read(inputStream), 0, 0, null);
      }

      return street;
   }
}
