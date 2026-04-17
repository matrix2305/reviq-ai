package com.reviq.shared.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reviq.shared.search.enums.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SortRequest implements Serializable {

    private String key;
    private SortDirection direction;
}
