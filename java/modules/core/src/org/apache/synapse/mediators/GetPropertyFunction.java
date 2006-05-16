/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.synapse.mediators;

import org.jaxen.Function;
import org.jaxen.Context;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.function.StringFunction;
import org.apache.synapse.MessageContext;
import org.apache.synapse.HeaderType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Iterator;

/**
 * Implements the XPath extension function synapse:get-property(prop-name)
 */
public class GetPropertyFunction implements Function {

    private static final Log log = LogFactory.getLog(GetPropertyFunction.class);

    private MessageContext synCtx = null;

    public MessageContext getSynCtx() {
        return synCtx;
    }

    public void setSynCtx(MessageContext synCtx) {
        this.synCtx = synCtx;
    }

    public Object call(Context context, List args) throws FunctionCallException {
        if (args.isEmpty()) {
            log.warn("Property key value for lookup was not specified");
            return null;
        } else if (synCtx == null) {
            log.warn("Synapse context has not been set for the XPath extension function" +
                "'synapse:get-property(prop-name)'");
            return null;

        } else {
            Navigator navigator = context.getNavigator();
            Iterator iter = args.iterator();
            while (iter.hasNext()) {
                String key = StringFunction.evaluate(iter.next(), navigator);
                // ignore if more than one argument has been specified
                Object result = synCtx.getProperty(key);

                if (result != null) {
                    return result;
                } else {
                    if (HeaderType.STR_TO.equals(key) && synCtx.getTo() != null) {
                        return synCtx.getTo().getAddress();
                    } else if (HeaderType.STR_FROM.equals(key) && synCtx.getFrom() != null) {
                        return synCtx.getFrom().getAddress();
                    } else if (HeaderType.STR_ACTION.equals(key) && synCtx.getWSAAction() != null) {
                        return synCtx.getWSAAction();
                    } else if (HeaderType.STR_FAULT.equals(key) && synCtx.getFaultTo() != null) {
                        return synCtx.getFaultTo().getAddress();
                    } else if (HeaderType.STR_REPLY_TO.equals(key) && synCtx.getReplyTo() != null) {
                        return synCtx.getReplyTo().getAddress();
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
