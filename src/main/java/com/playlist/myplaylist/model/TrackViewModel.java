package com.playlist.myplaylist.model;

// Using lombok for boilerplate code reduction
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackViewModel {
    private String id;
    private String name;
    private String artists;
    private String duration;
    private int trackNumber;
}
