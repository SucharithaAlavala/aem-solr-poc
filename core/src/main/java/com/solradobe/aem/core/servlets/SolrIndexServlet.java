package com.solradobe.aem.core.servlets;


import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.solradobe.aem.core.services.SolrService;

import javax.servlet.Servlet;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/solr/index",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class SolrIndexServlet extends SlingSafeMethodsServlet {

    @Reference
    private SolrService solrService;

    @Override
    protected void doGet(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response)
            throws IOException {

        String pagePath = request.getParameter("path");

        if (pagePath == null || pagePath.isEmpty()) {
            response.setStatus(
                    SlingHttpServletResponse.SC_BAD_REQUEST
            );
            response.getWriter().write(
                    "Please provide page path. Example: ?path=/content/aem-solr-poc/us/en/products"
            );
            return;
        }

        solrService.indexPage(pagePath);

        response.setContentType("text/plain");
        response.getWriter().write(
                "Indexing request sent for: " + pagePath
        );
    }
}

