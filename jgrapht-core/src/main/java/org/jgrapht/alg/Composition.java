/*
 * (C) Copyright 2007-2018, by France Telecom and Contributors.
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
package org.jgrapht.alg;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Static methods for composition of relations.
 *
 * See https://en.wikipedia.org/wiki/Composition_of_relations
 */
public class Composition {

// Cannot be done (neither is a requested feature) since:
// - we cannot call constructors of generic types;
// - DefaultEdge source and target are not public.

//    public static interface EdgeComposer<T,S,R> {
//        public R composeEdges(S a, T b);
//    }
//
//    public static final class DefaultEdgeComposer
//            implements EdgeComposer<DefaultEdge, DefaultEdge, DefaultEdge>
//    {
//        public DefaultEdge composeEdges(DefaultEdge b, DefaultEdge a) {
//            return new DefaultEdge();
//        }
//    }

    /*
     * Composition of relations <code>a</code> and <code>b</code>.
     *
     * Note the argument order!
     *
     * The result is currently always a directed graph. (But this may change
     * in the future.)
     *
     * TODO: Should we also sum weights of the two graphs?
     */
    public static <V> Graph<V, DefaultEdge> compose(Graph<V, DefaultEdge> b, Graph<V, DefaultEdge> a) {
        Graph<V, DefaultEdge> result = new DefaultDirectedGraph(DefaultEdge.class);
        for(V x : a.vertexSet()) {
            java.util.Set<DefaultEdge> edges1 = a.outgoingEdgesOf(x);
            for(V z : b.vertexSet()) {
                for(DefaultEdge e1: edges1) {
                    final V y = a.getEdgeTarget(e1);
                    if(b.containsEdge(y, z)) {
                        Graphs.addEdgeWithVertices(result, x, z);
                        break;
                    }
                }
            }
        }
        return result;
    }

}
