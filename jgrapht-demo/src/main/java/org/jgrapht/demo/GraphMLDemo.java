/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.demo;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.AttributeType;
import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DefaultAttribute;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphExporter;
import org.jgrapht.io.GraphImporter;
import org.jgrapht.io.GraphMLExporter;
import org.jgrapht.io.GraphMLExporter.AttributeCategory;
import org.jgrapht.io.GraphMLImporter;
import org.jgrapht.io.ImportException;
import org.jgrapht.io.IntegerComponentNameProvider;
import org.jgrapht.io.VertexProvider;

/**
 * This class demonstrates exporting and importing a graph with custom vertex and edge attributes in
 * GraphML. Vertices of the graph have an attribute called "color" and a "name" attribute. Edges
 * have a "weight" attribute as well as a "name" attribute.
 * 
 * The demo constructs a complete graph with random edge weights and exports it as GraphML. The
 * output is then re-imported into a second graph.
 */
public final class GraphMLDemo
{
    // number of vertices
    private static final int SIZE = 6;

    // random number generator
    private static final Random GENERATOR = new Random(17);

    /**
     * Color
     */
    enum Color
    {
        BLACK("black"),
        WHITE("white");

        private final String value;

        private Color(String value)
        {
            this.value = value;
        }

        public String toString()
        {
            return value;
        }

    }

    /**
     * A custom graph vertex.
     */
    static class CustomVertex
    {
        private String id;
        private Color color;

        public CustomVertex(String id)
        {
            this(id, null);
        }

        public CustomVertex(String id, Color color)
        {
            this.id = id;
            this.color = color;
        }

        @Override
        public int hashCode()
        {
            return (id == null) ? 0 : id.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CustomVertex other = (CustomVertex) obj;
            if (id == null) {
                return other.id == null;
            } else {
                return id.equals(other.id);
            }
        }

        public Color getColor()
        {
            return color;
        }

