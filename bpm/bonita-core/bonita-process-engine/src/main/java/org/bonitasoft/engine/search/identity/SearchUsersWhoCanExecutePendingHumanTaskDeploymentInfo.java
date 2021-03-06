/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.search.identity;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractUserSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchUserDescriptor;

/**
 * @author Julien Reboul
 */
public class SearchUsersWhoCanExecutePendingHumanTaskDeploymentInfo extends AbstractUserSearchEntity {

    private final ActivityInstanceService activityInstanceService;

    private final long humanTaskInstanceId;

    public SearchUsersWhoCanExecutePendingHumanTaskDeploymentInfo(long humanTaskInstanceId,
            final ActivityInstanceService activityInstanceService,
            final SearchUserDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
        this.activityInstanceService = activityInstanceService;
        this.humanTaskInstanceId = humanTaskInstanceId;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return activityInstanceService.getNumberOfUsersWhoCanExecutePendingHumanTaskDeploymentInfo(humanTaskInstanceId,
                searchOptions);
    }

    @Override
    public List<SUser> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return activityInstanceService.searchUsersWhoCanExecutePendingHumanTaskDeploymentInfo(humanTaskInstanceId,
                searchOptions);
    }

}
