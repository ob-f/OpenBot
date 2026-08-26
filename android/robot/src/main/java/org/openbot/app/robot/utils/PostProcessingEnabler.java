package org.openbot.app.robot.utils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public class PostProcessingEnabler implements TypeAdapterFactory {
  public interface PostProcessable {
    void postProcess();
  }

  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

    return new TypeAdapter<T>() {
      public void write(JsonWriter out, T value) throws IOException {
        delegate.write(out, value);
      }

      public T read(JsonReader in) throws IOException {
        T obj = delegate.read(in);
        if (obj instanceof PostProcessable) {
          ((PostProcessable) obj).postProcess();
        }
        return obj;
      }
    };
  }
}
