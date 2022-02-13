package com.hathor.daemon.data.repositories;

import com.hathor.daemon.data.entities.City;
import com.hathor.daemon.data.entities.Mint;
import com.hathor.daemon.data.entities.NftCity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface NftCityRepository extends CrudRepository<NftCity, String> {

   Optional<NftCity> findCityNftByCity(City city);

   List<NftCity> getAllByState(int state);

}
