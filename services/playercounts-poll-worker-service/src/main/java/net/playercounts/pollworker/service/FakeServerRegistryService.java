package net.playercounts.pollworker.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FakeServerRegistryService {

    public List<String> getTrackedServers() {
        return List.of(
                "hypixel.net",
                "mineplex.com",
                "cubecraft.net",
                "manacube.net",
                "purpleprison.net",
                "complexmc.net",
                "performium.net",
                "insanitycraft.net",
                "jartexnetwork.com",
                "snapcraft.net"
        );
    }
}