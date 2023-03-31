package edu.uci.ics.asterix.result.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/*
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "Name")*/

public class Nested implements IMetadataJson {
    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("primaryKey")
    private List<String> primaryKey;

    @JsonProperty("nestedFields")
    private List<Nested> nestedFields;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(List<String> primaryKeys) {
        this.primaryKey = primaryKeys;
    }

    public List<Nested> getNested() {
        return nestedFields;
    }

    public void setNested(List<Nested> Nested) {
        this.nestedFields = nestedFields;
    }
}