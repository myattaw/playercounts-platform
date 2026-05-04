package net.playercounts.models.snapshot.platform;

import java.util.List;

public record GraphServerSnapshot(String address, int currentPlayers, int peakPlayers, List<GraphHistoryPoint> history,
                                  String color, int rank) {


}