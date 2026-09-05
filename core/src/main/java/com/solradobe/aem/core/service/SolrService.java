package com.solradobe.aem.core.service;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.solradobe.aem.core.configuration.SolrConfiguration;

@Component(service = SolrService.class, immediate = true)
@Designate(ocd = SolrConfiguration.class)
public class SolrService {

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private String solrUrl;
    private String solrCore;

    @Activate
    protected void activate(SolrConfiguration config) {
        solrUrl = config.solrUrl();
        solrCore = config.solrCore();

        System.out.println("Solr URL: " + solrUrl);
        System.out.println("Solr Core: " + solrCore);
    }

    public void readPage(String pagePath) {

        try (ResourceResolver resourceResolver =
                     resourceResolverFactory.getServiceResourceResolver(null)) {

            Resource resource = resourceResolver.getResource(pagePath);

            if (resource == null) {
                System.out.println("Page not found: " + pagePath);
                return;
            }

            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

            if (pageManager == null) {
                System.out.println("PageManager not available");
                return;
            }

            Page page = pageManager.getPage(pagePath);

            if (page == null) {
                System.out.println("AEM Page not found: " + pagePath);
                return;
            }

            System.out.println("========== AEM PAGE ==========");
            System.out.println("Path  : " + page.getPath());
            System.out.println("Title : " + page.getTitle());
            System.out.println("Name  : " + page.getName());
            System.out.println("==============================");

        } catch (Exception e) {
            System.out.println("Error reading AEM page: " + e.getMessage());
        }
    }
}
