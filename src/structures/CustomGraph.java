package structures;

/**
 * Grafo Dirigido/No Dirigido implementado con Listas de Adyacencia.
 * Utiliza CustomHashTable para mapear vértices a sus listas de vecinos.
 */
public class CustomGraph<T> {
    private CustomHashTable<T, CustomList<T>> adjacencyList;
    private boolean bidirectional;

    public CustomGraph(boolean bidirectional) {
        this.adjacencyList = new CustomHashTable<>(50);
        this.bidirectional = bidirectional;
    }

    // Agrega un vértice al grafo si no existe
    public void addVertex(T vertex) {
        if (adjacencyList.get(vertex) == null) {
            adjacencyList.put(vertex, new CustomList<T>());
        }
    }

    // Agrega una arista entre dos vértices
    public void addEdge(T source, T destination) {
        addVertex(source);
        addVertex(destination);

        adjacencyList.get(source).add(destination);
        
        if (bidirectional) {
            adjacencyList.get(destination).add(source);
        }
    }

    // Retorna los vecinos de un vértice
    public CustomList<T> getNeighbors(T vertex) {
        return adjacencyList.get(vertex);
    }

    /**
     * Motor de recomendaciones simple:
     * Si el vértice 'A' (Cliente) visitó 'B' (Inmueble),
     * busca qué otros inmuebles 'C' han sido visitados por otros clientes 
     * que también visitaron 'B'.
     */
    public CustomList<T> getRecommendations(T startNode) {
        CustomList<T> recommendations = new CustomList<>();
        CustomList<T> visitedProperties = adjacencyList.get(startNode);
        
        if (visitedProperties == null) return recommendations;

        // Para cada inmueble que visitó el cliente inicial
        for (int i = 0; i < visitedProperties.getSize(); i++) {
            T property = visitedProperties.get(i);
            
            // Si el grafo es bidireccional, los vecinos de 'property' son otros clientes
            CustomList<T> otherClients = adjacencyList.get(property);
            if (otherClients != null) {
                for (int j = 0; j < otherClients.getSize(); j++) {
                    T otherClient = otherClients.get(j);
                    if (otherClient.equals(startNode)) continue;

                    // Para esos otros clientes, ver qué otros inmuebles visitaron
                    CustomList<T> otherProperties = adjacencyList.get(otherClient);
                    if (otherProperties != null) {
                        for (int k = 0; k < otherProperties.getSize(); k++) {
                            T recProperty = otherProperties.get(k);
                            // Si no es una que ya visitó y no está en recomendaciones
                            if (!contains(visitedProperties, recProperty) && !contains(recommendations, recProperty)) {
                                recommendations.add(recProperty);
                            }
                        }
                    }
                }
            }
        }
        return recommendations;
    }

    private boolean contains(CustomList<T> list, T item) {
        for (int i = 0; i < list.getSize(); i++) {
            if (list.get(i).equals(item)) return true;
        }
        return false;
    }
}
