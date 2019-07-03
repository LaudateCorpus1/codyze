package de.fhg.aisec.markmodel;

import de.fhg.aisec.mark.markDsl.*;
import de.fhg.aisec.markmodel.fsm.FSM;
import de.fraunhofer.aisec.crymlin.utils.Pair;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MRule {

  private String name;
  private RuleStatement statement;
  private FSM fsm = null;
  private String errorMessage;
  private HashMap<String, Pair<String, MEntity>> entityReferences;

  @Nullable
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setStatement(RuleStatement stmt) {
    // This is an ECore RuleStatement.
    this.statement = stmt;
  }

  public RuleStatement getStatement() {
    return statement;
  }

  // https://javapapers.com/java/java-string-vs-stringbuilder-vs-stringbuffer-concatenation-performance-micro-benchmark/
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("rule ").append(getName()).append(" {");
    if (!statement.getEntities().isEmpty()) {
      sb.append("\n\tusing ")
          .append(
              statement.getEntities().stream()
                  .map(entity -> entity.getE().getName() + " as " + entity.getN())
                  .collect(Collectors.joining(", \n\t\t")));
    }
    if (statement.getCond() != null) {
      sb.append("\n\twhen ").append(MarkInterpreter.exprToString(statement.getCond().getExp()));
    }
    sb.append("\n\tensure\n\t\t")
        .append(MarkInterpreter.exprToString(statement.getEnsure().getExp()))
        .append("\n\tonfail ")
        .append(statement.getMsg())
        .append("\n}");
    return sb.toString();
  }

  public void setFSM(FSM fsm) {
    this.fsm = fsm;
  }

  public FSM getFSM() {
    return fsm;
  }

  public void setErrorMessage(String msg) {
    this.errorMessage = msg;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setEntityReferences(HashMap<String, Pair<String, MEntity>> entityReferences) {
    this.entityReferences = entityReferences;
  }

  public HashMap<String, Pair<String, MEntity>> getEntityReferences() {
    return entityReferences;
  }
}