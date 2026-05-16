package net.playercounts.apigateway.service;

import net.playercounts.apigateway.repository.HistoricalTelemetryRepository;
import net.playercounts.apigateway.repository.row.ServerAggregateRow;
import net.playercounts.models.snapshot.TopServerSnapshot;
import net.playercounts.models.snapshot.graph.GraphHistoryPoint;
import net.playercounts.models.snapshot.graph.GraphServerSnapshot;
import net.playercounts.models.snapshot.platform.PlatformDashboardSnapshot;
import net.playercounts.models.snapshot.platform.PlatformOverviewSnapshot;
import net.playercounts.models.snapshot.server.SelectableServerSnapshot;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlatformTelemetryService {

    private static final String[] COLORS = {
            "#f59e0b",
            "#10b981",
            "#3b82f6",
            "#8b5cf6",
            "#ec4899",
            "#14b8a6",
            "#f97316",
            "#06b6d4",
            "#a855f7",
            "#84cc16"
    };

    private final HistoricalTelemetryRepository repository;

    private volatile PlatformDashboardSnapshot cachedDashboard;

    public PlatformTelemetryService(HistoricalTelemetryRepository repository) {
        this.repository = repository;
        this.cachedDashboard = buildDashboard();
    }

    public PlatformDashboardSnapshot getDashboard() {
        return cachedDashboard;
    }

    @Scheduled(fixedRate = 5000)
    public void refreshDashboard() {
        this.cachedDashboard = buildDashboard();
    }

    public GraphServerSnapshot getGraphServer(String address) {
        List<ServerAggregateRow> aggregates = repository.getServerAggregates();

        ServerAggregateRow row = aggregates.stream()
                .filter(server -> server.address().equalsIgnoreCase(address))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Server not found: " + address));

        Map<String, List<GraphHistoryPoint>> histories =
                repository.getGraphHistories(List.of(row.address()));

        return toGraphServerSnapshot(
                row,
                histories.getOrDefault(row.address(), List.of()),
                0,
                repository.getServerIcon(row.address())
        );
    }

    private PlatformDashboardSnapshot buildDashboard() {
        List<ServerAggregateRow> aggregates = repository.getServerAggregates();

        List<ServerAggregateRow> byPeak = aggregates.stream()
                .sorted(Comparator.comparingInt(ServerAggregateRow::peakPlayers).reversed())
                .toList();

        List<ServerAggregateRow> graphRows = byPeak.stream()
                .limit(8)
                .toList();

        List<String> graphAddresses = graphRows.stream()
                .map(ServerAggregateRow::address)
                .toList();

        Map<String, List<GraphHistoryPoint>> histories = repository.getGraphHistories(graphAddresses);
        Map<String, String> icons = repository.getServerIcons(
                aggregates.stream().map(ServerAggregateRow::address).toList()
        );

        PlatformOverviewSnapshot overview = buildOverview(aggregates);

        List<GraphServerSnapshot> graphServers = new ArrayList<>();
        for (int i = 0; i < graphRows.size(); i++) {
            ServerAggregateRow row = graphRows.get(i);
            graphServers.add(toGraphServerSnapshot(
                    row,
                    histories.getOrDefault(row.address(), List.of()),
                    i + 1,
                    icons.get(row.address())
            ));
        }

        return new PlatformDashboardSnapshot(
                overview,
                graphServers,
                buildSelectableServers(byPeak, icons),
                buildTopServers(aggregates, SortMode.LIVE),
                buildTopServers(aggregates, SortMode.PEAK),
                buildTopServers(aggregates, SortMode.TRENDING)
        );
    }

    private PlatformOverviewSnapshot buildOverview(List<ServerAggregateRow> rows) {
        int trackedServers = rows.size();

        int totalCurrentPlayers = rows.stream()
                .mapToInt(ServerAggregateRow::currentPlayers)
                .sum();

        int avgCurrentPlayers = trackedServers == 0
                ? 0
                : (int) Math.round((double) totalCurrentPlayers / trackedServers);

        ServerAggregateRow largest = rows.stream()
                .max(Comparator.comparingInt(ServerAggregateRow::currentPlayers))
                .orElse(null);

        return new PlatformOverviewSnapshot(
                trackedServers,
                totalCurrentPlayers,
                avgCurrentPlayers,
                repository.getTotalTelemetryPoints(),
                largest == null ? null : largest.address(),
                largest == null ? 0 : largest.currentPlayers()
        );
    }

    private List<SelectableServerSnapshot> buildSelectableServers(
            List<ServerAggregateRow> byPeak,
            Map<String, String> icons
    ) {
        List<SelectableServerSnapshot> result = new ArrayList<>();

        for (int i = 0; i < Math.min(1000, byPeak.size()); i++) {
            ServerAggregateRow row = byPeak.get(i);

            result.add(new SelectableServerSnapshot(
                    row.address(),
                    row.currentPlayers(),
                    row.peakPlayers(),
                    i + 1,
                    icons.get(row.address())
            ));
        }

        return result;
    }

    private List<TopServerSnapshot> buildTopServers(List<ServerAggregateRow> rows, SortMode mode) {
        Comparator<ServerAggregateRow> comparator = switch (mode) {
            case LIVE -> Comparator.comparingInt(ServerAggregateRow::currentPlayers).reversed();
            case PEAK -> Comparator.comparingInt(ServerAggregateRow::peakPlayers).reversed();
            case TRENDING -> Comparator.comparingDouble(ServerAggregateRow::trendingScore).reversed();
        };

        return rows.stream()
                .sorted(comparator)
                .limit(10)
                .map(row -> new TopServerSnapshot(
                        row.address(),
                        row.currentPlayers(),
                        row.peakPlayers(),
                        row.avgPlayers()
                ))
                .collect(Collectors.toList());
    }

    private GraphServerSnapshot toGraphServerSnapshot(
            ServerAggregateRow row,
            List<GraphHistoryPoint> history,
            int rank,
            String iconBase64
    ) {

        int colorIndex = rank > 0
                ? (rank - 1) % COLORS.length
                : Math.abs(row.address().hashCode()) % COLORS.length;

        return new GraphServerSnapshot(
                row.address(),
                row.currentPlayers(),
                row.peakPlayers(),
                row.growth24hPercent(),
                row.growth7dPercent(),
                row.growth30dPercent(),
                history,
                COLORS[colorIndex],
                rank,
                iconBase64
        );
    }

    private enum SortMode {
        LIVE,
        PEAK,
        TRENDING
    }
}