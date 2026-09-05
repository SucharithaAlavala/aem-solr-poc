package com.solradobe.aem.core.services;

public interface SolrSearchService {

    String search(String query, int start, int rows, String sort);
}
