package org.neo4j.examplekernelextension;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.LabelEntry;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.ConstraintType;
import org.neo4j.helpers.Predicates;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import java.text.MessageFormat;

/*
    TransactionEventHandler template use String type, this type will be used as a 
    result type of beforeCommit method, and parameter type of the afterCommit and
    afterRollback methods. You can set it to any type or class you need.
*/
class ExampleEventHandler implements TransactionEventHandler<String> {
    private final static Logger logger = Logger.getLogger(ExampleEventHandler.class.getName());
    private GraphDatabaseService gds;
    private Boolean debug;
    private String somevar;
    private Map<String, List<String>> constraints = new HashMap<String, List<String>>();

    public ExampleEventHandler(GraphDatabaseService gds, Boolean debug, String somevar) {
        this.gds = gds;
        this.debug = debug;
        this.somevar = somevar;
    }

    public void log(Level level, Object... objs) {
        String data = "";
        for (Object obj : objs) {
            data += " " + String.valueOf(obj);
        }
        logger.log(level, data);
    }

    public void debug(Object... objs) {
      if(debug)
        this.log(Level.WARNING, objs);
    }

    public void error(Object... objs) {
      this.log(Level.WARNING, objs);
    }

    /*****************************
     * Node Helpers
     *******************************/

    private List<String> getNodeLabels(Node node) {
        List<String> aLabels = new ArrayList<String>();
        for (Label label : node.getLabels()) {
            aLabels.add(label.name());
        }
        return aLabels;
    }

    private Set<String> getConstraints(List<String> labels) {
        Set<String> filter = new HashSet<String>();
        for(String label : labels) {
            try {
                filter.addAll(constraints.get(label));
            } catch (NullPointerException e) {
            }
        }
        return filter;
    }

    private Map<String, Object> getProperties(PropertyContainer entity, Set<String> filter) {
        Map<String, Object> mProperties = new HashMap<String, Object>();

        for (String key : entity.getPropertyKeys()) {
            if (!filter.isEmpty() && !filter.contains(key))
                continue;
            mProperties.put(key, entity.getProperty(key));
        }
        return mProperties;
    }

    private Map<String, Object> getProperties(Map<String, Object> props, Set<String> filter) {
        Map<String, Object> mProperties = new HashMap<String, Object>();

        for (Map.Entry<String, Object> e : props.entrySet()) {
            if (!filter.isEmpty() && !filter.contains(e.getKey()))
                continue;
            mProperties.put(e.getKey(), e.getValue());
        }
        return mProperties;
    }


    private Boolean isNodeDeleted(Node node) {
        try {
            node.getPropertyKeys();
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    /***************************
     * Neo4J Helpers
     ********************************/

    private void updateConstraints() {
        Transaction tx = gds.beginTx();
        for (ConstraintDefinition ct : gds.schema().getConstraints()) {
            if (ct.getConstraintType() == ConstraintType.UNIQUENESS)
                constraints.put(ct.getLabel().name(), Lists.newArrayList(ct.getPropertyKeys()));
        }
        tx.success();
        tx.close();
    }

    /*****************************
     * Implementations
     ********************************/

    private String prepareCreatedNodes(TransactionData transactionData) {
        String out = "";
        for (Node node : transactionData.createdNodes()) {
            Map<String, Object> props = getProperties(node, new HashSet<String>());
            List<String> labels = getNodeLabels(node);
            String result = "Created Node (:"+String.join(":", labels)+" {";
            List<String> sprops = new ArrayList<String>();
            for (Map.Entry<String, Object> e : props.entrySet()) {
                sprops.add(e.getKey() + ":" + String.valueOf(e.getValue()));
            }
            result += String.join(", ", sprops) + " } )";
            out += result+"\n";
        }
        return out;
    }

    

    private void processCreatedNodes(String result) {
        debug("This updates was made: \n" + result);
    }

    /***************************
     * Hooks
     ******************************/

    @Override
    public String beforeCommit(TransactionData transactionData) throws Exception {
        // Updating unique fields that we use to find nodes, instead of server depend node id.
        // This can bee needed if you want, for example, query them on another db or service
        updateConstraints();
        
        // exception in this hook will cause transaction to rollback
        
        // also be aware that deleted nodes params, labels will not be accesible here through
        // GraphDatabaseService instance. Only through TransactionData events.

        return prepareCreatedNodes(transactionData);

    }

    @Override
    public void afterCommit(TransactionData transactionData, String result) {
        // we need transaction here if we will be querying local db
        Transaction tx = gds.beginTx();
        try {
            processCreatedNodes(result);
            tx.success();
        } finally {
            tx.close();
        }
        /*
        If you need to make changes on local db, run all queries in a thread.
        Because afterCommit method still have open transaction and that will 
        cause a deadlock if running not in thread.
        
        new Thread(new Runnable() {
            public void run() {
                Transaction t = gds.beginTx();
                try {
                    dosomething();
                    t.success();
                } finally {
                    t.close();
                }
            }
        }).start();
        */
    }

    @Override
    public void afterRollback(TransactionData transactionData, String result) {
        error("Something bad happend, Harry: " + result);
    }
}
