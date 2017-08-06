package com.github.tonivade.tinydb;

import static com.github.tonivade.resp.protocol.SafeString.safeString;
import static com.github.tonivade.tinydb.data.DatabaseKey.safeKey;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import com.github.tonivade.resp.protocol.SafeString;
import com.github.tonivade.tinydb.data.Database;
import com.github.tonivade.tinydb.data.DatabaseKey;
import com.github.tonivade.tinydb.data.DatabaseValue;
import com.github.tonivade.tinydb.data.SimpleDatabase;
import com.github.tonivade.tinydb.persistence.RDBInputStream;
import com.github.tonivade.tinydb.persistence.RDBOutputStream;

public class TinyDBServerState {

  private static final int RDB_VERSION = 6;

  private static final DatabaseKey SLAVES_KEY = safeKey(safeString("slaves"));

  private boolean master;
  private final List<Database> databases = new ArrayList<>();
  private final Database admin = new SimpleDatabase();
  private final Map<SafeString, SafeString> scripts = new HashMap<>();
  private final DB db = DBMaker.memoryDirectDB().make();

  public TinyDBServerState(int numDatabases) {
    this.master = true;
    for (int i = 0; i < numDatabases; i++) {
      this.databases.add(new SimpleDatabase(createMap(i)));
    }
  }
  
  public void setMaster(boolean master) {
    this.master = master;
  }

  public boolean isMaster() {
    return master;
  }

  public Database getAdminDatabase() {
    return admin;
  }

  public Database getDatabase(int id) {
    return databases.get(id);
  }

  public void clear() {
    databases.clear();
    db.close();
  }

  public boolean hasSlaves() {
    DatabaseValue slaves = admin.getOrDefault(SLAVES_KEY, DatabaseValue.EMPTY_SET);
    return !slaves.<Set<String>>getValue().isEmpty();
  }

  public void exportRDB(OutputStream output) throws IOException {
    RDBOutputStream rdb = new RDBOutputStream(output);
    rdb.preamble(RDB_VERSION);
    for (int i = 0; i < databases.size(); i++) {
      Database db = databases.get(i);
      if (!db.isEmpty()) {
        rdb.select(i);
        rdb.dabatase(db);
      }
    }
    rdb.end();
  }

  public void importRDB(InputStream input) throws IOException {
    RDBInputStream rdb = new RDBInputStream(input);

    rdb.parse().forEach((i, db) -> this.databases.set(i, db));
  }

  public void saveScript(SafeString sha1, SafeString script) {
    scripts.put(sha1, script);
  }

  public Optional<SafeString> getScript(SafeString sha1) {
    return Optional.ofNullable(scripts.get(sha1));
  }

  public void cleanScripts() {
    scripts.clear();
  }

  private BTreeMap<DatabaseKey, DatabaseValue> createMap(int i) {
    return db.treeMap("name" + i).keySerializer(Serializer.JAVA).valueSerializer(Serializer.JAVA).create();
  }
}
