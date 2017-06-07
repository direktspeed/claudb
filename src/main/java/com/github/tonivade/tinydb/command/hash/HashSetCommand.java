/*
 * Copyright (c) 2015-2017, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */

package com.github.tonivade.tinydb.command.hash;

import static com.github.tonivade.tinydb.data.DatabaseKey.safeKey;
import static com.github.tonivade.tinydb.data.DatabaseValue.entry;
import static com.github.tonivade.tinydb.data.DatabaseValue.hash;

import java.util.HashMap;
import java.util.Map;

import com.github.tonivade.resp.annotation.Command;
import com.github.tonivade.resp.annotation.ParamLength;
import com.github.tonivade.resp.command.Request;
import com.github.tonivade.resp.protocol.RedisToken;
import com.github.tonivade.resp.protocol.SafeString;
import com.github.tonivade.tinydb.command.TinyDBCommand;
import com.github.tonivade.tinydb.command.annotation.ParamType;
import com.github.tonivade.tinydb.data.DataType;
import com.github.tonivade.tinydb.data.DatabaseValue;
import com.github.tonivade.tinydb.data.Database;

@Command("hset")
@ParamLength(3)
@ParamType(DataType.HASH)
public class HashSetCommand implements TinyDBCommand {

  @Override
  public RedisToken execute(Database db, Request request) {
    DatabaseValue value = hash(entry(request.getParam(1), request.getParam(2)));

    DatabaseValue resultValue = db.merge(safeKey(request.getParam(0)), value,
        (oldValue, newValue) -> {
          Map<SafeString, SafeString> merge = new HashMap<>();
          merge.putAll(oldValue.getValue());
          merge.putAll(newValue.getValue());
          return hash(merge.entrySet());
        });

    Map<SafeString, SafeString> resultMap = resultValue.getValue();

    return RedisToken.integer(resultMap.get(request.getParam(1)) == null);
  }

}
