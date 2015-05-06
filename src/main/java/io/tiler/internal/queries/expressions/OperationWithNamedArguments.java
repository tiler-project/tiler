package io.tiler.internal.queries.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class OperationWithNamedArguments extends Operation {
  private final Map<String, Expression> arguments = new HashMap<>();

  public OperationWithNamedArguments(Iterable<Map.Entry<String, Expression>> arguments) throws InvalidExpressionException {
    List<String> mandatoryArgumentNames = new ArrayList<>(getMandatoryArgumentNames());
    List<String> optionalArgumentNames = new ArrayList<>(getOptionalArgumentNames());

    for (Map.Entry<String, Expression> nameAndArgument : arguments) {
      String name = nameAndArgument.getKey();

      if (mandatoryArgumentNames.contains(name)) {
        mandatoryArgumentNames.remove(name);
      }
      else if (optionalArgumentNames.contains(name)) {
        optionalArgumentNames.remove(name);
      }
      else {
        throw new InvalidExpressionException("Unsupported named argument " + name);
      }

      this.arguments.put(name, nameAndArgument.getValue());
    }

    if (!mandatoryArgumentNames.isEmpty()) {
      String missingArguments = String.join(", ", mandatoryArgumentNames);
      String message = mandatoryArgumentNames.size() == 1 ?
        "Mandatory argument " + missingArguments + " is missing" :
        "Mandatory arguments " + missingArguments + " are missing";
      throw new InvalidExpressionException(message);
    }
  }

  public Iterable<Map.Entry<String, Expression>> getArguments() {
    return arguments.entrySet();
  }

  public Expression getArgument(String name) {
    return arguments.get(name);
  }

  public boolean hasArgument(String name) {
    return arguments.containsKey(name);
  }

  public abstract List<String> getMandatoryArgumentNames();

  public abstract List<String> getOptionalArgumentNames();
}
