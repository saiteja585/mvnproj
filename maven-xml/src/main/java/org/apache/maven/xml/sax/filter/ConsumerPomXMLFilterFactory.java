package org.apache.maven.xml.sax.filter;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.nio.file.Path;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

/**
 * 
 * @author Robert Scholte
 * @since 3.7.0
 */
public class ConsumerPomXMLFilterFactory
{
    private BuildPomXMLFilterFactory buildPomXMLFilterFactory;
    
    public ConsumerPomXMLFilterFactory( BuildPomXMLFilterFactory buildPomXMLFilterFactory )
    {
        this.buildPomXMLFilterFactory = buildPomXMLFilterFactory;
    }
    
    public final ConsumerPomXMLFilter get( Path projectPath )
        throws SAXException, ParserConfigurationException, TransformerConfigurationException
    {
        BuildPomXMLFilter parent = buildPomXMLFilterFactory.get( projectPath );
        
        // Ensure that xs:any elements aren't touched by next filters
        AbstractSAXFilter filter = new FastForwardFilter( parent );
        
        CiFriendlyXMLFilter ciFriendlyFilter = new CiFriendlyXMLFilter();
        getChangelist().ifPresent( ciFriendlyFilter::setChangelist  );
        getRevision().ifPresent( ciFriendlyFilter::setRevision );
        getSha1().ifPresent( ciFriendlyFilter::setSha1 );
        
        if ( ciFriendlyFilter.isSet() )
        {
            ciFriendlyFilter.setParent( parent );
            ciFriendlyFilter.setLexicalHandler( parent );
            filter = ciFriendlyFilter;
        }
        
        // Strip modules
        filter = new ModulesXMLFilter( filter );
        // Adjust relativePath
        filter = new RelativePathXMLFilter( filter );
        
        return new ConsumerPomXMLFilter( filter );
    }
    
    // getters for the 3 magic properties of CIFriendly versions ( https://maven.apache.org/maven-ci-friendly.html )

    protected Optional<String> getChangelist()
    {
        return Optional.empty();
    }

    protected Optional<String> getRevision()
    {
        return Optional.empty();
    }

    protected Optional<String> getSha1()
    {
        return Optional.empty();
    }

}
