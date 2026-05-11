package net.playercounts.apigateway.repository.admin;

import net.playercounts.models.entity.TrackedServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackedServerRepository extends JpaRepository<TrackedServer, Long> {

    Optional<TrackedServer> findByAddress(String address);

}