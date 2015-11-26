/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.process.instance.model.builder.event.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateCatchEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.impl.SIntermediateCatchEventInstanceImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 */
public class SIntermediateCatchEventInstanceBuilderImpl extends SEventInstanceBuilderImpl implements SIntermediateCatchEventInstanceBuilder {

    public SIntermediateCatchEventInstanceBuilderImpl(final SIntermediateCatchEventInstanceImpl entity) {
        super(entity);
    }

    @Override
    public SEventInstance done() {
        return (SEventInstance) entity;
    }

}