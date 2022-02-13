package com.hathor.daemon.services;

import com.hathor.daemon.data.entities.City;
import com.hathor.daemon.data.entities.CityStreet;
import com.hathor.daemon.data.entities.Street;
import com.hathor.daemon.data.entities.StreetAttributes;
import com.hathor.daemon.data.repositories.CityRepository;
import com.hathor.daemon.data.repositories.StreetRepository;
import com.hathor.daemon.services.image.ImageMaps;
import com.hathor.daemon.services.image.ImageTile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ImageService {

   private final CityRepository cityRepository;
   private final StreetRepository streetRepository;
   private final PinataService pinataService;

   private final BigDecimal STREET_WIDTH = new BigDecimal("1720");
   private final BigDecimal STREET_WIDTH_SNAP = new BigDecimal("859");

   private final BigDecimal STREET_HEIGHT = new BigDecimal("1445");
   private final BigDecimal STREET_HEIGHT_SNAP = new BigDecimal("496");

   private final BigDecimal STREET_X_SNAP = new BigDecimal("249.6");
   private final BigDecimal STREET_Y_SNAP = new BigDecimal("144");

   public ImageService(CityRepository cityRepository, StreetRepository streetRepository, PinataService pinataService) {
      this.cityRepository = cityRepository;
      this.streetRepository = streetRepository;
      this.pinataService = pinataService;
   }

//   @PostConstruct
//   public void init() {
//      City c = cityRepository.findById("1a2dffbd-fb77-44d3-871e-0b0551f5c92e").get();
//      try {
//         createImage(c);
//      } catch (Exception ex){
//         ex.printStackTrace();
//      }
//   }

   public String createImage(City city) throws Exception {
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

      BufferedImage cityImage = new BufferedImage(width.intValue(), height.intValue(), BufferedImage.TYPE_INT_ARGB);
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

      int snapWidth = STREET_WIDTH_SNAP.intValue();
      int snapHeight = STREET_HEIGHT_SNAP.divide(new BigDecimal("2"), RoundingMode.HALF_UP).intValue();

      boolean offset = false;
      for(int y = -(snapHeight * 4); y <= height.intValue(); y += (snapHeight * 2)) {
         for (int x = 0; x <= width.intValue(); x += (snapWidth * 2)) {
            if(offset) {
               int xx = x - snapWidth;
               int yy = y;
               if(!tilesContains(tiles, xx, yy)) {
                  //tiles.add(new ImageTile(xx, yy, getEmptyTile()));
               }
            }
            else {
               if(!tilesContains(tiles, x, y)) {
                  //tiles.add(new ImageTile(x, y, getEmptyTile()));
               }
            }
         }
         offset = !offset;
      }

      tiles = tiles.stream().sorted((o1, o2) -> o1.getY().compareTo(o2.getY())).collect(Collectors.toList());
      for(ImageTile tile : tiles) {
         graph.drawImage(tile.getImage(), tile.getX(), tile.getY(), null);
      }

      String cityName = city.getName();
      if(cityName == null || cityName.trim().isEmpty()) {
         cityName = "No Name";
      }
      cityName = cityName + ".png";
      String hash = pinataService.uploadFile(cityName, cityImage);
      //String hash = "hash";

      //File cityImageFile = new File(cityName);
      //ImageIO.write(cityImage, "png", cityImageFile);

      return hash;
   }

   private boolean tilesContains(List<ImageTile> tiles, int x, int y) {
      for(ImageTile tile : tiles) {
         if(tile.getX() == x && tile.getY() == y) {
            return true;
         }
      }
      return false;
   }

   private BufferedImage getEmptyTile() throws Exception {
      Random rand = new Random();
      float f = rand.nextFloat();

      BufferedImage empty = new BufferedImage(STREET_WIDTH.intValue(), STREET_HEIGHT.intValue(), BufferedImage.TYPE_INT_ARGB);
      String fileName = "generator/layers/trees.png";
      if(f < 0.2) {
         fileName = "generator/layers/trees_lake.png";
      }
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
