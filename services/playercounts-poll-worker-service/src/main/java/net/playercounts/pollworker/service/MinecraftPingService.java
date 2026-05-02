package net.playercounts.pollworker.service;

import net.playercounts.pollworker.model.MinecraftPingResult;
import org.geysermc.mcprotocollib.network.BuiltinFlags;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.factory.ClientNetworkSessionFactory;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.session.ClientNetworkSession;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.packet.status.clientbound.ClientboundStatusResponsePacket;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class MinecraftPingService {

    public MinecraftPingResult ping(String host, int port) {
        CompletableFuture<MinecraftPingResult> future = new CompletableFuture<>();
        long start = System.currentTimeMillis();

        try {
            MinecraftProtocol protocol = new MinecraftProtocol();

            ClientNetworkSession session = ClientNetworkSessionFactory.factory()
                    .setAddress(host, port)
                    .setProtocol(protocol)
                    .create();

            session.setFlag(BuiltinFlags.READ_TIMEOUT, 5000);

            session.addListener(new SessionAdapter() {

                @Override
                public void packetReceived(Session session, Packet packet) {
                    if (packet instanceof ClientboundStatusResponsePacket responsePacket) {
                        try {
                            ServerStatusInfo info = responsePacket.parseInfo();

                            int online = 0;
                            int max = 0;

                            PlayerInfo playerInfo = info.getPlayerInfo();
                            if (playerInfo != null) {
                                online = playerInfo.getOnlinePlayers();
                                max = playerInfo.getMaxPlayers();
                            }

                            long latency = System.currentTimeMillis() - start;

                            future.complete(new MinecraftPingResult(true, online, max, latency));
                        } catch (Exception e) {
                            future.complete(new MinecraftPingResult(false, 0, 0, -1));
                        } finally {
                            session.disconnect("Ping complete");
                        }
                    }
                }

                @Override
                public void disconnected(DisconnectedEvent event) {
                    if (!future.isDone()) {
                        future.complete(new MinecraftPingResult(false, 0, 0, -1));
                    }
                }
            });

            session.connect(false);

            return future.get(6, TimeUnit.SECONDS);

        } catch (Exception e) {
            return new MinecraftPingResult(false, 0, 0, -1);
        }
    }
}