package de.ruben.xcore.job.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.Document;
import org.checkerframework.checker.index.qual.NegativeIndexFor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Job {
    private Integer level;
    private Double curentXP;
    private Integer prestige;
}
