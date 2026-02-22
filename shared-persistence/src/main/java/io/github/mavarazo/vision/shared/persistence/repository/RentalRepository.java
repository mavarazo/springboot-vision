package io.github.mavarazo.vision.shared.persistence.repository;

import io.github.mavarazo.vision.shared.persistence.entity.Rental;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RentalRepository extends CrudRepository<Rental, UUID> {
}
