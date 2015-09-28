/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileBuilder;
import org.bonitasoft.engine.profile.builder.SProfileBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilder;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilderFactory;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.service.ModelConvertor;

import com.bonitasoft.engine.core.reporting.SReport;
import com.bonitasoft.engine.core.reporting.SReportBuilder;
import com.bonitasoft.engine.core.reporting.SReportBuilderFactory;
import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.log.SeverityLevel;
import com.bonitasoft.engine.log.impl.LogImpl;
import com.bonitasoft.engine.monitoring.GcInfo;
import com.bonitasoft.engine.monitoring.MemoryUsage;
import com.bonitasoft.engine.monitoring.SGcInfo;
import com.bonitasoft.engine.monitoring.SMemoryUsage;
import com.bonitasoft.engine.monitoring.impl.GcInfoImpl;
import com.bonitasoft.engine.monitoring.impl.MemoryUsageImpl;
import com.bonitasoft.engine.platform.Tenant;
import com.bonitasoft.engine.platform.TenantCreator;
import com.bonitasoft.engine.platform.TenantCreator.TenantField;
import com.bonitasoft.engine.platform.impl.TenantImpl;
import com.bonitasoft.engine.profile.ProfileCreator;
import com.bonitasoft.engine.profile.ProfileCreator.ProfileField;
import com.bonitasoft.engine.profile.ProfileEntryCreator;
import com.bonitasoft.engine.profile.ProfileEntryCreator.ProfileEntryField;
import com.bonitasoft.engine.reporting.Report;
import com.bonitasoft.engine.reporting.ReportCreator;
import com.bonitasoft.engine.reporting.ReportCreator.ReportField;
import com.bonitasoft.engine.reporting.impl.ReportImpl;

/**
 * @author Matthieu Chaffotte
 */
public final class SPModelConvertor extends ModelConvertor {

    private static final String TENANT_STATUS_DEACTIVATED = "DEACTIVATED";

    public static List<Log> toLogs(final Collection<SQueriableLog> sQueriableLogs) {
        final List<Log> logs = new ArrayList<Log>();
        for (final SQueriableLog sQueriableLog : sQueriableLogs) {
            final Log log = toLog(sQueriableLog);
            logs.add(log);
        }
        return logs;
    }

    public static Log toLog(final SQueriableLog sQueriableLog) {
        final LogImpl log = new LogImpl(sQueriableLog.getId(), sQueriableLog.getRawMessage());
        log.setActionScope(sQueriableLog.getActionScope());
        log.setActionType(sQueriableLog.getActionType());
        log.setCallerClassName(sQueriableLog.getCallerClassName());
        log.setCallerMethodName(sQueriableLog.getCallerMethodName());
        log.setCreatedBy(sQueriableLog.getUserId());
        log.setCreationDate(new Date(sQueriableLog.getTimeStamp()));
        log.setSeverityLevel(SeverityLevel.valueOf(sQueriableLog.getSeverity().name()));
        return log;
    }

    public static Tenant toTenant(final STenant sTenant) {
        final TenantImpl tenant = new TenantImpl();
        tenant.setTenantId(sTenant.getId());
        tenant.setName(sTenant.getName());
        tenant.setState(sTenant.getStatus());
        tenant.setIconPath(sTenant.getIconPath());
        tenant.setIconName(sTenant.getIconName());
        tenant.setDescription(sTenant.getDescription());
        tenant.setDefaultTenant(sTenant.isDefaultTenant());
        tenant.setCreationDate(new Date(sTenant.getCreated()));
        // no createdBy in tenantImpl
        return tenant;
    }

    public static STenant constructTenant(final TenantCreator tCreator) {
        final Map<TenantField, Serializable> fields = tCreator.getFields();
        final STenantBuilder sTenantBuilder = BuilderFactory.get(STenantBuilderFactory.class).createNewInstance((String) fields.get(TenantField.NAME),
                "defaultUser", System.currentTimeMillis(), TENANT_STATUS_DEACTIVATED, (Boolean) fields.get(TenantField.DEFAULT_TENANT));
        sTenantBuilder.setDescription((String) fields.get(TenantField.DESCRIPTION));
        sTenantBuilder.setIconName((String) fields.get(TenantField.ICON_NAME));
        sTenantBuilder.setIconPath((String) fields.get(TenantField.ICON_PATH));
        return sTenantBuilder.done();
    }

    public static List<Tenant> toTenants(final List<STenant> sTenants) {
        final List<Tenant> tenants = new ArrayList<Tenant>();
        for (final STenant sTenant : sTenants) {
            tenants.add(toTenant(sTenant));
        }
        return tenants;
    }

    public static SProfile constructSProfile(final ProfileCreator creator, final boolean isDefault, final long createdBy) {
        final long creationDate = System.currentTimeMillis();
        final Map<ProfileField, Serializable> fields = creator.getFields();
        final SProfileBuilder newSProfileBuilder = BuilderFactory.get(SProfileBuilderFactory.class).createNewInstance((String) fields.get(ProfileField.NAME),
                isDefault, creationDate, createdBy, creationDate, createdBy);
        final String description = (String) fields.get(ProfileField.DESCRIPTION);
        if (description != null) {
            newSProfileBuilder.setDescription(description);
        }
        return newSProfileBuilder.done();
    }

