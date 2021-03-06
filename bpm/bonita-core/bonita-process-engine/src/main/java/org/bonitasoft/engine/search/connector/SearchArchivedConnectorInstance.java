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
package org.bonitasoft.engine.search.connector;

import java.util.List;

import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAConnectorInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractArchivedConnectorInstanceSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

/**
 * @author Baptiste Mesta
 */
public class SearchArchivedConnectorInstance extends AbstractArchivedConnectorInstanceSearchEntity {

    private final ReadPersistenceService persistenceService;

    private final ConnectorInstanceService connectorInstanceService;

    public SearchArchivedConnectorInstance(final ConnectorInstanceService connectorInstanceService,
            final SearchEntityDescriptor searchDescriptor,
            final SearchOptions options, final ReadPersistenceService persistenceService) {
        super(searchDescriptor, options);
        this.connectorInstanceService = connectorInstanceService;
        this.persistenceService = persistenceService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return connectorInstanceService.getNumberArchivedConnectorInstance(searchOptions, persistenceService);
    }

    @Override
    public List<SAConnectorInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return connectorInstanceService.searchArchivedConnectorInstance(searchOptions, persistenceService);
    }

}
