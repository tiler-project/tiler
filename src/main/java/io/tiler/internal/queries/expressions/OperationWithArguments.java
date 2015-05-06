package io.tiler.internal.queries.expressions;

import java.util.ArrayList;
import java.util.List;

public abstract class OperationWithArguments extends Operation {
  private List<Expression> arguments = new ArrayList<>();

  public OperationWithArguments(Iterable<Expression> arguments) {
    for (Expression item : arguments) {
      this.arguments.add(item);
    }
  }

  public OperationWithArguments(Expression argument) {
    this.arguments.add(argument);
  }

  public Iterable<Expression> getArguments() {
    return arguments;
  }

  public Expression getArgument(int index) {
    return arguments.get(index);
  }
}
