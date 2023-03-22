package org.hk.models;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class HkRecordMainValue {
    private LocalDate date;
    private String dt;
    private String kt;
    private String[] recordContent;
    private boolean isBladder;
}