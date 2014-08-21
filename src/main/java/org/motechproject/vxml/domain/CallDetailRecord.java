package org.motechproject.vxml.domain;

import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Entity
public class CallDetailRecord {

    //todo: see if we can get that from MDS
    private static final int MAX_ENTITY_STRING_LENGTH = 255;

    private static Logger logger = LoggerFactory.getLogger(CallDetailRecord.class);

    @Field(required = true)
    private String timestamp;

    @Field
    private String configName;

    @Field
    private String from;

    @Field
    private String to;

    @Field
    private CallDirection callDirection;

    @Field
    private CallStatus callStatus;

    @Field
    private String motechCallId;

    @Field
    private String providerCallId;

    @Field
    private Map<String, String> providerExtraData;

    public CallDetailRecord() {
        providerExtraData = new HashMap<>();
        callStatus = CallStatus.UNKNOWN;
    }

    public CallDetailRecord(String timestamp, String configName, String from, String to, CallDirection callDirection,
                            CallStatus callStatus, String motechCallId, String providerCallId,
                            Map<String, String> providerExtraData) {
        this();
        this.timestamp = timestamp;
        this.configName = configName;
        this.from = from;
        this.to = to;
        this.callDirection = callDirection;
        this.callStatus = callStatus;
        this.motechCallId = motechCallId;
        this.providerCallId = providerCallId;
        if (providerExtraData != null) {
            this.providerExtraData = providerExtraData;
        }
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }

    public CallStatus getCallStatus() {
        return callStatus;
    }

    public String getMotechCallId() {
        return motechCallId;
    }

    public String getProviderCallId() {
        return providerCallId;
    }

    public Map<String, String> getProviderExtraData() {
        return providerExtraData;
    }

    /**
     * When receiving call detail information from an IVR provider the specific call details must be mapped from
     * what the provider sends back to MOTECH and a CallDetailRecord object. This method will find which field on the
     * given callDetailRecord matches the given key and set service to the given value. If there is no matching
     * CallDetailRecord field, then the key/value pair is added to the providerExtraData map field. Also, if a
     * callStatus value does not map to an existing CallStatus, then the value of the callStatus field is set to
     * CallStatus.UNKNOWN and the string value (with 'callStatus' key) is added to the providerExtraData
     * CallDetailRecord map field.
     *
     * @param key
     * @param value
     */
    public void setField(String key, String value) {
        if (value.length() > MAX_ENTITY_STRING_LENGTH) {
            logger.warn("The value for {} exceeds {} characters and will be truncated.", key, MAX_ENTITY_STRING_LENGTH);
            logger.warn("The complete value for {} is {}", key, value);
            value = value.substring(0, MAX_ENTITY_STRING_LENGTH);
        }

        try {
            java.lang.reflect.Field field = this.getClass().getDeclaredField(key);
            Object object;
            try {
                if ("callStatus".equals(key)) {
                    try {
                        object = CallStatus.valueOf(value);
                    } catch (IllegalArgumentException e) {
                        // Always add unknown call status to the provider extra data, for inspection
                        logger.warn("Unknown callStatus: {}", value);
                        providerExtraData.put(key, value);
                        object = CallStatus.UNKNOWN;
                    }
                }
                else if ("callDirection".equals(key)) {
                    try {
                        CallDirection callDirection = CallDirection.valueOf(value);
                        object = callDirection;
                    } catch (IllegalArgumentException e) {
                        // Always add unknown call directions to the provider extra data, for inspection
                        logger.warn("Unknown callDirection: {}", value);
                        providerExtraData.put(key, value);
                        object = CallDirection.UNKNOWN;
                    }
                }
                else {
                    object = value;
                }
                field.set(this, object);
            } catch (IllegalAccessException e) {
                // This should never happen as all CallDetailRecord fields should be accessible, but if somehow there
                // happens to be a 'final' public field with the same name as a call detail key, then this will throw
                throw new IllegalStateException(String.format(
                        "Unable to set call detail record field '%s' to value '%s':\n%s", key, value, e));
            }
        } catch (NoSuchFieldException e) {
            logger.info("Extra data from provider: '{}': '{}'", key, value);
            providerExtraData.put(key, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallDetailRecord)) return false;

        CallDetailRecord that = (CallDetailRecord) o;

        if (callDirection != that.callDirection) return false;
        if (callStatus != that.callStatus) return false;
        if (!configName.equals(that.configName)) return false;
        if (from != null ? !from.equals(that.from) : that.from != null) return false;
        if (motechCallId != null ? !motechCallId.equals(that.motechCallId) : that.motechCallId != null) return false;
        if (providerCallId != null ? !providerCallId.equals(that.providerCallId) : that.providerCallId != null)
            return false;
        if (providerExtraData != null ? !providerExtraData.equals(that.providerExtraData) :
                that.providerExtraData != null) return false;
        if (!timestamp.equals(that.timestamp)) return false;
        if (to != null ? !to.equals(that.to) : that.to != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = timestamp.hashCode();
        result = 31 * result + configName.hashCode();
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        result = 31 * result + callDirection.hashCode();
        result = 31 * result + callStatus.hashCode();
        result = 31 * result + (motechCallId != null ? motechCallId.hashCode() : 0);
        result = 31 * result + (providerCallId != null ? providerCallId.hashCode() : 0);
        result = 31 * result + (providerExtraData != null ? providerExtraData.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CallDetailRecord{" +
                "timestamp=" + timestamp +
                ", configName='" + configName + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", callDirection=" + callDirection +
                ", callStatus=" + callStatus +
                ", motechCallId='" + motechCallId + '\'' +
                ", providerCallId='" + providerCallId + '\'' +
                ", providerExtraData=" + providerExtraData +
                '}';
    }
}