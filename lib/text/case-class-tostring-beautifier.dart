// Adopted from https://github.com/nimatrueway/case-class-tostring-beautifier

import 'dart:math';

class Node {
  String value;
  bool isLeaf;
  Node? parent;
  List<Node> children;

  factory Node.Leaf(String value) => Node(value, [], true, null);

  factory Node.NonLeaf(String value) => Node(value, [], false, null);

  Node(this.value, this.children, this.isLeaf, this.parent);

  bool get isNonLeaf => !isLeaf;

  @override
  String toString() => isLeaf ? value : '$value(${children.join(",")})';
}

extension NodeTransformation on Node {
  Node newChild(Node child) {
    child.parent = this;
    children.add(child);
    return child;
  }
}

extension NodePresentation on Node {
  String printTree([String indent = '']) {
    final printedArgs;
    if (isLeaf) {
      printedArgs = '';
    } else if (children.isEmpty) {
      printedArgs = '()';
    } else if (children.length == 1 && children.first.isLeaf) {
      printedArgs = '(${children.first.value})';
    } else {
      final multiLineArgs = children.map((n) => n.printTree(indent + '  '));
      printedArgs = '(\n${multiLineArgs.join(',\n')}\n$indent)';
    }

    return indent + value + printedArgs;
  }
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
//                                 PARSING                                   //
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////

final tokenPattern = RegExp(r'(\()|(\))|(,)|([^,()]+\()|([^,()]+)');

class ParseResult {
  Node? result;
  List<String> errors;

  ParseResult(this.result, this.errors);
}

ParseResult parse(String input) {
  String? isFn(String input) =>
      input.endsWith('(') ? input.substring(0, input.length - 1) : null;
  final root = Node.NonLeaf('');
  var current = root;
  Match? lastToken;
  var errors = <String>[];

  for (final tokenMatch in tokenPattern.allMatches(input)) {
    void fail() {
      final i = tokenMatch.start;
      final I = ' ' * i.toString().length;
      final b = input.substring(max(0, i - 10), i);
      final B = ' ' * b.length;
      final a = input.substring(i, min(i + 10, input.length));
      errors.add('''
           |Illegal token starts at index $i : $b$a
           |                              $I   $B^
        ''');
    }

    final token = tokenMatch.group(0)!;
    final fnName = isFn(token);
    String lastTokenChar() => input[lastToken!.end - 1];

    // cover any input that is not case-class
    if (lastToken == null && fnName == null) {
      root.newChild(Node.Leaf(input));
      break;
    }

    // cover Fn(
    if (current.isNonLeaf && fnName != null) {
      current = current.newChild(Node.NonLeaf(fnName));
    }
    // cover Fn(X,
    else if (current.isNonLeaf && token == ',') {
      if (current.parent == null) {
        fail();
        break;
      }
      // cover Fn(X,,Y) || cover Fn(,X,Y)
      if (lastTokenChar() == ',' || lastTokenChar() == '(') {
        current.newChild(Node.Leaf(''));
      }
    }
    // cover Fn(X..)
    else if (token == ')') {
      if (current.parent == null) {
        fail();
        break;
      }
      // cover Fn(X,Y,)
      if (lastTokenChar() == ',') {
        current.newChild(Node.Leaf(''));
      }
      if (current.parent == root && tokenMatch.end < input.length) {
        // cover redundant tail after first Fn(...)
        final sampleText = input.substring(
            tokenMatch.end, min(tokenMatch.end + 30, input.length));
        errors.add(
            'Warning: input had some redundant tail from ${tokenMatch.end}: $sampleText');
        break;
      }
      current = current.parent!;
    }
    // cover Fn(X
    else if (current.isNonLeaf) {
      current.newChild(Node.Leaf(token));
    }
    // invalid state
    else {
      fail();
      break;
    }

    lastToken = tokenMatch;
  }

  return ParseResult(root.children.first, errors);
}