package net.playercounts.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tracked_servers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackedServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String address;

    @Column(nullable = false)
    private String displayName;

    @ManyToMany
    @JoinTable(
            name = "tracked_server_tags",
            joinColumns = @JoinColumn(name = "server_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<ServerTag> tags = new ArrayList<>();

    @Column(nullable = false)
    private String color;

    @Column(columnDefinition = "BYTEA")
    private byte[] icon;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int currentPlayers = 0;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int maxPlayerCount = 0;

    private long createdAt;

    private long updatedAt;

}