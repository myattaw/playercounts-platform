package net.playercounts.statusconsumer.repository;

import net.playercounts.models.ServerLatestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerLatestStatusRepository extends JpaRepository<ServerLatestStatus, String> {
}