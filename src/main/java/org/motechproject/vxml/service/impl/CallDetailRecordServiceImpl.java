package org.motechproject.vxml.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.motechproject.vxml.domain.CallDetailRecord;
import org.motechproject.vxml.domain.CallDirection;
import org.motechproject.vxml.domain.CallStatus;
import org.motechproject.vxml.repository.CallDetailRecordDataService;
import org.motechproject.vxml.service.CallDetailRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link org.motechproject.vxml.service.CallDetailRecordService} interface. Uses
 * {@link org.motechproject.vxml.repository.CallDetailRecordDataService} in order to retrieve and persist authors.
 */
@Service("callRecordService")
public class CallDetailRecordServiceImpl implements CallDetailRecordService {

    @Autowired
    private CallDetailRecordDataService callDetailRecordDataService;

    @Override
    public void logFromProvider(String config, String from, String to, CallDirection callDirection,
                                CallStatus callStatus, String providerStatus, String motechCallId,
                                String providerCallId, Map<String, String> providerExtraData) {
        List<CallDetailRecord> callDetailRecords = callDetailRecords = callDetailRecordDataService.findByProviderCallId(
                providerCallId);
        if (callDetailRecords.size() > 0 && StringUtils.isNotBlank(callDetailRecords.get(0).motechCallId)) {
            motechCallId = callDetailRecords.get(0).motechCallId;
        }
        callDetailRecordDataService.create(new CallDetailRecord(DateTime.now(), config, from, to, callDirection,
                callStatus, providerStatus, motechCallId, providerCallId, providerExtraData));
    }

    @Override
    public void logFromMotech(String config, String from, String to, CallDirection callDirection, CallStatus callStatus,
                              String motechId) {
        callDetailRecordDataService.create(new CallDetailRecord(DateTime.now(), config, from, to, callDirection,
                callStatus, callStatus.toString(), motechId, null, null));
    }
}
