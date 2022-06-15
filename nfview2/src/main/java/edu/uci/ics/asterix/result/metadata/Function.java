package edu.uci.ics.asterix.result.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Function implements IMetadataJson {
    //We need this to bind the capitalized field names to the member dataverseName
    @JsonProperty("DataverseName")
    private String dataverseName;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Arity")
    private String arity;

    @JsonProperty("Params")
    private List<String> Params;

    @JsonProperty("Definition")
    private String Definition;

    @JsonProperty("Dependencies")
    private List<List<List<String>>> Dependencies;

    public String getDataverseName() {
        return dataverseName;
    }

    public void setDataverseName(String dataverseName) {
        this.dataverseName = dataverseName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArity() {
        return arity;
    }

    public void setArity(String arity) {
        this.arity = arity;
    }

    public List<String> getParams() {
        return Params;
    }

    public void setParams(List<String> params) {
        Params = params;
    }

    public String getDefinition() {
        return Definition;
    }

    public void setDefinition(String definition) {
        Definition = definition;
    }

    public List<List<List<String>>> getDependencies() {
        return Dependencies;
    }

    public void setDependencies(List<List<List<String>>> dependencies) {
        Dependencies = dependencies;
    }
}