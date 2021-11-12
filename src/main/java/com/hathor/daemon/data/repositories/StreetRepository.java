package com.hathor.daemon.data.repositories;

import com.hathor.daemon.data.entities.Address;
import com.hathor.daemon.data.entities.Street;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StreetRepository extends CrudRepository<Street, Integer> {

   @Query(nativeQuery = true, value = "SELECT * FROM street s WHERE s.taken = 0 order by RAND() LIMIT :count")
   List<Street> findNotTaken(@Param("count") int count);

   List<Street> findByTakenAndStreetAttributesBillboard(boolean taken, String billboard);
}
