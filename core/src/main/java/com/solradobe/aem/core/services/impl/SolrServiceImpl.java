package com.solradobe.aem.core.services.impl;

import com.solradobe.aem.core.configuration.SolrConfiguration;
import com.solradobe.aem.core.services.SolrService;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component(service = SolrService.class)
@Designate (ocd = SolrConfiguration.class)
public class SolrServiceImpl implements SolrService {

    private static final Logger LOG =
            LoggerFactory.getLogger(SolrServiceImpl.class);

    private static final String SUBSERVICE = "solr-service";
    private static final String CONTENT_ROOT = "/content/aem-solr-poc";

    @Reference
    private ResourceResolverFactory resolverFactory;

    private String solrUrl;
    private String solrCore;

    @Activate
    protected void activate(SolrConfiguration config) {
        solrUrl = config.solrUrl();
        solrCore = config.solrCore();
    }

    @Override
    public ResourceResolver getServiceResourceResolver() {
        try {
            return resolverFactory.getServiceResourceResolver(
                    Collections.singletonMap(
                            ResourceResolverFactory.SUBSERVICE,
                            SUBSERVICE
                    )
            );
        } catch (Exception e) {
            LOG.error("Unable to get service resource resolver", e);
            return null;
        }
    }

    @Override
    public void testServiceUser() {

        try (ResourceResolver resolver = getServiceResourceResolver()) {

            if (resolver == null) {
                return;
            }

            Resource resource = resolver.getResource(CONTENT_ROOT);

            if (resource != null) {
                LOG.info("Service user can access {}", CONTENT_ROOT);
            } else {
                LOG.warn("Cannot access {}", CONTENT_ROOT);
            }

        } catch (Exception e) {
            LOG.error("Error testing service user", e);
        }
    }

    @Override
    public void readPageContent(String pagePath) {

        try (ResourceResolver resolver = getServiceResourceResolver()) {

            if (resolver == null) {
                return;
            }

            Resource page = resolver.getResource(pagePath);

            if (page == null) {
                LOG.warn("Page not found: {}", pagePath);
                return;
            }

            Resource content = page.getChild("jcr:content");

            if (content == null) {
                LOG.warn("jcr:content not found: {}", pagePath);
                return;
            }

            String title = content.getValueMap()
                    .get("jcr:title", String.class);

            String description = content.getValueMap()
                    .get("jcr:description", String.class);

            LOG.info("Page: {}", pagePath);
            LOG.info("Title: {}", title);
            LOG.info("Description: {}", description);

        } catch (Exception e) {
            LOG.error("Error reading page: {}", pagePath, e);
        }
    }

    @Override
    public void indexPage(String pagePath) {

        try (ResourceResolver resolver = getServiceResourceResolver()) {

            if (resolver == null) {
                return;
            }

            Resource page = resolver.getResource(pagePath);

            if (page == null) {
                LOG.warn("Page not found: {}", pagePath);
                return;
            }

            Resource content = page.getChild("jcr:content");

            if (content == null) {
                LOG.warn("jcr:content not found: {}", pagePath);
                return;
            }

            String title = content.getValueMap()
                    .get("jcr:title", String.class);

            String description = content.getValueMap()
                    .get("jcr:description", String.class);

            String json = "{"
                    + "\"id\":\"" + escape(pagePath) + "\","
                    + "\"title\":\"" + escape(title) + "\","
                    + "\"description\":\"" + escape(description) + "\""
                    + "}";

            sendToSolr(json);

        } catch (Exception e) {
            LOG.error("Error indexing page: {}", pagePath, e);
        }
    }

    private void sendToSolr(String json) throws Exception {

        String url = solrUrl + "/" + solrCore
                + "/update/json/docs?commit=true";

        HttpURLConnection connection =
                (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty(
                "Content-Type", "application/json");
        connection.setDoOutput(true);

        try (OutputStream output = connection.getOutputStream()) {
            output.write(json.getBytes(StandardCharsets.UTF_8));
        }

        LOG.info("Solr response: {}", connection.getResponseCode());

        connection.disconnect();
    }

    private String escape(String value) {

        if (value == null) {
            return "";

        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}