    public static SProfileEntry constructSProfileEntry(final ProfileEntryCreator creator) {
        final Map<ProfileEntryField, Serializable> fields = creator.getFields();
        final SProfileEntryBuilder newSProfileEntryBuilder = BuilderFactory.get(SProfileEntryBuilderFactory.class).createNewInstance(
                (String) fields.get(ProfileEntryField.NAME), (Long) fields.get(ProfileEntryField.PROFILE_ID));
        final String description = (String) fields.get(ProfileEntryField.DESCRIPTION);
        if (description != null) {
            newSProfileEntryBuilder.setDescription(description);
        }
        final Long index = (Long) fields.get(ProfileEntryField.INDEX);
        if (index != null) {
            newSProfileEntryBuilder.setIndex(index);
        } else {
            // Insert the profile entry at the end of the profile entry list
            newSProfileEntryBuilder.setIndex(Long.MAX_VALUE);
        }
        final String page = (String) fields.get(ProfileEntryField.PAGE);
        if (page != null) {
            newSProfileEntryBuilder.setPage(page);
        }
        final Long parentId = (Long) fields.get(ProfileEntryField.PARENT_ID);
        if (parentId != null) {
            newSProfileEntryBuilder.setParentId(parentId);
        }
        final String type = (String) fields.get(ProfileEntryField.TYPE);
        if (type != null) {
            newSProfileEntryBuilder.setType(type);
        }
        final Boolean custom = (Boolean) fields.get(ProfileEntryField.CUSTOM);
        if (custom != null) {
            newSProfileEntryBuilder.setCustom(custom);
        }
        return newSProfileEntryBuilder.done();
    }

    public static Map<String, GcInfo> toGcInfos(final Map<String, SGcInfo> lastGcInfo) {
        final Map<String, GcInfo> gcInfo = new HashMap<String, GcInfo>();
        for (final Entry<String, SGcInfo> entry : lastGcInfo.entrySet()) {
            gcInfo.put(entry.getKey(), toGcInfo(entry.getValue()));
        }
        return gcInfo;
    }

    private static GcInfo toGcInfo(final SGcInfo sGcInfo) {
        final long startTime = sGcInfo.getStartTime();
        final long endTime = sGcInfo.getEndTime();
        final long duration = sGcInfo.getDuration();
        final Map<String, MemoryUsage> memoryUsageBeforeGc = new HashMap<String, MemoryUsage>();
        final Map<String, SMemoryUsage> beforeGc = sGcInfo.getMemoryUsageBeforeGc();
        for (final Entry<String, SMemoryUsage> mem : beforeGc.entrySet()) {
            final SMemoryUsage sMemoryUsage = mem.getValue();
            memoryUsageBeforeGc.put(mem.getKey(), toMemoryUsage(sMemoryUsage));
        }
        final Map<String, MemoryUsage> memoryUsageAfterGc = new HashMap<String, MemoryUsage>();
        final Map<String, SMemoryUsage> afterGc = sGcInfo.getMemoryUsageAfterGc();
        for (final Entry<String, SMemoryUsage> mem : afterGc.entrySet()) {
            final SMemoryUsage sMemoryUsage = mem.getValue();
            memoryUsageAfterGc.put(mem.getKey(), toMemoryUsage(sMemoryUsage));
        }
        return new GcInfoImpl(startTime, endTime, duration, memoryUsageBeforeGc, memoryUsageAfterGc);
    }

    private static MemoryUsage toMemoryUsage(final SMemoryUsage sMemoryUsage) {
        final long committed = sMemoryUsage.getCommitted();
        final long max = sMemoryUsage.getMax();
        final long init = sMemoryUsage.getInit();
        final long used = sMemoryUsage.getUsed();
        return new MemoryUsageImpl(committed, max, init, used);
    }

    public static Report toReport(final SReport sReport) {
        final ReportImpl report = new ReportImpl(sReport.getId(), sReport.getName(), sReport.getInstallationDate(), sReport.getInstalledBy());
        report.setDescription(sReport.getDescription());
        report.setProvided(sReport.isProvided());
        report.setLastModificationDate(new Date(sReport.getLastModificationDate()));
        report.setScreenshot(sReport.getScreenshot());
        return report;
    }

    public static List<Report> toReports(final List<SReport> sReports) {
        final List<Report> reports = new ArrayList<Report>(sReports.size());
        for (final SReport sReport : sReports) {
            reports.add(toReport(sReport));
        }
        return reports;
    }

    public static SReport constructSReport(final ReportCreator creator, final long creatorUserId) {
        final Map<ReportField, Serializable> fields = creator.getFields();
        final String name = (String) fields.get(ReportField.NAME);
        final SReportBuilder newSReportBuilder = BuilderFactory.get(SReportBuilderFactory.class).createNewInstance(name, System.currentTimeMillis(),
                creatorUserId, false);
        final String description = (String) fields.get(ReportField.DESCRIPTION);
        if (description != null) {
            newSReportBuilder.setDescription(description);
        }
        final byte[] screenshot = (byte[]) fields.get(ReportField.SCREENSHOT);
        if (screenshot != null) {
            newSReportBuilder.setScreenshot(screenshot);
        }
        return newSReportBuilder.done();
    }

}