package net.playercounts.apigateway.repository.admin;

import net.playercounts.models.entity.ServerTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<ServerTag, Long> {

    Optional<ServerTag> findByNameIgnoreCase(
            String name
    );

}