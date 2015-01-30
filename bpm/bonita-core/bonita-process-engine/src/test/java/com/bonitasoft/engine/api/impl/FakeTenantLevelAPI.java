/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.impl.AvailableOnStoppedNode;

/**
 * @author Emmanuel Duchastenier
 */
public class FakeTenantLevelAPI {

    @AvailableWhenTenantIsPaused
    @AvailableOnStoppedNode
    public void canAlsoBeCalledOnPausedTenant() {

    }

    @AvailableOnStoppedNode
    public void mustBeCalledOnRunningTenant() {

    }

    @AvailableWhenTenantIsPaused(only = true)
    @AvailableOnStoppedNode
    public void canOnlyBeCalledOnPausedTenant() {

    }

    @AvailableOnStoppedNode
    public void platformAPIMethod() {

    }

}
