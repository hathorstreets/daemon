package com.hathor.daemon.data.repositories;

import com.hathor.daemon.data.entities.Address;
import com.hathor.daemon.data.entities.Street;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AddressRepository extends CrudRepository<Address, Integer> {

   Address findTopByTaken(boolean taken);
}
