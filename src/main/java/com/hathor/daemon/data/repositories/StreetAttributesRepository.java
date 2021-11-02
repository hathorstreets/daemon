package com.hathor.daemon.data.repositories;

import com.hathor.daemon.data.entities.Street;
import com.hathor.daemon.data.entities.StreetAttributes;
import org.springframework.data.repository.CrudRepository;

public interface StreetAttributesRepository extends CrudRepository<StreetAttributes, Integer> {
}
