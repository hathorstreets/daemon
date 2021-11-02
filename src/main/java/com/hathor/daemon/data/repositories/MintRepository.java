package com.hathor.daemon.data.repositories;

import com.hathor.daemon.data.entities.Address;
import com.hathor.daemon.data.entities.Mint;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MintRepository extends CrudRepository<Mint, String> {

   List<Mint> getAllByState(int state);

}
