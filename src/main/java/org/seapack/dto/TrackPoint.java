package org.seapack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackPoint {
    private int x; // X坐标
    private long t; // 时间戳（相对于开始时间）
}
