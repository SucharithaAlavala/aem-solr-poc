package com.solradobe.aem.core.services.impl;

import com.solradobe.aem.core.configuration.SolrConfiguration;
import com.solradobe.aem.core.services.SolrSearchService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component(service = SolrSearchService.class)
@Designate(ocd = SolrConfiguration.class)
public class SolrSearchServiceImpl implements SolrSearchService {

    private static final Logger LOG =
            LoggerFactory.getLogger(SolrSearchServiceImpl.class);

    private String solrUrl;
    private String solrCore;

    @Activate
    protected void activate(SolrConfiguration config) {
        solrUrl = config.solrUrl();
        solrCore = config.solrCore();
    }

    @Override
    public String search(String query, int start, int rows, String sort) {

        try {

            String solrQuery = "title:\"" + query + "\" OR description:\"" + query + "\"";

            String url = solrUrl + "/" + solrCore + "/select"
                    + "?q=" + URLEncoder.encode(solrQuery, StandardCharsets.UTF_8)
                    + "&start=" + start
                    + "&rows=" + rows
                    + "&wt=json";

            if (sort != null && !sort.isEmpty()) {
                url += "&sort=" +
                        URLEncoder.encode(sort, StandardCharsets.UTF_8);
            }

            LOG.info("Searching Solr: {}", url);

            HttpURLConnection connection =
                    (HttpURLConnection) new URL(url).openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {

                try (InputStream inputStream =
                             connection.getInputStream()) {

                    return new String(
                            inputStream.readAllBytes(),
                            StandardCharsets.UTF_8
                    );
                }

            }

            LOG.error("Solr search failed. Response code: {}",
                    responseCode);

            return "{\"error\":\"Solr search failed\"}";

        } catch (Exception e) {

            LOG.error("Error while searching Solr", e);

            return "{\"error\":\"Unable to search Solr\"}";
        }
    }
}