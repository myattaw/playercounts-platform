package net.playercounts.pollworker.repository;

import net.playercounts.models.entity.TrackedServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackedServerRepository extends JpaRepository<TrackedServer, Long> {

    List<TrackedServer> findByActiveTrue();

}