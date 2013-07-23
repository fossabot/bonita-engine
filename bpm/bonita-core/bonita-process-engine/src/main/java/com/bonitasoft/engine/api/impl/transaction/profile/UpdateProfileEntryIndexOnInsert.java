/*******************************************************************************
 * Copyright (C) 2011, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.profile;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.profile.ProfileEntrySearchDescriptor;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.SProfileEntryNotFoundException;
import org.bonitasoft.engine.profile.SProfileEntryUpdateException;
import org.bonitasoft.engine.profile.builder.SProfileEntryBuilder;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * Update indexes for all elements having the same parent category
 * Indexes are always a multiple of 2
 * This is done to let web insert easily element between 2 other elements
 * e.g. if you want to insert an element index between 2 and 4 elements
 * put index to 3
 * here we update all element to have:
 * element(0) -> new index = 0
 * element(2) -> new index = 2
 * element(3) -> new index = 4
 * element(4) -> new index = 6
 * and so on
 * If element must be first just insert it with value < 0
 * 
 * @author Julien Mege
 * @author Celine Souchet
 */
public class UpdateProfileEntryIndexOnInsert implements TransactionContent {

    private static final int NUMBER_OF_RESULTS = 100;

    private final ProfileService profileService;

    private SProfileEntry insertedProfileEntry = null;

    public UpdateProfileEntryIndexOnInsert(final ProfileService profileService, final SProfileEntry profileEntry) {
        super();
        this.profileService = profileService;
        insertedProfileEntry = profileEntry;
    }

    @Override
    public void execute() throws SBonitaException {
        Long fromIndex = insertedProfileEntry.getIndex();
        List<SProfileEntry> profileEntryList;
        Long loopIndex = fromIndex + 1;

        // loop on profile entry results because we get NUMBER_OF_RESULTS * i entries
        do {

            profileEntryList = searchProfileEntriesForParentIdAndProfileId(loopIndex);
            for (final SProfileEntry profileEntry : profileEntryList) {
                updateProfileEntryIndex(profileEntry);// for every element of the set we update the index
            }
            loopIndex++;
        } while (!profileEntryList.isEmpty());

        // at the end we update the inserted profile entry to set the right index:
        // if -1 is sent the right value will be 0 to put the profile entry at the first position
        // else the value is only a odd number so we had to increment by 1 to set a good index
        updateProfileEntry(insertedProfileEntry, fromIndex < 0 ? 0 : fromIndex + 1);
    }

    private void updateProfileEntryIndex(final SProfileEntry profileEntry) throws SProfileEntryNotFoundException,
            SProfileEntryUpdateException {
        // inserted entry is insert before the set, so we must shift the set of indexes by 2 values
        updateProfileEntry(profileEntry, profileEntry.getIndex() + 2);
    }

    private List<SProfileEntry> searchProfileEntriesForParentIdAndProfileId(final Long fromIndex) throws SBonitaSearchException {
        Long profileId = null;
        Long parentId = null;
        if (insertedProfileEntry != null) {
            profileId = insertedProfileEntry.getProfileId();
            parentId = insertedProfileEntry.getParentId();
        }

        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(new FilterOption(SProfileEntry.class, ProfileEntrySearchDescriptor.PROFILE_ID, profileId));
        filters.add(new FilterOption(SProfileEntry.class, ProfileEntrySearchDescriptor.PARENT_ID, parentId));
        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>(2);
        orderByOptions.add(new OrderByOption(SProfileEntry.class, ProfileEntrySearchDescriptor.INDEX, OrderByType.ASC));
        final QueryOptions queryOptions = new QueryOptions(fromIndex.intValue() * NUMBER_OF_RESULTS, NUMBER_OF_RESULTS, orderByOptions, filters, null);
        return profileService.searchProfileEntries(queryOptions);
    }

    private void updateProfileEntry(final SProfileEntry profileEntry, final Long profileEntryIndex) throws SProfileEntryNotFoundException,
            SProfileEntryUpdateException {
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(SProfileEntryBuilder.INDEX, profileEntryIndex);
        profileService.updateProfileEntry(profileEntry, entityUpdateDescriptor);
    }
}
