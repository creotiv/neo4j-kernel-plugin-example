package org.neo4j.examplekernelextension;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import org.neo4j.examplekernelextension.ExampleKernelExtensionFactory;

import java.util.*;
import java.sql.*;

import static org.junit.Assert.*;
import static org.neo4j.helpers.collection.MapUtil.map;
import static org.neo4j.helpers.collection.MapUtil.stringMap;

import java.util.logging.Logger;
import java.util.logging.Level;

public class ExampleEventHandlerIntegrationTest {

    private GraphDatabaseService db;
    private final static Logger logger = Logger.getLogger(ExampleEventHandlerIntegrationTest.class.getName());

    @Before
    public void setUp() throws Exception {
        // creating local test database with our plugin installed
        db = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(config())
                .newGraphDatabase();
    }

    /*
        Setting up plugin test parameters
    */
    private Map<String, String> config() {
        return stringMap(
                "examplekernelextension.debug", "true",
                "examplekernelextension.somevar", "42");
    }

    @After
    public void tearDown() throws Exception {
        logger.log(Level.INFO, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        db.shutdown();
    }

    @Test
    public void testCreateRel() throws Exception {
        logger.log(Level.INFO, ">>>>>>>>>>>>>> testCreateRel");
        Transaction tx = db.beginTx();
        org.neo4j.graphdb.Node node1 = db.createNode(DynamicLabel.label("Node1"),DynamicLabel.label("TestNode"));
        org.neo4j.graphdb.Node node2 = db.createNode(DynamicLabel.label("Node2"));
        RelationshipType relType = DynamicRelationshipType.withName("Rel");
        Relationship rel = node1.createRelationshipTo(node2, relType);
        rel.setProperty("test","test");
        node1.setProperty("test1","test");
        node2.setProperty("test2","test");
        tx.success();
        tx.close();

        Thread.sleep(2000); // wait for completition of different tasks(calls to another servers, etc) 

        try {
            Result rs = db.execute("MATCH (n:Node1)-[r:Rel]->(k:Node2) return r.test as test");
            Map<String,Object> out = rs.next();
            assertEquals(out.get("test"), "test");
        } catch (Exception e) {
            fail(e.toString());
        }

    }
}
