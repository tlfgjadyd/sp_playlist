package com.playlist.myplaylist.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UI용 트랙의 간소화된 보기를 나타내는 데이터 전송 객체
 */
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
