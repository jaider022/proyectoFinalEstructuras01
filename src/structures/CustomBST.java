package structures;

/**
 * Árbol Binario de Búsqueda Genérico (Binary Search Tree - BST)
 * Su regla de oro es: los menores a la izquierda, los mayores a la derecha.
 * Nos permitirá organizar y buscar Inmuebles mágicamente por sus precios.
 */
public class CustomBST<K extends Comparable<K>, V> {
    private TreeNode<K, V> root;

    public CustomBST() {
        this.root = null;
    }

    // Insertar un nuevo nodo automáticamente ruteado por el árbol
    public void insert(K key, V value) {
        root = insertRec(root, key, value);
    }

    private TreeNode<K, V> insertRec(TreeNode<K, V> current, K key, V value) {
        if (current == null) {
            return new TreeNode<>(key, value); // Lugar ideal encontrado
        }

        // Si la llave a insertar es menor que la actual, viaje a la Lado Izquierdo
        if (key.compareTo(current.getKey()) < 0) {
            current.setLeft(insertRec(current.getLeft(), key, value));
        } 
        // Si es mayor, viaje al Lado Derecho
        else if (key.compareTo(current.getKey()) > 0) {
            current.setRight(insertRec(current.getRight(), key, value));
        } else {
            // Si insertan exactamente una llave repetida
            current.setValue(value);
        }
        return current;
    }

    // Encontrar algo en el árbol (Búsqueda Binaria O(log n))
    public V search(K key) {
        TreeNode<K, V> result = searchRec(root, key);
        return result == null ? null : result.getValue();
    }

    private TreeNode<K, V> searchRec(TreeNode<K, V> current, K key) {
        if (current == null || current.getKey().equals(key)) {
            return current;
        }

        if (key.compareTo(current.getKey()) < 0) {
            return searchRec(current.getLeft(), key);
        }
        return searchRec(current.getRight(), key);
    }

    // Función para imprimir el árbol de Menor a Mayor (Recorrido In-Order)
    public void printInOrder() {
        inOrderRec(root);
    }

    private void inOrderRec(TreeNode<K, V> node) {
        if (node != null) {
            inOrderRec(node.getLeft());
            System.out.println("  [$" + node.getKey() + "] -> " + node.getValue().toString());
            inOrderRec(node.getRight());
        }
    }

    // --- BÚSQUEDA POR RANGO (Req 5.6) ---
    /** Retorna todos los valores cuya clave está entre minKey y maxKey (inclusive). */
    public CustomList<V> rangeQuery(K minKey, K maxKey) {
        CustomList<V> result = new CustomList<>();
        rangeRec(root, minKey, maxKey, result);
        return result;
    }

    private void rangeRec(TreeNode<K, V> node, K minKey, K maxKey, CustomList<V> result) {
        if (node == null) return;
        // Si la clave actual es mayor que el mínimo, el subárbol izquierdo puede tener candidatos
        if (minKey.compareTo(node.getKey()) < 0) {
            rangeRec(node.getLeft(), minKey, maxKey, result);
        }
        // Añadir nodo actual si está dentro del rango
        if (minKey.compareTo(node.getKey()) <= 0 && maxKey.compareTo(node.getKey()) >= 0) {
            result.add(node.getValue());
        }
        // Si la clave actual es menor que el máximo, el subárbol derecho puede tener candidatos
        if (maxKey.compareTo(node.getKey()) > 0) {
            rangeRec(node.getRight(), minKey, maxKey, result);
        }
    }

    // Retornar lista ordenada
    public CustomList<V> toList() {
        CustomList<V> list = new CustomList<>();
        toListRec(root, list);
        return list;
    }

    private void toListRec(TreeNode<K, V> node, CustomList<V> list) {
        if (node != null) {
            toListRec(node.getLeft(), list);
            list.add(node.getValue());
            toListRec(node.getRight(), list);
        }
    }

    // --- NUEVO: ELIMINAR NODO ---
    public void delete(K key) {
        root = deleteRec(root, key);
    }

    private TreeNode<K, V> deleteRec(TreeNode<K, V> current, K key) {
        if (current == null) return null;

        if (key.compareTo(current.getKey()) < 0) {
            current.setLeft(deleteRec(current.getLeft(), key));
        } else if (key.compareTo(current.getKey()) > 0) {
            current.setRight(deleteRec(current.getRight(), key));
        } else {
            // Nodo encontrado! 
            // Casos 1 y 2: Sin hijos o un solo hijo
            if (current.getLeft() == null) return current.getRight();
            if (current.getRight() == null) return current.getLeft();

            // Caso 3: Dos hijos. Buscamos el sucesor (mínimo del subárbol derecho)
            TreeNode<K, V> successor = findMin(current.getRight());
            current.setKey(successor.getKey());
            current.setValue(successor.getValue());
            // Borramos el sucesor
            current.setRight(deleteRec(current.getRight(), current.getKey()));
        }
        return current;
    }

    private TreeNode<K, V> findMin(TreeNode<K, V> node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node;
    }
}
