package org.motechproject.vxml.it;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.vxml.domain.CallDetailRecord;
import org.motechproject.vxml.domain.Config;
import org.motechproject.vxml.domain.HttpMethod;
import org.motechproject.vxml.repository.CallDetailRecordDataService;
import org.motechproject.vxml.repository.ConfigDataService;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jdo.JDOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Verify that the OutboundCallService is present & functional.
 */
@SuppressWarnings("ALL")
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class OutboundCallServiceIT extends BasePaxIT {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private org.motechproject.vxml.service.OutboundCallService outboundCallService;
    @Inject
    private CallDetailRecordDataService callDetailRecordDataService;
    @Inject
    private ConfigDataService configDataService;

    @Before
    public void setup() throws Exception {
        logger.info("setup()");
        try { configDataService.deleteAll(); } catch (JDOException e) { }
        try { callDetailRecordDataService.deleteAll(); } catch (JDOException e) { }
    }

    @Test
    public void verifyServiceFunctional() {
        logger.info("verifyServiceFunctional()");

        String httpServerURI = new SimpleHttpServer().start("foo", HttpStatus.SC_OK, "OK");
        logger.debug("verifyServiceFunctional - We have a server listening at {}", httpServerURI);

        //Create a config
        Map<String, String> outgoingCallUriParams = new HashMap<>();
        Config config = new Config("conf123", null, httpServerURI, HttpMethod.GET, outgoingCallUriParams);
        logger.debug("verifyServiceFunctional - We create a config  {}", config.toString());
        configDataService.create(config);

        Map<String, String> params = new HashMap<>();
        outboundCallService.initiateCall(config.getName(), params);

        List<CallDetailRecord> callDetailRecords = callDetailRecordDataService.retrieveAll();
        assertEquals(1, callDetailRecords.size());
        assertEquals("conf123", callDetailRecords.get(0).getConfigName());
    }

    @Test
    public void shouldHandleInvalidServerResponse() {
        logger.info("shouldHandleInvalidServerResponse()");

        String httpServerURI = new SimpleHttpServer().start("bar", HttpStatus.SC_BAD_REQUEST, "Eeek!");
        logger.debug("shouldHandleInvalidServerResponse - We have a server listening at {}", httpServerURI);

        //Create a config
        Map<String, String> outgoingCallUriParams = new HashMap<>();
        Config config = new Config("conf456", "", httpServerURI, HttpMethod.GET, outgoingCallUriParams);
        logger.debug("shouldHandleInvalidServerResponse - We create a config  {}", config.toString());
        configDataService.create(config);

        boolean exceptionThrown = false;
        Map<String, String> params = new HashMap<>();
        try {
            outboundCallService.initiateCall("conf456", params);
        }
        catch (RuntimeException e) {
            exceptionThrown = true;
        }
        // We're expecting an exception to be thrown
        assertTrue(exceptionThrown);

        List<CallDetailRecord> callDetailRecords = callDetailRecordDataService.retrieveAll();
        assertEquals(0, callDetailRecords.size());
    }
}
