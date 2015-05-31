package io.tiler.internal;

import io.tiler.internal.queries.Query;

import java.util.HashMap;

public class SocketState {
  private HashMap<String, Query> queries = new HashMap<>();

  public HashMap<String, Query> queries() {
    return queries;
  }
}
