/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.synapse.mediators.template;

import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.eip.EIPUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class handles invocation of a synapse function template.
 * <invoke target="">
 * <parameter name="p1" value="{expr} | {{expr}} | value" />*
 * ..
 * </invoke>
 */
public class InvokeMediator extends AbstractMediator {
    /**
     * refers to the target template this is going to invoke
     * this is a read only attribute of the mediator
     */
    private String targetTemplate;

    /**
     * maps each parameter name to a Expression/Value
     * this is a read only attribute of the mediator
     */
    private Map<String, Value> pName2ExpressionMap;

    public InvokeMediator() {
        pName2ExpressionMap = new HashMap<String, Value>();
    }

    public boolean mediate(MessageContext synCtx) {
        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Invoking Target EIP Sequence " + targetTemplate + " paramNames : " +
                                pName2ExpressionMap.keySet());
            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }
        populateParameters(synCtx);
        //get the target function template and invoke by passing populated parameters
        Mediator mediator = synCtx.getConfiguration().getSequence(targetTemplate);
        if (mediator != null && mediator instanceof TemplateMediator) {
            return mediator.mediate(synCtx);
        }
        return false;
    }

    /**
     * poplulate declared parameters on temp synapse properties
     * @param synCtx
     */
    private void populateParameters(MessageContext synCtx) {
        Iterator<String> params = pName2ExpressionMap.keySet().iterator();
        while (params.hasNext()) {
            String parameter = params.next();
            if (!"".equals(parameter)) {
                Value expression = pName2ExpressionMap.get(parameter);
                if (expression != null) {
                    EIPUtils.createSynapseEIPTemplateProperty(synCtx, targetTemplate, parameter, expression);
                }
            }
        }
    }

    public String getTargetTemplate() {
        return targetTemplate;
    }

    public void setTargetTemplate(String targetTemplate) {
        this.targetTemplate = targetTemplate;
    }

    public Map<String, Value> getpName2ExpressionMap() {
        return pName2ExpressionMap;
    }

    public void addExpressionForParamName(String pName, Value expr) {
        pName2ExpressionMap.put(pName, expr);
    }
}
