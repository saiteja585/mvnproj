package org.apache.maven.model.building;

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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.maven.xml.sax.filter.AbstractSAXFilter;
import org.apache.maven.xml.sax.filter.BuildPomXMLFilterFactory;
import org.apache.maven.xml.sax.filter.BuildPomXMLFilterListener;
import org.eclipse.sisu.Nullable;
import org.xml.sax.SAXException;

/**
 * ModelSourceTransformer for the build pom
 * 
 * @author Robert Scholte
 * @since 3.7.0
 */
@Named
@Singleton
class BuildModelSourceTransformer extends AbstractModelSourceTransformer
{
    @Inject
    @Nullable
    private BuildPomXMLFilterListener xmlFilterListener;
    
    protected AbstractSAXFilter getSAXFilter( Path pomFile, TransformerContext context )
        throws TransformerConfigurationException, SAXException, ParserConfigurationException
    {
        BuildPomXMLFilterFactory buildPomXMLFilterFactory = new DefaultBuildPomXMLFilterFactory( context );
        
        return buildPomXMLFilterFactory.get( pomFile );
    }
    
    @Override
    protected OutputStream filterOutputStream( OutputStream outputStream, Path pomFile )
    {
        OutputStream out;
        if ( xmlFilterListener != null )
        {
            out = new FilterOutputStream( outputStream )
            {
                @Override
                public void write( byte[] b, int off, int len )
                    throws IOException
                {
                    super.write( b, off, len );
                    xmlFilterListener.write( pomFile, b, off, len );
                }  
            };
        }
        else
        {
            out = outputStream;
        }
        return out;
    }
}
