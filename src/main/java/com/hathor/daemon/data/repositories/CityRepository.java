package com.hathor.daemon.data.repositories;

import com.hathor.daemon.data.entities.City;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CityRepository extends CrudRepository<City, String> {

   City findByShareId(String shareId);

   List<City> findAllByRequestImage(boolean requestImage);
}
