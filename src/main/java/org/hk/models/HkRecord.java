package org.hk.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HkRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column
    private String dt;
    @Column
    private String kt;
    @Column
    private String doc;
    @Column
    private LocalDate date;
    @Column
    private LocalDateTime dateTime;
    @Column
    private double count;
    @Column
    private double sum;
    @Column
    private String warehouseFrom;
    @Column
    private String warehouseTo;
    @Column
    private String product;
    @Column
    private boolean isBladder;
    @Column
    private String content1;
    @Column
    private String content4;
}
