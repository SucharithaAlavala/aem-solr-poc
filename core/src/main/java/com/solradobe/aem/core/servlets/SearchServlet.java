package com.solradobe.aem.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.solradobe.aem.core.services.SolrSearchService;

import javax.servlet.Servlet;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/solr/search",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class SearchServlet extends SlingSafeMethodsServlet {

    @Reference
    private SolrSearchService solrSearchService;

    @Override
    protected void doGet(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response)
            throws IOException {

        String query = request.getParameter("q");

        if (query == null || query.isEmpty()) {
            response.setStatus(
                    SlingHttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    "{\"error\":\"Please provide search query\"}");
            return;
        }

        int start = getIntParameter(
                request.getParameter("start"), 0);

        int rows = getIntParameter(
                request.getParameter("rows"), 10);

        String sort = request.getParameter("sort");

        String result =
                solrSearchService.search(
                        query,
                        start,
                        rows,
                        sort);

        response.setContentType("application/json");
        response.getWriter().write(result);
    }

    private int getIntParameter(String value, int defaultValue) {

        try {
            return value != null
                    ? Integer.parseInt(value)
                    : defaultValue;

        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}