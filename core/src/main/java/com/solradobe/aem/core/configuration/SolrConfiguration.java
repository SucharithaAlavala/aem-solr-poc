package com.solradobe.aem.core.configuration;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "AEM Solr Configuration")
public @interface SolrConfiguration {

    @AttributeDefinition(name = "Solr URL")
    String solrUrl() default "http://localhost:8983/solr";

    @AttributeDefinition(name = "Solr Core")
    String solrCore() default "aem-content";
}
