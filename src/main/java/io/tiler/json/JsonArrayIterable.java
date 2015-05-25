package io.tiler.json;

import org.vertx.java.core.json.JsonArray;

import java.util.Iterator;

public class JsonArrayIterable<T> implements Iterable<T> {
  private JsonArray jsonArray;

  public JsonArrayIterable(JsonArray jsonArray) {
    this.jsonArray = jsonArray;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      Iterator<Object> innerIterator;

      {
        innerIterator = jsonArray.iterator();
      }

      @Override
      public boolean hasNext() {
        return innerIterator.hasNext();
      }

      @Override
      public T next() {
        return (T) innerIterator.next();
      }

      @Override
      public void remove() {
        innerIterator.remove();
      }
    };
  }
}
