package de.fhg.aisec.markmodel.fsm;

import de.fhg.aisec.mark.markDsl.Expression;
import de.fhg.aisec.mark.markDsl.RepetitionExpression;
import de.fhg.aisec.mark.markDsl.SequenceExpression;
import de.fhg.aisec.mark.markDsl.Terminal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FSM {

  private static final Logger log = LoggerFactory.getLogger(FSM.class);

  private HashSet<Node> startNodes = null;

  public FSM() {}

  public String toString() {
    HashSet<Node> seen = new HashSet<>();
    ArrayList<Node> current = new ArrayList<>(startNodes);
    HashMap<Node, Integer> nodeToId = new HashMap<>();
    StringBuilder sb = new StringBuilder();
    int node_counter = 0;
    while (!current.isEmpty()) {
      ArrayList<Node> newWork = new ArrayList<>();
      for (Node n : current) {
        if (!seen.contains(n)) {
          Integer id = nodeToId.get(n);
          if (id == null) {
            id = node_counter++;
            nodeToId.put(n, id);
          }
          sb.append(n).append(" (").append(id).append(")\n");
          TreeMap<String, Node> sorted = new TreeMap<>();
          for (Node s : n.getSuccessors()) {
            sorted.put(s.getName(), s);
          }
          for (Map.Entry<String, Node> entry : sorted.entrySet()) {
            Node s = entry.getValue();
            Integer id_succ = nodeToId.get(s);
            if (id_succ == null) {
              id_succ = node_counter++;
              nodeToId.put(s, id_succ);
            }
            if (!seen.contains(s)) {
              newWork.add(s);
            }
            sb.append("\t-> ").append(s).append("(").append(id_succ).append(")").append("\n");
          }
          seen.add(n);
        }
      }
      current = newWork;
    }
    return sb.toString();
  }

  public static void clearDB() {
    String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost");
    String username = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
    String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "password");
    Configuration configuration =
        (new Configuration.Builder())
            .uri(uri)
            .autoIndex("none")
            .credentials(username, password)
            .verifyConnection(true)
            .build();
    SessionFactory sessionFactory = new SessionFactory(configuration, "de.fhg.aisec.markmodel.fsm");
    Session session = sessionFactory.openSession();
    session.purgeDatabase();
    sessionFactory.close();
  }

  public void pushToDB() {
    String uri = System.getenv().getOrDefault("NEO4J_URI", "bolt://localhost");
    String username = System.getenv().getOrDefault("NEO4J_USERNAME", "neo4j");
    String password = System.getenv().getOrDefault("NEO4J_PASSWORD", "password");
    Configuration configuration =
        (new Configuration.Builder())
            .uri(uri)
            .autoIndex("none")
            .credentials(username, password)
            .verifyConnection(true)
            .build();
    SessionFactory sessionFactory = new SessionFactory(configuration, "de.fhg.aisec.markmodel.fsm");
    Session session = sessionFactory.openSession();
    startNodes.forEach(session::save);
    sessionFactory.close();
  }

  /**
   * Order-Statement to FSM
   *
   * <p>Possible classes of the order construct: Terminal SequenceExpression RepetitionExpression
   * (mit ?, *, +)
   *
   * <p>Start with a "empty" FSM with only StartNode and EndNode
   *
   * <p>prevPointer = [&StartNode]
   *
   * <p>For each Terminal Add node, connect each last node (= each Node in prevPointer) to the
   * current node, return current node as only new prevPointer
   *
   * <p>For each Exp in SequenceExpression: call algo recursively, update (=overwrite)
   * prevPointer-List after each algo-call
   *
   * <p>For RepetitionExpression For + algo(inner) use * - part below once For ? algo(inner) the
   * resulting prevPointer-List needs to be added to the outer prevPointer List For * algo(inner),
   * BUT: the last node of the inner construct needs to point to the first node of the inner
   * construct the resulting prevPointer-List needs to be added to the outer prevPointer List
   */
  public void sequenceToFSM(final Expression seq) {
    Node start = new Node(null, "BEGIN");
    start.setStart(true);

    HashSet<Node> currentNodes = new HashSet<>();
    currentNodes.add(start);
    // System.out.println(MarkInterpreter.exprToString(seq));
    addExpr(seq, currentNodes);

    // not strictly needed, we could simply set end=true for all the returned nodes
    Node end = new Node(null, "END");
    end.setEnd(true);
    end.setFake(true);
    currentNodes.forEach(x -> x.addSuccessor(end));

    // we could remove BEGIN here, and set begin=true for its successors
    for (Node n : start.getSuccessors()) {
      n.setStart(true);
    }
    startNodes = start.getSuccessors();
  }

  private HashSet<Node> addExpr(final Expression expr, final HashSet<Node> currentNodes) {
    if (expr instanceof Terminal) {
      Terminal inner = (Terminal) expr;
      Node n = new Node(inner.getEntity(), inner.getOp());
      currentNodes.forEach(x -> x.addSuccessor(n));
      currentNodes.clear();
      currentNodes.add(n);
      return currentNodes;
    } else if (expr instanceof SequenceExpression) {
      SequenceExpression inner = (SequenceExpression) expr;
      addExpr(inner.getLeft(), currentNodes);
      return addExpr(inner.getRight(), currentNodes);
    } else if (expr instanceof RepetitionExpression) {
      RepetitionExpression inner = (RepetitionExpression) expr;
      switch (inner.getOp()) {
        case "?":
          {
            HashSet<Node> remember = new HashSet<>(currentNodes);
            addExpr(inner.getExpr(), currentNodes);
            currentNodes.addAll(remember);
            return currentNodes;
          }
        case "+":
          {
            HashSet<Node> remember = new HashSet<>(currentNodes);
            addExpr(inner.getExpr(), currentNodes);
            for (Node n : remember) {
              currentNodes.forEach(x -> x.addSuccessor(n.getSuccessors()));
            }
            return currentNodes;
          }
        case "*":
          {
            HashSet<Node> remember = new HashSet<>(currentNodes);
            addExpr(inner.getExpr(), currentNodes);
            for (Node n : remember) {
              currentNodes.forEach(x -> x.addSuccessor(n.getSuccessors()));
            }
            currentNodes.addAll(remember);
            return currentNodes;
          }
        default:
          log.error("UNKNOWN OP: " + inner.getOp());
          return addExpr(inner.getExpr(), currentNodes);
      }
    }

    log.error("ERROR, unknown Expression: " + expr.getClass());
    return null;
  }

  public HashSet<Node> getStart() {
    return startNodes;
  }
}