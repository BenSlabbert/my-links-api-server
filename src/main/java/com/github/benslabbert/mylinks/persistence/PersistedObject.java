package com.github.benslabbert.mylinks.persistence;

import java.util.Map;

public interface PersistedObject {

  Map<byte[], byte[]> serialize();
}