        public void setColor(Color color)
        {
            this.color = color;
        }

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(id);
            if (color != null) {
                sb.append(",").append(color);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    /**
     * Create exporter
     */
    private static GraphExporter<CustomVertex, DefaultWeightedEdge> createExporter()
    {
        /*
         * Create vertex id provider.
         *
         * The exporter needs to generate for each vertex a unique identifier.
         */
        ComponentNameProvider<CustomVertex> vertexIdProvider = v -> v.id;

        /*
         * Create vertex label provider.
         *
         * The exporter may need to generate for each vertex a (not necessarily unique) label. If
         * null the exporter does not output any labels.
         */
        ComponentNameProvider<CustomVertex> vertexLabelProvider = null;

        /*
         * The exporter may need to generate for each vertex a set of attributes. Attributes must
         * also be registered as shown later on.
         */
        ComponentAttributeProvider<CustomVertex> vertexAttributeProvider = v -> {
            Map<String, Attribute> m = new HashMap<>();
            if (v.getColor() != null) {
                m.put("color", DefaultAttribute.createAttribute(v.getColor().toString()));
            }
            m.put("name", DefaultAttribute.createAttribute("node-" + v.id));
            return m;
        };

        /*
         * Create edge id provider.
         *
         * The exporter needs to generate for each edge a unique identifier.
         */
        ComponentNameProvider<DefaultWeightedEdge> edgeIdProvider =
            new IntegerComponentNameProvider<>();

        /*
         * Create edge label provider.
         *
         * The exporter may need to generate for each edge a (not necessarily unique) label. If null
         * the exporter does not output any labels.
         */
        ComponentNameProvider<DefaultWeightedEdge> edgeLabelProvider = null;

        /*
         * The exporter may need to generate for each edge a set of attributes. Attributes must also
         * be registered as shown later on.
         */
        ComponentAttributeProvider<DefaultWeightedEdge> edgeAttributeProvider = e -> {
            Map<String, Attribute> m = new HashMap<>();
            m.put("name", DefaultAttribute.createAttribute(e.toString()));
            return m;
        };

        /*
         * Create the exporter
         */
        GraphMLExporter<CustomVertex,
            DefaultWeightedEdge> exporter = new GraphMLExporter<>(
                vertexIdProvider, vertexLabelProvider, vertexAttributeProvider, edgeIdProvider,
                edgeLabelProvider, edgeAttributeProvider);

        /*
         * Set to export the internal edge weights
         */
        exporter.setExportEdgeWeights(true);

        /*
         * Register additional color attribute for vertices
         */
        exporter.registerAttribute("color", AttributeCategory.NODE, AttributeType.STRING);

        /*
         * Register additional name attribute for vertices and edges
         */
        exporter.registerAttribute("name", AttributeCategory.ALL, AttributeType.STRING);

        return exporter;
    }

    /**
     * Create the importer
     */
    private static GraphImporter<CustomVertex, DefaultWeightedEdge> createImporter()
    {
        /*
         * Create vertex provider.
         *
         * The importer reads vertices and calls a vertex provider to create them. The provider
         * receives as input the unique id of each vertex and any additional attributes from the
         * input stream.
         */
        VertexProvider<CustomVertex> vertexProvider = (id, attributes) -> {
            CustomVertex cv = new CustomVertex(id);

            // read color from attributes map
            if (attributes.containsKey("color")) {
                String color = attributes.get("color").getValue();
                switch (color) {
                case "black":
                    cv.setColor(Color.BLACK);
                    break;
                case "white":
                    cv.setColor(Color.WHITE);
                    break;
                default:
                    // ignore not supported color
                }
            }

            return cv;
        };

        /*
         * Create edge provider.
         *
         * The importer reads edges from the input stream and calls an edge provider to create them.
         * The provider receives as input the source and target vertex of the edge, an edge label
         * (which can be null) and a set of edge attributes all read from the input stream.
         */
        EdgeProvider<CustomVertex, DefaultWeightedEdge> edgeProvider =
            (from, to, label, attributes) -> new DefaultWeightedEdge();

        /*
         * Create the graph importer with a vertex and an edge provider.
         */
        GraphMLImporter<CustomVertex, DefaultWeightedEdge> importer =
            new GraphMLImporter<>(vertexProvider, edgeProvider);

        return importer;
    }

    /**
     * Main demo method
     * 
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        /*
         * Generate the complete graph. Vertices have random colors and edges have random edge
         * weights.
         */
        Graph<CustomVertex, DefaultWeightedEdge> graph1 =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        CompleteGraphGenerator<CustomVertex, DefaultWeightedEdge> completeGenerator =
            new CompleteGraphGenerator<>(SIZE);

        VertexFactory<CustomVertex> vFactory = new VertexFactory<CustomVertex>()
        {
            private int id = 0;

            @Override
            public CustomVertex createVertex()
            {
                return new CustomVertex(
                    String.valueOf(id++), GENERATOR.nextBoolean() ? Color.BLACK : Color.WHITE);
            }

        };

        System.out.println("-- Generating complete graph");
        completeGenerator.generateGraph(graph1, vFactory);

        /*
         * Assign random weights to the graph
         */
        for (DefaultWeightedEdge e : graph1.edgeSet()) {
            graph1.setEdgeWeight(e, GENERATOR.nextInt(100));
        }

        try {
            // now export and import back again
            System.out.println("-- Exporting graph as GraphML");
            GraphExporter<CustomVertex, DefaultWeightedEdge> exporter = createExporter();
            // export as string
            Writer writer = new StringWriter();
            exporter.exportGraph(graph1, writer);
            String graph1AsGraphML = writer.toString();

            // display
            System.out.println(graph1AsGraphML);

            // import it back
            System.out.println("-- Importing graph back from GraphML");
            Graph<CustomVertex, DefaultWeightedEdge> graph2 =
                new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
            GraphImporter<CustomVertex, DefaultWeightedEdge> importer = createImporter();
            importer.importGraph(graph2, new StringReader(graph1AsGraphML));

        } catch (ExportException | ImportException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(-1);
        }

    }

}
