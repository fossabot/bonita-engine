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
package org.bonitasoft.engine.profile.model;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.hibernate.annotations.Filter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "profileentry")
@IdClass(PersistentObjectId.class)
@Filter(name = "tenantFilter")
public class SProfileEntry implements PersistentObject {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String PROFILE_ID = "profileId";
    public static final String PARENT_ID = "parentId";
    public static final String PAGE = "page";
    public static final String INDEX = "index";
    public static final String TYPE = "type";
    public static final String CUSTOM = "custom";
    @Id
    private long id;
    @Id
    private long tenantId;
    @Column
    private long profileId;
    @Column
    private long parentId;
    @Column
    private String name;
    @Column
    private String description;
    @Column(name = "index_")
    private long index;
    @Column
    private String type;
    @Column
    private String page;
    @Column
    private boolean custom;

    public SProfileEntry(final String name, final long profileId) {
        this.name = name;
        this.profileId = profileId;
    }

    public SProfileEntry(final SProfileEntry profileEntry) {
        super();
        id = profileEntry.getId();
        name = profileEntry.getName();
        description = profileEntry.getDescription();
        profileId = profileEntry.getProfileId();
        parentId = profileEntry.getParentId();
        index = profileEntry.getIndex();
        page = profileEntry.getPage();
        type = profileEntry.getType();
        custom = profileEntry.isCustom();
    }

}
