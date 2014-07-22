package org.motechproject.vxml.service.it;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.vxml.domain.CallDetailsRecord;
import org.motechproject.vxml.repository.CallDetailsRecordDataService;
import org.motechproject.vxml.service.CallDetailsRecordService;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jdo.JDOException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Verify that HelloWorldAuthorService present, functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class CallDetailsRecordServiceIT extends BasePaxIT {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private CallDetailsRecordService callDetailsRecordService;
    @Inject
    private CallDetailsRecordDataService callDetailsRecordDataService;

    @Before
    public void cleanupDatabase() {
        logger.info("cleanupDatabase");
        try { callDetailsRecordDataService.deleteAll(); } catch (JDOException e) { }
    }

    @Test
    public void verifyServiceFunctional() throws Exception {

        logger.info("verifyServiceFunctional");

        String motechId = UUID.randomUUID().toString();
        callDetailsRecordService.logCallStatusFromMotech("from", "to", "status", motechId);
        logger.info("created call record with motechID {}", motechId);

        List<CallDetailsRecord> callDetailsRecords = callDetailsRecordDataService.findByMotechId(motechId);
        assertEquals(1, callDetailsRecords.size());
        assertEquals(motechId, callDetailsRecords.get(0).getMotechId());
        logger.info("found call record with motechID {}", callDetailsRecords.get(0).getMotechId());
    }
}