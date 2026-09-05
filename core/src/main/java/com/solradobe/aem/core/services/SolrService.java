package com.solradobe.aem.core.services;

import org.apache.sling.api.resource.ResourceResolver;

public interface SolrService {

    ResourceResolver getServiceResourceResolver();
    void testServiceUser();
    void readPageContent(String pagePath);
    void indexPage(String pagePath);
}
