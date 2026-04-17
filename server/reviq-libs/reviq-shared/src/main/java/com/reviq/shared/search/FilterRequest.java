package com.reviq.shared.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reviq.shared.search.enums.FieldType;
import com.reviq.shared.search.enums.Operator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FilterRequest implements Serializable {

    private String key;
    private Operator operator;
    private FieldType fieldType;
    private transient Object value;
    private transient Object valueTo;
    private transient List<Object> values;

    public List<Object> getValues() {
        if (Objects.isNull(this.values)) return new ArrayList<>();
        return this.values;
    }
}
