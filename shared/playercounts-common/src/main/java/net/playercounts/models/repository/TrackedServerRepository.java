package net.playercounts.models.repository;

import net.playercounts.models.entity.TrackedServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackedServerRepository extends JpaRepository<TrackedServer, Long> {

    Optional<TrackedServer> findByAddress(String address);

    List<TrackedServer> findByActiveTrue();


}