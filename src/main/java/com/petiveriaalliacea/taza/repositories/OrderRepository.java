package com.petiveriaalliacea.taza.repositories;

import com.petiveriaalliacea.taza.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findById(Long id);

//    List<Order> findByUserId(Long id);
//
//    List<Order> findByCompanyId(Long id);

}
