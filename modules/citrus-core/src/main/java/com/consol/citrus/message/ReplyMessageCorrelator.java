/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.message;

import org.springframework.integration.Message;

/**
 * Message correlator interface for synchronous reply messages. Correlator uses
 * a specific header entry in messages in order to construct a unique message correlation key.
 * 
 * @author Christoph Deppisch
 */
public interface ReplyMessageCorrelator {
    /**
     * Constructs the correlation key from the message header.
     * @param request
     * @return
     */
    String getCorrelationKey(Message<?> request);
    
    /**
     * Get the correlation header name.
     * @param id
     * @return
     */
    String getCorrelationKey(String id);
}
