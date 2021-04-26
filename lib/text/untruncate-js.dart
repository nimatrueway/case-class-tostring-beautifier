// Adopted from https://github.com/dphilipson/untruncate-json

enum ContextType {
  TOP_LEVEL,
  STRING,
  STRING_ESCAPED,
  STRING_UNICODE,
  NUMBER,
  NUMBER_NEEDS_DIGIT,
  NUMBER_NEEDS_EXPONENT,
  TRUE,
  FALSE,
  NULL,
  ARRAY_NEEDS_VALUE,
  ARRAY_NEEDS_COMMA,
  OBJECT_NEEDS_KEY,
  OBJECT_NEEDS_COLON,
  OBJECT_NEEDS_VALUE,
  OBJECT_NEEDS_COMMA
}

enum RespawnReason { STRING_ESCAPE, COLLECTION_ITEM }

bool isWhitespace(String char) {
  return '\u0020\u000D\u000A\u0009'.contains(char);
}

String untruncateJson(String json) {
  var contextStack = [ContextType.TOP_LEVEL];
  var position = 0;
  int? respawnPosition;
  int? respawnStackLength;
  RespawnReason? respawnReason;

  void push(ContextType context) {
    contextStack.add(context);
  }

  void replace(ContextType context) {
    (contextStack[contextStack.length - 1] = context);
  }

  void setRespawn(RespawnReason reason) {
    if (respawnPosition == null) {
      respawnPosition = position;
      respawnStackLength = contextStack.length;
      respawnReason = reason;
    }
  }

  ;
  void clearRespawn(RespawnReason reason) {
    if (reason == respawnReason) {
      respawnPosition = null;
      respawnStackLength = null;
      respawnReason = null;
    }
  }

  ;
  ContextType pop() {
    return contextStack.removeLast();
  }

  int dontConsumeCharacter() {
    return position--;
  }

  void startAny(String char) {
    if (RegExp(r'^[0-9]$').hasMatch(char)) {
      push(ContextType.NUMBER);
      return;
    }
    switch (char) {
      case '"':
        push(ContextType.STRING);
        return;
      case '-':
        push(ContextType.NUMBER_NEEDS_DIGIT);
        return;
      case 't':
        push(ContextType.TRUE);
        return;
      case 'f':
        push(ContextType.FALSE);
        return;
      case 'n':
        push(ContextType.NULL);
        return;
      case '[':
        push(ContextType.ARRAY_NEEDS_VALUE);
        return;
      case '{':
        push(ContextType.OBJECT_NEEDS_KEY);
        return;
    }
  }

  ;

  for (final length = json.length; position < length; position++) {
    final char = json[position];
    switch (contextStack[contextStack.length - 1]) {
      case ContextType.TOP_LEVEL:
        startAny(char);
        break;
      case ContextType.STRING:
        switch (char) {
          case '"':
            pop();
            break;
          case '\\':
            setRespawn(RespawnReason.STRING_ESCAPE);
            push(ContextType.STRING_ESCAPED);
            break;
        }
        break;
      case ContextType.STRING_ESCAPED:
        if (char == 'u') {
          push(ContextType.STRING_UNICODE);
        } else {
          clearRespawn(RespawnReason.STRING_ESCAPE);
          pop();
        }
        break;
      case ContextType.STRING_UNICODE:
        if (position - json.lastIndexOf('u', position) == 4) {
          clearRespawn(RespawnReason.STRING_ESCAPE);
          pop();
        }
        break;
      case ContextType.NUMBER:
        if (char == '.') {
          replace(ContextType.NUMBER_NEEDS_DIGIT);
        } else if (char == 'e' || char == 'E') {
          replace(ContextType.NUMBER_NEEDS_EXPONENT);
        } else if (!RegExp(r'^[0-9]$').hasMatch(char)) {
          dontConsumeCharacter();
          pop();
        }
        break;
      case ContextType.NUMBER_NEEDS_DIGIT:
        replace(ContextType.NUMBER);
        break;
      case ContextType.NUMBER_NEEDS_EXPONENT:
        if (char == '+' || char == '-') {
          replace(ContextType.NUMBER_NEEDS_DIGIT);
        } else {
          replace(ContextType.NUMBER);
        }
        break;
      case ContextType.TRUE:
      case ContextType.FALSE:
      case ContextType.NULL:
        if (!RegExp(r'^[a-z]$').hasMatch(char)) {
          dontConsumeCharacter();
          pop();
        }
        break;
      case ContextType.ARRAY_NEEDS_VALUE:
        if (char == ']') {
          pop();
        } else if (!isWhitespace(char)) {
          clearRespawn(RespawnReason.COLLECTION_ITEM);
          replace(ContextType.ARRAY_NEEDS_COMMA);
          startAny(char);
        }
        break;
      case ContextType.ARRAY_NEEDS_COMMA:
        if (char == ']') {
          pop();
        } else if (char == ',') {
          setRespawn(RespawnReason.COLLECTION_ITEM);
          replace(ContextType.ARRAY_NEEDS_VALUE);
        }
        break;
      case ContextType.OBJECT_NEEDS_KEY:
        if (char == '}') {
          pop();
        } else if (char == '"') {
          setRespawn(RespawnReason.COLLECTION_ITEM);
          replace(ContextType.OBJECT_NEEDS_COLON);
          push(ContextType.STRING);
        }
        break;
      case ContextType.OBJECT_NEEDS_COLON:
        if (char == ':') {
          replace(ContextType.OBJECT_NEEDS_VALUE);
        }
        break;
      case ContextType.OBJECT_NEEDS_VALUE:
        if (!isWhitespace(char)) {
          clearRespawn(RespawnReason.COLLECTION_ITEM);
          replace(ContextType.OBJECT_NEEDS_COMMA);
          startAny(char);
        }
        break;
      case ContextType.OBJECT_NEEDS_COMMA:
        if (char == '}') {
          pop();
        } else if (char == ',') {
          setRespawn(RespawnReason.COLLECTION_ITEM);
          replace(ContextType.OBJECT_NEEDS_KEY);
        }
        break;
    }
  }

  if (respawnStackLength != null) {
    contextStack.length = respawnStackLength!;
  }
  var result = [
    (respawnPosition != null) ? json.substring(0, respawnPosition) : json,
  ];
  void finishWord(String word) {
    result.add(word.substring(json.length - json.lastIndexOf(word[0])));
  }

  for (var i = contextStack.length - 1; i >= 0; i--) {
    switch (contextStack[i]) {
      case ContextType.STRING:
        result.add('"');
        break;
      case ContextType.NUMBER_NEEDS_DIGIT:
      case ContextType.NUMBER_NEEDS_EXPONENT:
        result.add('0');
        break;
      case ContextType.TRUE:
        finishWord('true');
        break;
      case ContextType.FALSE:
        finishWord('false');
        break;
      case ContextType.NULL:
        finishWord('null');
        break;
      case ContextType.ARRAY_NEEDS_VALUE:
      case ContextType.ARRAY_NEEDS_COMMA:
        result.add(']');
        break;
      case ContextType.OBJECT_NEEDS_KEY:
      case ContextType.OBJECT_NEEDS_COLON:
      case ContextType.OBJECT_NEEDS_VALUE:
      case ContextType.OBJECT_NEEDS_COMMA:
        result.add('}');
        break;
      case ContextType.TOP_LEVEL:
      case ContextType.STRING_ESCAPED:
      case ContextType.STRING_UNICODE:
      case ContextType.NUMBER:
        // Skip?
        break;
    }
  }
  return result.join('');
}