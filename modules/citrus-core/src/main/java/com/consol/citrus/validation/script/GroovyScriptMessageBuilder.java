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

package com.consol.citrus.validation.script;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;

/**
 * Builds a control message from Groovy code with markup builder support.
 * 
 * @author Christoph Deppisch
 */
public class GroovyScriptMessageBuilder extends AbstractMessageContentBuilder<String> {

    /** Default path to script template */
    private Resource scriptTemplateResource = new ClassPathResource("com/consol/citrus/script/markup-builder-template.groovy");
    
    /** Control message payload defined in external file resource as Groovy MarkupBuilder script */
    private Resource scriptResource;

    /** Inline control message payload as Groovy MarkupBuilder script */
    private String scriptData;
    
    /**
     * Build the control message from script code.
     */
    public String buildMessagePayload(TestContext context) {
        try {    
            //construct control message payload
            String messagePayload = "";
            if (scriptResource != null){
                messagePayload = buildMarkupBuilderScript(context.replaceDynamicContentInString(
                        FileUtils.readToString(scriptResource)));
            } else if (scriptData != null){
                messagePayload = buildMarkupBuilderScript(context.replaceDynamicContentInString(
                        scriptData));
            }
            
            return messagePayload;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to build control message payload", e);
        }
    }
    
    /**
     * Builds an automatic Groovy MarkupBuilder script with given script body.
     * 
     * @param scriptData
     * @return
     */
    private String buildMarkupBuilderScript(String scriptData) {
        try {
            ClassLoader parent = GroovyScriptMessageBuilder.class.getClassLoader(); 
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            
            Class<?> groovyClass = loader.parseClass(TemplateBasedScriptBuilder.fromTemplateResource(scriptTemplateResource)
                                                            .withCode(scriptData)
                                                            .build());
            
            if (groovyClass == null) {
                throw new CitrusRuntimeException("Could not load groovy script!");    
            }
            
            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            return (String) groovyObject.invokeMethod("run", new Object[] {});
        } catch (CompilationFailedException e) {
            throw new CitrusRuntimeException(e);
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Set message payload data as inline Groovy MarkupBuilder script.
     * @param scriptData the scriptData to set
     */
    public void setScriptData(String scriptData) {
        this.scriptData = scriptData;
    }

    /**
     * Message payload as external Groovy MarkupBuilder script file resource.
     * @param scriptResource the scriptResource to set
     */
    public void setScriptResource(Resource scriptResource) {
        this.scriptResource = scriptResource;
    }

    /**
     * Gets the scriptResource.
     * @return the scriptResource
     */
    public Resource getScriptResource() {
        return scriptResource;
    }

    /**
     * Gets the scriptData.
     * @return the scriptData
     */
    public String getScriptData() {
        return scriptData;
    }
}
