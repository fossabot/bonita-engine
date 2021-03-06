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
package org.bonitasoft.engine.execution;

import java.util.List;
import java.util.stream.Collectors;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.SWorkRegisterException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class ProcessInstanceInterruptor {

    private ProcessInstanceService processInstanceService;
    private FlowNodeInstanceService flowNodeInstanceService;
    private ContainerRegistry containerRegistry;
    private final TechnicalLogger logger;

    public ProcessInstanceInterruptor(ProcessInstanceService processInstanceService,
            FlowNodeInstanceService flowNodeInstanceService,
            ContainerRegistry containerRegistry, final TechnicalLoggerService technicalLoggerService) {
        this.processInstanceService = processInstanceService;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.containerRegistry = containerRegistry;
        logger = technicalLoggerService.asLogger(ProcessInstanceInterruptor.class);
    }

    /**
     * Interrupt the given process instant AND its children
     *
     * @param processInstanceId the process instance
     * @param stateCategory the state category
     */
    public void interruptProcessInstance(final long processInstanceId, final SStateCategory stateCategory)
            throws SBonitaException {
        processInstanceService.setStateCategory(processInstanceService.getProcessInstance(processInstanceId),
                stateCategory);
        List<SFlowNodeInstance> flowNodeInstances = flowNodeInstanceService
                .getFlowNodeInstancesOfProcess(processInstanceId, 0, Integer.MAX_VALUE);
        interruptFlowNodeInstances(flowNodeInstances, stateCategory);
        logger.info("Process instance {} and its children were {}", processInstanceId,
                getInterruptionType(stateCategory));
    }

    private String getInterruptionType(SStateCategory stateCategory) {
        return stateCategory.equals(SStateCategory.ABORTING) ? "aborted" : "cancelled";
    }

    private void executeChildren(SStateCategory stateCategory, List<SFlowNodeInstance> stableChildrenIds)
            throws SWorkRegisterException {
        for (final SFlowNodeInstance child : stableChildrenIds) {
            logger.debug("Resume child in stateCategory {}: {}, {}, {}", stateCategory, child.getId(),
                    child.getStateName(), child.getStateCategory());
            executeFlowNode(child);
        }
    }

    private void executeFlowNode(SFlowNodeInstance child) throws SWorkRegisterException {
        if (child.isTerminal()) {
            containerRegistry.notifyChildFinished(child);
        } else {

            //should not try to execute these because its the children that should be aborted
            if (child.getType() != SFlowNodeType.MULTI_INSTANCE_ACTIVITY
                    || child.getType() != SFlowNodeType.LOOP_ACTIVITY) {
                containerRegistry.executeFlowNode(child);
            }
        }
    }

    /**
     * Interrupt the given process instant AND its children, excluding the exceptionChildId
     *
     * @param processInstanceId the process instance
     * @param stateCategory the state category
     * @param exceptionChildId the element to exclude
     */
    public void interruptProcessInstance(final long processInstanceId, final SStateCategory stateCategory,
            final long exceptionChildId)
            throws SBonitaException {
        processInstanceService.setStateCategory(processInstanceService.getProcessInstance(processInstanceId),
                stateCategory);
        List<SFlowNodeInstance> flowNodeInstances = flowNodeInstanceService
                .getFlowNodeInstancesOfProcess(processInstanceId, 0, Integer.MAX_VALUE);
        flowNodeInstances = excludeFlowNode(exceptionChildId, flowNodeInstances);
        interruptFlowNodeInstances(flowNodeInstances, stateCategory);
    }

    private List<SFlowNodeInstance> excludeFlowNode(long exceptionChildId, List<SFlowNodeInstance> flowNodeInstances) {
        return flowNodeInstances
                .stream()
                .filter(f -> f.getId() != exceptionChildId)
                .collect(Collectors.toList());
    }

    /**
     * Interrupt children of given process instance excluding interruptorChildId
     *
     * @param processInstanceId the process instance
     * @param stateCategory the state category in which children must be set
     * @param interruptorChildId the element to exclude
     */
    public void interruptChildrenOfProcessInstance(final long processInstanceId, final SStateCategory stateCategory,
            final long interruptorChildId)
            throws SBonitaException {
        List<SFlowNodeInstance> flowNodeInstances = flowNodeInstanceService
                .getFlowNodeInstancesOfProcess(processInstanceId, 0, Integer.MAX_VALUE);
        flowNodeInstances = excludeFlowNode(interruptorChildId, flowNodeInstances);
        interruptFlowNodeInstances(flowNodeInstances, stateCategory);
        logger.info("Process instance {} and its children were {} by flownode {}", processInstanceId,
                getInterruptionType(stateCategory), interruptorChildId);
    }

    /**
     * Interrupt children of given flow node instance
     *
     * @param flowNodeInstance the flow node instance
     * @param stateCategory the state category in which children must be set
     */
    public void interruptChildrenOfFlowNodeInstance(SFlowNodeInstance flowNodeInstance, SStateCategory stateCategory)
            throws SBonitaException {
        if (flowNodeInstance.getTokenCount() == 0) {
            return;
        }
        List<SFlowNodeInstance> flowNodeInstances = flowNodeInstanceService
                .getFlowNodeInstancesOfActivity(flowNodeInstance.getId(), 0, Integer.MAX_VALUE);
        interruptFlowNodeInstances(flowNodeInstances, stateCategory);
    }

    private void interruptFlowNodeInstances(final List<SFlowNodeInstance> children, final SStateCategory stateCategory)
            throws SBonitaException {
        for (final SFlowNodeInstance child : children) {
            logger.debug("Put element in {}, element:  {}, {}, {}", stateCategory, child.getId(), child.getStateName(),
                    child.getType());
            flowNodeInstanceService.setStateCategory(child, stateCategory);
        }
        executeChildren(stateCategory, children);
    }

}
