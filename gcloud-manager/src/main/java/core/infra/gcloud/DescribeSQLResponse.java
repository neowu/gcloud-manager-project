package core.infra.gcloud;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author neo
 */
public class DescribeSQLResponse {
    @JsonProperty("ipAddresses")
    public List<IPAddress> addresses;

    public String publicIP() {
        return addresses.stream().filter(address -> "PRIMARY".equals(address.type)).findFirst().orElseThrow().address;
    }

    public String privateIP() {
        return addresses.stream().filter(address -> "PRIVATE".equals(address.type)).findFirst().orElseThrow().address;
    }

    public static class IPAddress {
        @JsonProperty("ipAddress")
        public String address;
        @JsonProperty("type")
        public String type;
    }
}
