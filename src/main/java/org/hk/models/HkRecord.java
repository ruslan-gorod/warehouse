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
    @Column(name = "dt")
    private String dt;
    @Column(name = "kt")
    private String kt;
    @Column(name = "doc")
    private String doc;
    @Column(name = "date")
    private LocalDate date;
    @Column(name = "datetime")
    private LocalDateTime dateTime;
    @Column(name = "count")
    private double count;
    @Column(name = "sum")
    private double sum;
    @Column(name = "warehouseFrom")
    private String warehouseFrom;
    @Column(name = "warehouseTo")
    private String warehouseTo;
    @Column(name = "product")
    private String product;
    @Column(name = "isBladder")
    private boolean isBladder;
    @Column(name = "content1")
    private String content1;
    @Column(name = "content4")
    private String content4;
}
