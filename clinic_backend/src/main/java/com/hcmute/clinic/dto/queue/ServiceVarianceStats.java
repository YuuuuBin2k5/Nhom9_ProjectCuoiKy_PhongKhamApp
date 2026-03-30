package com.hcmute.clinic.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceVarianceStats {
    private Long serviceId;
    private Double mean;
    private Double stdDev;
    private Integer median;
    private Integer p75;
    private Integer p90;
    private Integer sampleSize;
    private String confidenceLevel; // HIGH, MEDIUM, LOW

    public static ServiceVarianceStats defaultStats() {
        return ServiceVarianceStats.builder()
                .mean(15.0)
                .stdDev(5.0)
                .median(15)
                .p75(20)
                .p90(25)
                .sampleSize(0)
                .confidenceLevel("LOW")
                .build();
    }
}
