/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.TestsInitializer;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;

public class TestsInitializerSP extends TestsInitializer {

    static ConfigurableApplicationContext springContext;

    private static APITestSPUtil testUtil = new APITestSPUtil();

    private static TestsInitializerSP _INSTANCE;

    public static void beforeAll() throws Exception {
        TestsInitializerSP.getInstance().before();
    }

    private static TestsInitializerSP getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new TestsInitializerSP();
        }
        return _INSTANCE;
    }

    public static void afterAll() throws Exception {
        TestsInitializerSP.getInstance().after();
    }

    @Override
    protected List<String> getSpringConfigLocations() {
        final List<String> springConfigLocations = new ArrayList<String>(super.getSpringConfigLocations());
        springConfigLocations.add("datasource-sp.xml");
        springConfigLocations.add("jndi-setup-sp.xml");
        return springConfigLocations;
    }

    @Override
    protected void deleteTenantAndPlatform() throws BonitaException {
        BPMTestSPUtil.destroyPlatformAndTenants();
        testUtil.deletePlatformStructure();
    }

    @Override
    protected void initPlatformAndTenant() throws Exception {
        try {
            testUtil.createPlatformStructure();
        } catch (final Exception e) {
            final Logger logger = LoggerFactory.getLogger(BonitaSuiteRunner.class);
            logger.error("unable to create platform", e);
            final PlatformSession session = testUtil.loginOnPlatform();
            final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
            platformAPI.stopNode();
            platformAPI.cleanPlatform();
            testUtil.deletePlatformStructure();
            testUtil.createPlatformStructure();
        }
        System.setProperty("delete.job.frequency", "0/30 * * * * ?");

        BPMTestSPUtil.createEnvironmentWithDefaultTenant();
    }

    @Override
    protected String getInitializerListenerClassName() {
        return "com.bonitasoft.engine.EngineInitializerSP";
    }

}
